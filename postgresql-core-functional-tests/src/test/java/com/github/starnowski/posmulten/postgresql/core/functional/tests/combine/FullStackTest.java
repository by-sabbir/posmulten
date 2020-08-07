package com.github.starnowski.posmulten.postgresql.core.functional.tests.combine;

import com.github.starnowski.posmulten.postgresql.core.*;
import com.github.starnowski.posmulten.postgresql.core.common.SQLDefinition;
import com.github.starnowski.posmulten.postgresql.core.functional.tests.TestNGSpringContextWithoutGenericTransactionalSupportTests;
import com.github.starnowski.posmulten.postgresql.core.rls.EnableRowLevelSecurityProducer;
import com.github.starnowski.posmulten.postgresql.core.rls.ForceRowLevelSecurityProducer;
import com.github.starnowski.posmulten.postgresql.core.rls.RLSPolicyProducer;
import com.github.starnowski.posmulten.postgresql.core.rls.function.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.Test;

import static com.github.starnowski.posmulten.postgresql.core.rls.DefaultRLSPolicyProducerParameters.builder;
import static com.github.starnowski.posmulten.postgresql.core.rls.PermissionCommandPolicyEnum.ALL;
import static com.github.starnowski.posmulten.postgresql.test.utils.TestUtils.VALID_CURRENT_TENANT_ID_PROPERTY_NAME;

public abstract class FullStackTest extends TestNGSpringContextWithoutGenericTransactionalSupportTests {

    protected static final String USER_TENANT = "primary_tenant";
    protected static final String SECONDARY_USER_TENANT = "someXDAFAS_id";
    protected static final String CUSTOM_TENANT_COLUMN_NAME = "tenant";
    protected static final String TENANT_COLUMN_NAME = "tenant";

    abstract protected String getSchema();

    protected ISetCurrentTenantIdFunctionInvocationFactory setCurrentTenantIdFunctionInvocationFactory;

    @Autowired
    @Qualifier("ownerJdbcTemplate")
    protected JdbcTemplate ownerJdbcTemplate;

