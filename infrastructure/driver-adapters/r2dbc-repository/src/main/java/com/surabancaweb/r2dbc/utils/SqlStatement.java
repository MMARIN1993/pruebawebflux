package com.surabancaweb.r2dbc.utils;

import java.lang.annotation.*;

/**
 * Anotación para inyectar sentencias SQL desde resources en campos estáticos.
 * Ruta esperada: classpath:sql/{namespace}/{value}.sql
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface SqlStatement {
    String namespace();
    String value();
}
