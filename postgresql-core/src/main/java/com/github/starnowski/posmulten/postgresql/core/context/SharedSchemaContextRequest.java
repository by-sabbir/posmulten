/**
 *     Posmulten library is an open-source project for the generation
 *     of SQL DDL statements that make it easy for implementation of
 *     Shared Schema Multi-tenancy strategy via the Row Security
 *     Policies in the Postgres database.
 *
 *     Copyright (C) 2020  Szymon Tarnowski
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */
package com.github.starnowski.posmulten.postgresql.core.context;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SharedSchemaContextRequest implements Cloneable{

    private String defaultSchema;
    private String currentTenantIdProperty = "posmulten.tenant_id";
    /**
     * Type of column that stores the tenant identifier.
     * Default value is "VARCHAR(255)".
     */
    private String currentTenantIdPropertyType = "VARCHAR(255)";
    /**
     * Name of the function that returns current tenant identifier.
     */
    private String getCurrentTenantIdFunctionName;
    /**
     * Name of the function that set current tenant identifier.
     */
    private String setCurrentTenantIdFunctionName;
    /**
     * Name of the function that checks if passed identifier is equal to the current tenant identifier.
     */
    private String equalsCurrentTenantIdentifierFunctionName;
    private String tenantHasAuthoritiesFunctionName;
    private String defaultTenantIdColumn = "tenant_id";
    private Map<TableKey, AbstractTableColumns> tableColumnsList = new HashMap<>();
    private Set<TableKey> createTenantColumnTableLists = new HashSet<>();
    private boolean forceRowLevelSecurityForTableOwner;
    private Map<TableKey, AbstractTableRLSPolicyProperties> tableRLSPolicies = new HashMap<>();
    private Map<SameTenantConstraintForForeignKey, AbstractSameTenantConstraintForForeignKeyProperties> sameTenantConstraintForForeignKeyProperties = new HashMap<>();
    private String grantee;
    private Map<TableKey, String> functionThatChecksIfRecordExistsInTableNames = new HashMap<>();

    public String getDefaultTenantIdColumn() {
        return defaultTenantIdColumn;
    }

    public void setDefaultTenantIdColumn(String defaultTenantIdColumn) {
        this.defaultTenantIdColumn = defaultTenantIdColumn;
    }

    public String getEqualsCurrentTenantIdentifierFunctionName() {
        return equalsCurrentTenantIdentifierFunctionName;
    }

    public void setEqualsCurrentTenantIdentifierFunctionName(String equalsCurrentTenantIdentifierFunctionName) {
        this.equalsCurrentTenantIdentifierFunctionName = equalsCurrentTenantIdentifierFunctionName;
    }

    public String getTenantHasAuthoritiesFunctionName() {
        return tenantHasAuthoritiesFunctionName;
    }

    public void setTenantHasAuthoritiesFunctionName(String tenantHasAuthoritiesFunctionName) {
        this.tenantHasAuthoritiesFunctionName = tenantHasAuthoritiesFunctionName;
    }

    public String getSetCurrentTenantIdFunctionName() {
        return setCurrentTenantIdFunctionName;
    }

    public void setSetCurrentTenantIdFunctionName(String setCurrentTenantIdFunctionName) {
        this.setCurrentTenantIdFunctionName = setCurrentTenantIdFunctionName;
    }

    public String getGetCurrentTenantIdFunctionName() {
        return getCurrentTenantIdFunctionName;
    }

    public void setGetCurrentTenantIdFunctionName(String getCurrentTenantIdFunctionName) {
        this.getCurrentTenantIdFunctionName = getCurrentTenantIdFunctionName;
    }

    public String getCurrentTenantIdPropertyType() {
        return currentTenantIdPropertyType;
    }

    public void setCurrentTenantIdPropertyType(String currentTenantIdPropertyType) {
        this.currentTenantIdPropertyType = currentTenantIdPropertyType;
    }

    public String getCurrentTenantIdProperty() {
        return currentTenantIdProperty;
    }

    public void setCurrentTenantIdProperty(String currentTenantIdProperty) {
        this.currentTenantIdProperty = currentTenantIdProperty;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
        this.defaultSchema = defaultSchema;
    }

    public Map<TableKey, AbstractTableColumns> getTableColumnsList() {
        return tableColumnsList;
    }

    public Set<TableKey> getCreateTenantColumnTableLists() {
        return createTenantColumnTableLists;
    }

    public boolean isForceRowLevelSecurityForTableOwner() {
        return forceRowLevelSecurityForTableOwner;
    }

    public void setForceRowLevelSecurityForTableOwner(boolean forceRowLevelSecurityForTableOwner) {
        this.forceRowLevelSecurityForTableOwner = forceRowLevelSecurityForTableOwner;
    }

    public String getGrantee() {
        return grantee;
    }

    public void setGrantee(String grantee) {
        this.grantee = grantee;
    }

    public Map<TableKey, AbstractTableRLSPolicyProperties> getTableRLSPolicies() {
        return tableRLSPolicies;
    }

    public Map<SameTenantConstraintForForeignKey, AbstractSameTenantConstraintForForeignKeyProperties> getSameTenantConstraintForForeignKeyProperties() {
        return sameTenantConstraintForForeignKeyProperties;
    }

    public Map<TableKey, String> getFunctionThatChecksIfRecordExistsInTableNames() {
        return functionThatChecksIfRecordExistsInTableNames;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
