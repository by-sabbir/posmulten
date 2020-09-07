package com.github.starnowski.posmulten.postgresql.core.rls.function

import com.github.starnowski.posmulten.postgresql.core.common.function.AbstractFunctionFactoryTest
import spock.lang.Unroll

import static com.github.starnowski.posmulten.postgresql.core.common.function.FunctionArgumentValue.forReference
import static com.github.starnowski.posmulten.postgresql.core.common.function.FunctionArgumentValue.forString

class IsTenantValidBasedOnConstantValuesFunctionProducerTest extends AbstractFunctionFactoryTest {

    def tested = new IsTenantValidBasedOnConstantValuesFunctionProducer()

    @Unroll
    def "should generate statement that creates function '#testFunctionName' for schema '#testSchema' with argument type '#argumentType' (null means 'text') which invalid tenant values'#invalidTenantValues'" () {
        expect:
            tested.produce(new IsTenantValidBasedOnConstantValuesFunctionProducerParameters(testFunctionName, testSchema, new HashSet<String>(invalidTenantValues), argumentType)).getCreateScript() == expectedStatement

        where:
            testSchema              |   testFunctionName            |   invalidTenantValues     |   argumentType        ||  expectedStatement
            null                    |   "is_tenant_valid"           |   ["XX-dadf-dsa"]         |   null                ||  "CREATE OR REPLACE FUNCTION is_tenant_valid(text) RETURNS BOOLEAN AS \$\$\nSELECT \$1 <> CAST ('XX-dadf-dsa' AS text)\n\$\$ LANGUAGE sql\nIMMUTABLE\nPARALLEL SAFE;"
            "public"                |   "is_tenant_valid"           |   ["XX-dadf-dsa"]         |   null                ||  "CREATE OR REPLACE FUNCTION public.is_tenant_valid(text) RETURNS BOOLEAN AS \$\$\nSELECT \$1 <> CAST ('XX-dadf-dsa' AS text)\n\$\$ LANGUAGE sql\nIMMUTABLE\nPARALLEL SAFE;"
            "non_public_schema"     |   "is_tenant_valid"           |   ["XX-dadf-dsa"]         |   null                ||  "CREATE OR REPLACE FUNCTION non_public_schema.is_tenant_valid(text) RETURNS BOOLEAN AS \$\$\nSELECT \$1 <> CAST ('XX-dadf-dsa' AS text)\n\$\$ LANGUAGE sql\nIMMUTABLE\nPARALLEL SAFE;"
            null                    |   "is_valid_ten"              |   ["3325", "adfzxcvz"]    |   null                ||  "CREATE OR REPLACE FUNCTION is_valid_ten(text) RETURNS BOOLEAN AS \$\$\nSELECT \$1 <> CAST ('3325' AS text) AND \$1 <> CAST ('adfzxcvz' AS text)\n\$\$ LANGUAGE sql\nIMMUTABLE\nPARALLEL SAFE;"
            "public"                |   "valid_tenant"              |   ["dgfsg", "433"]        |   "VARCHAR(32)"       ||  "CREATE OR REPLACE FUNCTION public.valid_tenant(VARCHAR(32)) RETURNS BOOLEAN AS \$\$\nSELECT \$1 <> CAST ('433' AS VARCHAR(32)) AND \$1 <> CAST ('dgfsg' AS VARCHAR(32))\n\$\$ LANGUAGE sql\nIMMUTABLE\nPARALLEL SAFE;"
            "schema2"               |   "tenant_is_correct"         |   ["66", "12", "0"]       |   "INTEGER"           ||  "CREATE OR REPLACE FUNCTION schema2.tenant_is_correct(INTEGER) RETURNS BOOLEAN AS \$\$\nSELECT \$1 <> CAST ('66' AS INTEGER) AND \$1 <> CAST ('0' AS INTEGER) AND \$1 <> CAST ('12' AS INTEGER)\n\$\$ LANGUAGE sql\nIMMUTABLE\nPARALLEL SAFE;"
    }

    @Unroll
    def "should generate sql function with name '#testFunctionName' for schema '#testSchema' that returns correct function invocation #expectedInvocation for argument #argument" () {
        expect:
            tested.produce(new IsTenantValidBasedOnConstantValuesFunctionProducerParameters(testFunctionName, testSchema, new HashSet<String>("N/A"), null)).returnIsTenantValidFunctionInvocation(argument) == expectedInvocation

        where:
            testSchema              |   testFunctionName            |   argument                    ||  expectedInvocation
            null                    |   "is_tenant_valid"           |   forString("sfdadf")         ||  "is_tenant_valid('sfdadf')"
            "public"                |   "tenant_valid"              |   forString("XXDFASD")        ||  "public.tenant_valid('XXDFASD')"
            "non_public_schema"     |   "is_tenant_correct"         |   forString("zz-dfadf-sfa")   ||  "non_public_schema.is_tenant_correct('zz-dfadf-sfa')"
            null                    |   "fun1"                      |   forReference("tenant")      ||  "fun1(tenant)"
            "public"                |   "correct_tenant"            |   forReference("col1")        ||  "public.correct_tenant(col1)"
            "non_public_schema"     |   "tenant_is_correct"         |   forReference("id")          ||  "non_public_schema.tenant_is_correct(id)"
    }

    //TODO null values
    //TODO Empty values
    //TODO empty argument type

    @Override
    protected returnTestedObject() {
        new IsTenantValidBasedOnConstantValuesFunctionProducer()
    }

    @Override
    protected returnCorrectParametersSpyObject() {
        Spy(IsTenantValidBasedOnConstantValuesFunctionProducerParameters, constructorArgs: ["is_tenant_valid",
                                                                                "public",
                                                                                new HashSet<>(Arrays.asList("bad_tenant", "tenant_1")),
                                                                                "VARCHAR(255)"])
    }
}
