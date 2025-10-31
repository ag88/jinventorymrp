package com.inventorymrp.util;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Converter for java.time.LocalDateTime to work with sql2o.
 * Converts between java.sql.Timestamp and java.time.LocalDateTime.
 */
public class LocalDateTimeConverter implements Converter<LocalDateTime> {
    
    @Override
    public LocalDateTime convert(Object val) throws ConverterException {
        if (val == null) {
            return null;
        }
        if (val instanceof Timestamp) {
            return ((Timestamp) val).toLocalDateTime();
        }
        throw new ConverterException("Cannot convert " + val.getClass().getName() + " to LocalDateTime");
    }
    
    @Override
    public Object toDatabaseParam(LocalDateTime val) {
        return val == null ? null : Timestamp.valueOf(val);
    }
}
