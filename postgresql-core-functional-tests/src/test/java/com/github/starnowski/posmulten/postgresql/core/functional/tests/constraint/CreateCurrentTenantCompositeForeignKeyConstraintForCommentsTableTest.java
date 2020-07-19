package com.github.starnowski.posmulten.postgresql.core.functional.tests.constraint;

import com.github.starnowski.posmulten.postgresql.core.common.SQLDefinition;
import com.github.starnowski.posmulten.postgresql.core.common.function.FunctionArgumentValue;
import com.github.starnowski.posmulten.postgresql.core.functional.tests.TestNGSpringContextWithoutGenericTransactionalSupportTests;
import com.github.starnowski.posmulten.postgresql.core.rls.DefaultIsRecordBelongsToCurrentTenantConstraintProducerParameters;
import com.github.starnowski.posmulten.postgresql.core.rls.IsRecordBelongsToCurrentTenantConstraintProducer;
import com.github.starnowski.posmulten.postgresql.core.rls.IsRecordBelongsToCurrentTenantConstraintProducerParameters;
import com.github.starnowski.posmulten.postgresql.core.rls.function.*;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.starnowski.posmulten.postgresql.core.common.function.FunctionArgumentValue.forReference;
import static com.github.starnowski.posmulten.postgresql.core.rls.function.AbstractIsRecordBelongsToCurrentTenantProducerParameters.pairOfColumnWithType;
import static com.github.starnowski.posmulten.postgresql.test.utils.TestUtils.VALID_CURRENT_TENANT_ID_PROPERTY_NAME;

public class CreateCurrentTenantCompositeForeignKeyConstraintForCommentsTableTest extends TestNGSpringContextWithoutGenericTransactionalSupportTests {
    //TODO

    protected static final String CONSTRAINT_NAME = "posts_user_info_fk_cu";
    protected static final String USER_TENANT = "primary_tenant";
    protected static final String SECONDARY_USER_TENANT = "someXDAFAS_id";

    protected String getSchema()
    {
        return null;
    }

    protected String getUsersTableReference()
    {
        return (getSchema() == null ? "" : getSchema() + ".") + "users";
    }

    protected String getPostsTableReference()
    {
        return (getSchema() == null ? "" : getSchema() + ".") + "posts";
    }

    SetCurrentTenantIdFunctionDefinition setCurrentTenantIdFunctionDefinition;

    @Test(testName = "create SQL definitions", description = "Create SQL function that creates statements that set current tenant value, retrieve current tenant value and create a constraint for a foreign key for a table that is multi-tenant aware")
    public void createSQLDefinitions()
    {
        //Create function that returns current tenant function
        GetCurrentTenantIdFunctionProducer getCurrentTenantIdFunctionProducer = new GetCurrentTenantIdFunctionProducer();
        GetCurrentTenantIdFunctionDefinition getCurrentTenantIdFunctionDefinition = getCurrentTenantIdFunctionProducer.produce(new GetCurrentTenantIdFunctionProducerParameters("rls_get_current_tenant", VALID_CURRENT_TENANT_ID_PROPERTY_NAME, getSchema(), null));
        sqlDefinitions.add(getCurrentTenantIdFunctionDefinition);

        //Create function that sets current tenant function
        SetCurrentTenantIdFunctionProducer setCurrentTenantIdFunctionProducer = new SetCurrentTenantIdFunctionProducer();
        setCurrentTenantIdFunctionDefinition = setCurrentTenantIdFunctionProducer.produce(new SetCurrentTenantIdFunctionProducerParameters("rls_set_current_tenant", VALID_CURRENT_TENANT_ID_PROPERTY_NAME, getSchema(), null));
        sqlDefinitions.add(setCurrentTenantIdFunctionDefinition);

        // Does record belongs to current tenant
        AbstractIsRecordBelongsToCurrentTenantProducerParameters isRecordBelongsToCurrentTenantProducerParameters = new IsRecordBelongsToCurrentTenantProducerParameters.Builder()
                .withSchema(getSchema())
                .withFunctionName("is_user_belongs_to_current_tenant")
                .withRecordTableName("users")
                .withRecordSchemaName(getSchema())
                .withiGetCurrentTenantIdFunctionInvocationFactory(getCurrentTenantIdFunctionDefinition)
                .withTenantColumn("tenant_id")
                .withKeyColumnsPairsList(ImmutableList.of(pairOfColumnWithType("id", "bigint"))).build();
        IsRecordBelongsToCurrentTenantProducer isRecordBelongsToCurrentTenantProducer = new IsRecordBelongsToCurrentTenantProducer();
        IsRecordBelongsToCurrentTenantFunctionDefinition isRecordBelongsToCurrentTenantFunctionDefinition = isRecordBelongsToCurrentTenantProducer.produce(isRecordBelongsToCurrentTenantProducerParameters);
        sqlDefinitions.add(isRecordBelongsToCurrentTenantFunctionDefinition);

        // Constraint
        IsRecordBelongsToCurrentTenantConstraintProducer isRecordBelongsToCurrentTenantConstraintProducer = new IsRecordBelongsToCurrentTenantConstraintProducer();
        //user_id
        Map<String, FunctionArgumentValue> primaryColumnsValuesMap = new HashMap<>();
        primaryColumnsValuesMap.put("id", forReference("user_id"));
        IsRecordBelongsToCurrentTenantConstraintProducerParameters isRecordBelongsToCurrentTenantConstraintProducerParameters = DefaultIsRecordBelongsToCurrentTenantConstraintProducerParameters.builder()
                .withConstraintName(CONSTRAINT_NAME)
                .withTableName("posts")
                .withTableSchema(getSchema())
                .withIsRecordBelongsToCurrentTenantFunctionInvocationFactory(isRecordBelongsToCurrentTenantFunctionDefinition)
                .withPrimaryColumnsValuesMap(primaryColumnsValuesMap).build();
        SQLDefinition recordBelongsToCurrentTenantConstrainSqlDefinition = isRecordBelongsToCurrentTenantConstraintProducer.produce(isRecordBelongsToCurrentTenantConstraintProducerParameters);
        sqlDefinitions.add(recordBelongsToCurrentTenantConstrainSqlDefinition);
    }
}
