package com.commandlinecommandos.campusmarketplace.converter;

import com.commandlinecommandos.campusmarketplace.model.ProductCondition;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for PostgreSQL ENUM type product_condition
 * Handles conversion between Java enum and PostgreSQL ENUM
 */
@Converter(autoApply = true)
public class ProductConditionConverter implements AttributeConverter<ProductCondition, String> {

    @Override
    public String convertToDatabaseColumn(ProductCondition attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ProductCondition convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return ProductCondition.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown product condition: " + dbData);
        }
    }
}
