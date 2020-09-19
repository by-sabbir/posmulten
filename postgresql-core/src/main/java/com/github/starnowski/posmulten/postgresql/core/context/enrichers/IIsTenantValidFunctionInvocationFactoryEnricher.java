package com.github.starnowski.posmulten.postgresql.core.context.enrichers;

import com.github.starnowski.posmulten.postgresql.core.context.ISharedSchemaContext;
import com.github.starnowski.posmulten.postgresql.core.context.SharedSchemaContextRequest;
import com.github.starnowski.posmulten.postgresql.core.context.exceptions.SharedSchemaContextBuilderException;
import com.github.starnowski.posmulten.postgresql.core.rls.function.IsTenantValidBasedOnConstantValuesFunctionDefinition;
import com.github.starnowski.posmulten.postgresql.core.rls.function.IsTenantValidBasedOnConstantValuesFunctionProducer;
import com.github.starnowski.posmulten.postgresql.core.rls.function.IsTenantValidBasedOnConstantValuesFunctionProducerParameters;

import java.util.HashSet;

public class IIsTenantValidFunctionInvocationFactoryEnricher implements ISharedSchemaContextEnricher {

    private final IsTenantValidBasedOnConstantValuesFunctionProducer isTenantIdentifierValidConstraintProducer;

    public IIsTenantValidFunctionInvocationFactoryEnricher() {
        this(new IsTenantValidBasedOnConstantValuesFunctionProducer());
    }

    public IIsTenantValidFunctionInvocationFactoryEnricher(IsTenantValidBasedOnConstantValuesFunctionProducer isTenantIdentifierValidConstraintProducer) {
        this.isTenantIdentifierValidConstraintProducer = isTenantIdentifierValidConstraintProducer;
    }

    @Override
    public ISharedSchemaContext enrich(ISharedSchemaContext context, SharedSchemaContextRequest request) throws SharedSchemaContextBuilderException {
        if (request.getTenantValuesBlacklist() != null && !request.getTenantValuesBlacklist().isEmpty())
        {
            String testFunctionName = "is_tenant_identifier_valid";
            IsTenantValidBasedOnConstantValuesFunctionDefinition sqlFunctionDefinition = isTenantIdentifierValidConstraintProducer.produce(new IsTenantValidBasedOnConstantValuesFunctionProducerParameters(testFunctionName, request.getDefaultSchema(), new HashSet<String>(request.getTenantValuesBlacklist()), request.getCurrentTenantIdPropertyType()));
            context.addSQLDefinition(sqlFunctionDefinition);
            context.setIIsTenantValidFunctionInvocationFactory(sqlFunctionDefinition);
        }
        return context;
    }
}
