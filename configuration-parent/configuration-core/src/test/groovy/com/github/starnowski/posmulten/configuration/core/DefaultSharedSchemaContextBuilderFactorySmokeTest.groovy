package com.github.starnowski.posmulten.configuration.core

import com.github.starnowski.posmulten.configuration.core.model.RLSPolicy
import com.github.starnowski.posmulten.configuration.core.model.SharedSchemaContextConfiguration
import com.github.starnowski.posmulten.configuration.core.model.TableEntry
import com.github.starnowski.posmulten.configuration.core.model.ValidTenantValueConstraintConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.starnowski.posmulten.postgresql.test.utils.MapBuilder.mapBuilder
import static java.util.Arrays.asList

class DefaultSharedSchemaContextBuilderFactorySmokeTest extends Specification {

    def tested = new DefaultSharedSchemaContextBuilderFactory()

    @Unroll
    def "should able to create builder components based on correct configuration object (#configuration)"()
    {
        when:
            def builder = tested.build(configuration)

        then:
            builder

        and: "builder should create correct context with ddl statements"
            builder.build().getSqlDefinitions().size() > 0

        where:
            configuration << [
                    new SharedSchemaContextConfiguration().setDefaultSchema("public")
                        .setGrantee("db-user")
                        .setValidTenantValueConstraint(new ValidTenantValueConstraintConfiguration()
                                                        .setTenantIdentifiersBlacklist(asList("xxxx")))
                        .setTables(asList(new TableEntry().setName("users")
                                                .setRlsPolicy(new RLSPolicy()
                                                                .setName("rls_pol")
                                                                .setNameForFunctionThatChecksIfRecordExistsInTable("does_users_record_exists")
                                                                .setPrimaryKeyColumnsNameToTypeMap(mapBuilder().put("id", "biging").build()))))
            ]
    }
}
