package com.commandlinecommandos.campusmarketplace.config;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

/**
 * Custom H2 Dialect that maps PostgreSQL NAMED_ENUM to VARCHAR for testing
 * This allows tests to run with H2 while production uses PostgreSQL ENUM types
 */
public class TestH2Dialect extends H2Dialect {

    public TestH2Dialect() {
        super();
    }

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);

        // Register NAMED_ENUM as VARCHAR for H2 compatibility
        typeContributions.contributeJdbcType(VarcharJdbcType.INSTANCE);
        typeContributions.getTypeConfiguration()
            .getJdbcTypeRegistry()
            .addDescriptor(SqlTypes.NAMED_ENUM, VarcharJdbcType.INSTANCE);
    }
}
