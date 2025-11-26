package com.commandlinecommandos.campusmarketplace.converter;

import com.commandlinecommandos.campusmarketplace.model.ModerationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for PostgreSQL ENUM type moderation_status
 * Handles conversion between Java enum and PostgreSQL ENUM
 */
@Converter(autoApply = true)
public class ModerationStatusConverter implements AttributeConverter<ModerationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ModerationStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ModerationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return ModerationStatus.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown moderation status: " + dbData);
        }
    }
}