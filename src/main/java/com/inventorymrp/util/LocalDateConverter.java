package com.inventorymrp.util;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Converter for java.time.LocalDateTime to work with sql2o.
 * Converts between java.sql.Timestamp and java.time.LocalDateTime.
 */
public class LocalDateConverter implements Converter<LocalDate> {
    
    @Override
    public LocalDate convert(Object val) throws ConverterException {
        if (val == null) {
            return null;
        }
        if (val instanceof Date) {
            return ((Date) val).toLocalDate();
        }
        throw new ConverterException("Cannot convert " + val.getClass().getName() + " to LocalDateTime");
    }
    
    @Override
    public Object toDatabaseParam(LocalDate val) {
        return val == null ? null : Date.valueOf(val);
    }
}
