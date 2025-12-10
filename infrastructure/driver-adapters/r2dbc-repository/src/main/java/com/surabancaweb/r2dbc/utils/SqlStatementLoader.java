

package com.surabancaweb.r2dbc.utils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * BeanPostProcessor que carga el contenido de archivos SQL (classpath:sql/{namespace}/{value}.sql)
 * y lo inyecta en campos anotados con @SqlStatement.
 */
@Component
public class SqlStatementLoader implements BeanPostProcessor {

    private final ResourceLoader resourceLoader;

    public SqlStatementLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            SqlStatement annotation = field.getAnnotation(SqlStatement.class);
            if (annotation != null) {
                String path = String.format("classpath:sql/%s/%s.sql", annotation.namespace(), annotation.value());
                Resource resource = resourceLoader.getResource(path);
                if (!resource.exists()) {
                    throw new IllegalStateException("SQL resource no existe: " + path + " en bean " + clazz.getName() + "." + field.getName());
                }
                try {
                    String sql = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    if (sql == null || sql.trim().isEmpty()) {
                        throw new IllegalStateException("SQL vacío o nulo: " + path + " en bean " + clazz.getName() + "." + field.getName());
                    }
                    boolean isStatic = java.lang.reflect.Modifier.isStatic(field.getModifiers());
                    field.setAccessible(true);
                    if (isStatic) {
                        field.set(null, sql);
                    } else {
                        field.set(bean, sql);
                    }
                } catch (IOException | IllegalAccessException e) {
                    throw new RuntimeException("No se pudo cargar el SQL para " + clazz.getName() + "." + field.getName() + ", ruta: " + path, e);
                }
            }
        }
        return bean;
    }
}
