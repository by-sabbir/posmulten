package com.github.starnowski.posmulten.postgresql.core.context.enrichers;

import com.github.starnowski.posmulten.postgresql.core.context.AbstractSharedSchemaContext;
import com.github.starnowski.posmulten.postgresql.core.context.AbstractSharedSchemaContextEnricher;
import com.github.starnowski.posmulten.postgresql.core.context.SharedSchemaContextRequest;
import com.github.starnowski.posmulten.postgresql.core.context.SingleTenantColumnSQLDefinitionsProducer;

public class TenantColumnSQLDefinitionsEnricher implements AbstractSharedSchemaContextEnricher {

    private SingleTenantColumnSQLDefinitionsProducer singleTenantColumnSQLDefinitionsProducer = new SingleTenantColumnSQLDefinitionsProducer();

    @Override
    public AbstractSharedSchemaContext enrich(AbstractSharedSchemaContext context, SharedSchemaContextRequest request) {
        //TODO Add exception for non-existed table declaration for which rls policy should be created
        return null;
    }

    void setSingleTenantColumnSQLDefinitionsProducer(SingleTenantColumnSQLDefinitionsProducer singleTenantColumnSQLDefinitionsProducer) {
        this.singleTenantColumnSQLDefinitionsProducer = singleTenantColumnSQLDefinitionsProducer;
    }
}
