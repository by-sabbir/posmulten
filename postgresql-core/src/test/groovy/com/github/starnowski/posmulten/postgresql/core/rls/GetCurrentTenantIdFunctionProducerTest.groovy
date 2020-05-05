package com.github.starnowski.posmulten.postgresql.core.rls


import spock.lang.Specification
import spock.lang.Unroll

class GetCurrentTenantIdFunctionProducerTest extends Specification {

    def tested = new GetCurrentTenantIdFunctionProducer()

    @Unroll
    def "should generate statement that creates function '#testFunctionName' for schema '#testSchema' which returns type '#testReturnType' which returns value for property '#testCurrentTenantIdProperty'" () {
        expect:
            tested.produce(new GetCurrentTenantIdFunctionProducerParameters(testFunctionName, testCurrentTenantIdProperty, testSchema, testReturnType)) == expectedStatement

        where:
            testSchema              |   testFunctionName            |   testCurrentTenantIdProperty     |   testReturnType      || expectedStatement
            null                    |   "get_current_tenant"        |   "c.c_ten"                       |   null                ||  "CREATE OR REPLACE FUNCTION get_current_tenant() RETURNS VARCHAR(255) as \$\$\nSELECT current_setting('c.c_ten')\n\$\$ LANGUAGE sql;"
            "public"                |   "get_current_tenant"        |   "c.c_ten"                       |   null                ||  "CREATE OR REPLACE FUNCTION public.get_current_tenant() RETURNS VARCHAR(255) as \$\$\nSELECT current_setting('c.c_ten')\n\$\$ LANGUAGE sql;"
            "non_public_schema"     |   "get_current_tenant"        |   "c.c_ten"                       |   null                ||  "CREATE OR REPLACE FUNCTION non_public_schema.get_current_tenant() RETURNS VARCHAR(255) as \$\$\nSELECT current_setting('c.c_ten')\n\$\$ LANGUAGE sql;"
            null                    |   "get_current_tenant"        |   "c.c_ten"                       |   "text"              ||  "CREATE OR REPLACE FUNCTION get_current_tenant() RETURNS text as \$\$\nSELECT current_setting('c.c_ten')\n\$\$ LANGUAGE sql;"
            "public"                |   "get_current_tenant"        |   "c.c_ten"                       |   "text"              ||  "CREATE OR REPLACE FUNCTION public.get_current_tenant() RETURNS text as \$\$\nSELECT current_setting('c.c_ten')\n\$\$ LANGUAGE sql;"
            "non_public_schema"     |   "get_current_tenant"        |   "c.c_ten"                       |   "text"              ||  "CREATE OR REPLACE FUNCTION non_public_schema.get_current_tenant() RETURNS text as \$\$\nSELECT current_setting('c.c_ten')\n\$\$ LANGUAGE sql;"
            null                    |   "cur_tenant_val"            |   "con.tenant_id"                 |   "VARCHAR(128)"      ||  "CREATE OR REPLACE FUNCTION cur_tenant_val() RETURNS VARCHAR(128) as \$\$\nSELECT current_setting('con.tenant_id')\n\$\$ LANGUAGE sql;"
            "public"                |   "give_me_tenant"            |   "pos.tenant"                    |   "VARCHAR(32)"       ||  "CREATE OR REPLACE FUNCTION public.give_me_tenant() RETURNS VARCHAR(32) as \$\$\nSELECT current_setting('pos.tenant')\n\$\$ LANGUAGE sql;"
            "non_public_schema"     |   "return_current_tenant"     |   "t.id"                          |   "text"              ||  "CREATE OR REPLACE FUNCTION non_public_schema.return_current_tenant() RETURNS text as \$\$\nSELECT current_setting('t.id')\n\$\$ LANGUAGE sql;"
    }
}
