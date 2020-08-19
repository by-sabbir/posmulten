package com.github.starnowski.posmulten.postgresql.core.context.enrichers

import com.github.starnowski.posmulten.postgresql.core.context.DefaultSharedSchemaContextBuilder
import com.github.starnowski.posmulten.postgresql.core.context.IsRecordBelongsToCurrentTenantFunctionDefinitionProducer
import com.github.starnowski.posmulten.postgresql.core.context.SharedSchemaContext
import com.github.starnowski.posmulten.postgresql.core.context.TableKey
import com.github.starnowski.posmulten.postgresql.core.rls.function.IGetCurrentTenantIdFunctionInvocationFactory
import com.github.starnowski.posmulten.postgresql.core.rls.function.IsRecordBelongsToCurrentTenantFunctionDefinition
import spock.lang.Specification
import spock.lang.Unroll

class IsRecordBelongsToCurrentTenantFunctionDefinitionsEnricherTest extends Specification {

    def tested = new IsRecordBelongsToCurrentTenantFunctionDefinitionsEnricher()

    @Unroll
    def "should create all required SQL definition that enables RLS policy but not force policy for schema #schema"()
    {
        given:
            def builder = new DefaultSharedSchemaContextBuilder(schema)
            builder.createRLSPolicyForColumn("users", [id: "bigint"], "tenant", "user_policy")
            builder.createRLSPolicyForColumn("comments", [uuid: "uuid"], "tenant_id", "comments_policy")
            builder.createRLSPolicyForColumn("some_table", [somedid: "bigint"], "tenant_xxx_id", "some_table_policy")
            def sharedSchemaContextRequest = builder.getSharedSchemaContextRequest()
            def context = new SharedSchemaContext()
            def iGetCurrentTenantIdFunctionInvocationFactory = Mock(IGetCurrentTenantIdFunctionInvocationFactory)
            context.setIGetCurrentTenantIdFunctionInvocationFactory(iGetCurrentTenantIdFunctionInvocationFactory)
            def usersTableSQLDefinition = Mock(IsRecordBelongsToCurrentTenantFunctionDefinition)
            def commentsTableSQLDefinition = Mock(IsRecordBelongsToCurrentTenantFunctionDefinition)
            def isRecordBelongsToCurrentTenantFunctionDefinitionProducer = Mock(IsRecordBelongsToCurrentTenantFunctionDefinitionProducer)
            tested.setIsRecordBelongsToCurrentTenantFunctionDefinitionProducer(isRecordBelongsToCurrentTenantFunctionDefinitionProducer)
            def usersTableKey = tk("users", schema)
            def commentsTableKey = tk("comments", schema)
            def usersTableColumns = sharedSchemaContextRequest.getTableColumnsList().get(usersTableKey)
            def commentsTableColumns = sharedSchemaContextRequest.getTableColumnsList().get(commentsTableKey)

        when:
            def result = tested.enrich(context, sharedSchemaContextRequest)

        then:
            1 * isRecordBelongsToCurrentTenantFunctionDefinitionProducer.produce(usersTableKey, usersTableColumns, iGetCurrentTenantIdFunctionInvocationFactory, "is_user_exists", schema) >> usersTableSQLDefinition
            1 * isRecordBelongsToCurrentTenantFunctionDefinitionProducer.produce(commentsTableKey, commentsTableColumns, iGetCurrentTenantIdFunctionInvocationFactory, "is_comment_exists", schema) >> commentsTableSQLDefinition
            0 * isRecordBelongsToCurrentTenantFunctionDefinitionProducer.produce(_)

            result.getSqlDefinitions().contains(usersTableSQLDefinition)
            result.getSqlDefinitions().contains(commentsTableSQLDefinition)

        where:
            schema << [null, "public", "some_schema"]
    }

    TableKey tk(String table, String schema)
    {
        new TableKey(table, schema)
    }
}