    @Test(testName = "create SQL definitions", description = "Create SQL function that creates statements that set current tenant value, retrieve current tenant value and create the row level security policy for a table that is multi-tenant aware")
    public void createSQLDefinitions()
    {
        //Create function that returns current tenant function
        GetCurrentTenantIdFunctionProducer getCurrentTenantIdFunctionProducer = new GetCurrentTenantIdFunctionProducer();
        GetCurrentTenantIdFunctionDefinition getCurrentTenantIdFunctionDefinition = getCurrentTenantIdFunctionProducer.produce(new GetCurrentTenantIdFunctionProducerParameters("rls_get_current_tenant", VALID_CURRENT_TENANT_ID_PROPERTY_NAME, getSchema(), null));
        sqlDefinitions.add(getCurrentTenantIdFunctionDefinition);

        //Create function that sets current tenant function
        SetCurrentTenantIdFunctionProducer setCurrentTenantIdFunctionProducer = new SetCurrentTenantIdFunctionProducer();
        SetCurrentTenantIdFunctionDefinition setCurrentTenantIdFunctionDefinition = setCurrentTenantIdFunctionProducer.produce(new SetCurrentTenantIdFunctionProducerParameters("rls_set_current_tenant", VALID_CURRENT_TENANT_ID_PROPERTY_NAME, getSchema(), null));
        setCurrentTenantIdFunctionInvocationFactory = setCurrentTenantIdFunctionDefinition;
        sqlDefinitions.add(setCurrentTenantIdFunctionDefinition);

        // EqualsCurrentTenantIdentifierFunctionProducer
        EqualsCurrentTenantIdentifierFunctionProducer equalsCurrentTenantIdentifierFunctionProducer = new EqualsCurrentTenantIdentifierFunctionProducer();
        EqualsCurrentTenantIdentifierFunctionDefinition equalsCurrentTenantIdentifierFunctionDefinition = equalsCurrentTenantIdentifierFunctionProducer.produce(new EqualsCurrentTenantIdentifierFunctionProducerParameters("is_id_equals_current_tenant_id", getSchema(), null, getCurrentTenantIdFunctionDefinition));
        sqlDefinitions.add(equalsCurrentTenantIdentifierFunctionDefinition);

        // TenantHasAuthoritiesFunctionProducer
        TenantHasAuthoritiesFunctionProducer tenantHasAuthoritiesFunctionProducer = new TenantHasAuthoritiesFunctionProducer();
        TenantHasAuthoritiesFunctionDefinition tenantHasAuthoritiesFunctionDefinition = tenantHasAuthoritiesFunctionProducer.produce(new TenantHasAuthoritiesFunctionProducerParameters("tenant_has_authorities", getSchema(), equalsCurrentTenantIdentifierFunctionDefinition));
        sqlDefinitions.add(tenantHasAuthoritiesFunctionDefinition);


        // Custom tenant column
        // Create tenant column in the notifications table
        CreateColumnStatementProducer createColumnStatementProducer = new CreateColumnStatementProducer();
        sqlDefinitions.add(createColumnStatementProducer.produce(new CreateColumnStatementProducerParameters(NOTIFICATIONS_TABLE_NAME, CUSTOM_TENANT_COLUMN_NAME, "character varying(255)", getSchema())));

        // Setting default value
        SetDefaultStatementProducer setDefaultStatementProducer = new SetDefaultStatementProducer();
        sqlDefinitions.add(setDefaultStatementProducer.produce(new SetDefaultStatementProducerParameters(NOTIFICATIONS_TABLE_NAME, CUSTOM_TENANT_COLUMN_NAME, getCurrentTenantIdFunctionDefinition.returnGetCurrentTenantIdFunctionInvocation(), getSchema())));

        // Setting NOT NULL declaration
        SetNotNullStatementProducer setNotNullStatementProducer = new SetNotNullStatementProducer();
        sqlDefinitions.add(setNotNullStatementProducer.produce(new SetNotNullStatementProducerParameters(NOTIFICATIONS_TABLE_NAME, CUSTOM_TENANT_COLUMN_NAME, getSchema())));

        // RLS - users
        // EnableRowLevelSecurityProducer
        EnableRowLevelSecurityProducer enableRowLevelSecurityProducer = new EnableRowLevelSecurityProducer();
        sqlDefinitions.add(enableRowLevelSecurityProducer.produce(USERS_TABLE_NAME, getSchema()));

        // ForceRowLevelSecurityProducer - forcing the row level security policy for table owner
        ForceRowLevelSecurityProducer forceRowLevelSecurityProducer = new ForceRowLevelSecurityProducer();
        sqlDefinitions.add(forceRowLevelSecurityProducer.produce(USERS_TABLE_NAME, getSchema()));

        // RLSPolicyProducer
        RLSPolicyProducer rlsPolicyProducer = new RLSPolicyProducer();
        SQLDefinition usersRLSPolicySQLDefinition = rlsPolicyProducer.produce(builder().withPolicyName("users_table_rls_policy")
                .withPolicySchema(getSchema())
                .withPolicyTable(USERS_TABLE_NAME)
                .withGrantee(CORE_OWNER_USER)
                .withPermissionCommandPolicy(ALL)
                .withUsingExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .withWithCheckExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .build());
        sqlDefinitions.add(usersRLSPolicySQLDefinition);

        // RLS - notifications
        // Enable Row Level Security for notifications table
        sqlDefinitions.add(enableRowLevelSecurityProducer.produce(NOTIFICATIONS_TABLE_NAME, getSchema()));

        // forcing the row level security policy for notifications table
        sqlDefinitions.add(forceRowLevelSecurityProducer.produce(NOTIFICATIONS_TABLE_NAME, getSchema()));

        // Adding RLS for the notifications table
        SQLDefinition notificationsRLSPolicySQLDefinition = rlsPolicyProducer.produce(builder().withPolicyName("notifications_table_rls_policy")
                .withPolicySchema(getSchema())
                .withPolicyTable(NOTIFICATIONS_TABLE_NAME)
                .withGrantee(CORE_OWNER_USER)
                .withPermissionCommandPolicy(ALL)
                .withUsingExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .withWithCheckExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .withTenantIdColumn(CUSTOM_TENANT_COLUMN_NAME)
                .build());
        sqlDefinitions.add(notificationsRLSPolicySQLDefinition);

        // RLS - posts
        // Enable Row Level Security for posts table
        sqlDefinitions.add(enableRowLevelSecurityProducer.produce(POSTS_TABLE_NAME, getSchema()));

        // forcing the row level security policy for posts table
        sqlDefinitions.add(forceRowLevelSecurityProducer.produce(POSTS_TABLE_NAME, getSchema()));

        // Adding RLS for the posts table
        SQLDefinition postsRLSPolicySQLDefinition = rlsPolicyProducer.produce(builder().withPolicyName("posts_table_rls_policy")
                .withPolicySchema(getSchema())
                .withPolicyTable(POSTS_TABLE_NAME)
                .withGrantee(CORE_OWNER_USER)
                .withPermissionCommandPolicy(ALL)
                .withUsingExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .withWithCheckExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .build());
        sqlDefinitions.add(postsRLSPolicySQLDefinition);

        // RLS - groups
        // Enable Row Level Security for groups table
        sqlDefinitions.add(enableRowLevelSecurityProducer.produce(GROUPS_TABLE_NAME, getSchema()));

        // forcing the row level security policy for groups table
        sqlDefinitions.add(forceRowLevelSecurityProducer.produce(GROUPS_TABLE_NAME, getSchema()));

        // Adding RLS for the groups table
        SQLDefinition groupsRLSPolicySQLDefinition = rlsPolicyProducer.produce(builder().withPolicyName("groups_table_rls_policy")
                .withPolicySchema(getSchema())
                .withPolicyTable(GROUPS_TABLE_NAME)
                .withGrantee(CORE_OWNER_USER)
                .withPermissionCommandPolicy(ALL)
                .withUsingExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .withWithCheckExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .build());
        sqlDefinitions.add(groupsRLSPolicySQLDefinition);

        // RLS - users_groups
        // Enable Row Level Security for users_groups table
        sqlDefinitions.add(enableRowLevelSecurityProducer.produce(USERS_GROUPS_TABLE_NAME, getSchema()));

        // forcing the row level security policy for groups table
        sqlDefinitions.add(forceRowLevelSecurityProducer.produce(USERS_GROUPS_TABLE_NAME, getSchema()));

        // Adding RLS for the groups table
        SQLDefinition usersGroupsRLSPolicySQLDefinition = rlsPolicyProducer.produce(builder().withPolicyName("users_groups_table_rls_policy")
                .withPolicySchema(getSchema())
                .withPolicyTable(USERS_GROUPS_TABLE_NAME)
                .withGrantee(CORE_OWNER_USER)
                .withPermissionCommandPolicy(ALL)
                .withUsingExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .withWithCheckExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .build());
        sqlDefinitions.add(usersGroupsRLSPolicySQLDefinition);

        // RLS - comments
        // Enable Row Level Security for comments table
        sqlDefinitions.add(enableRowLevelSecurityProducer.produce(COMMENTS_TABLE_NAME, getSchema()));

        // forcing the row level security policy for groups table
        sqlDefinitions.add(forceRowLevelSecurityProducer.produce(COMMENTS_TABLE_NAME, getSchema()));

        // Adding RLS for the groups table
        SQLDefinition commentsGroupsRLSPolicySQLDefinition = rlsPolicyProducer.produce(builder().withPolicyName("comments_table_rls_policy")
                .withPolicySchema(getSchema())
                .withPolicyTable(COMMENTS_TABLE_NAME)
                .withGrantee(CORE_OWNER_USER)
                .withPermissionCommandPolicy(ALL)
                .withUsingExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .withWithCheckExpressionTenantHasAuthoritiesFunctionInvocationFactory(tenantHasAuthoritiesFunctionDefinition)
                .build());
        sqlDefinitions.add(commentsGroupsRLSPolicySQLDefinition);


    }

}
