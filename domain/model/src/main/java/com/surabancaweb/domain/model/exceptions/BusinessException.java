package com.surabancaweb.domain.model.exceptions;

public class BusinessException extends RuntimeException {

    public enum Type {
        // Validaciones
        FECHA_MAYOR("La fecha de inicio debe ser mayor o igual a la fecha actual"),
        VALOR_MAYOR("El valor debe ser mayor a 0"),
        VALIDATION("Error de validación"),        // Negocio específicos
        POLIZA_NO_EXISTE_POLICYID("La Poliza con polizaId proporcionado no existe"),
        POLIZAS_NO_REGISTRADAS("No existen polizas registradas"),
        // Negocio generales
        NOT_FOUND("Recurso no encontrado"),
        CONFLICT("Conflicto de negocio"),
        // General
        INTERNAL("Error interno");

        private final String defaultMessage;

        Type(String defaultMessage) {
            this.defaultMessage = defaultMessage;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    private final Type type;

    public BusinessException(String message) {
        super(message);
        this.type = Type.INTERNAL;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.type = Type.INTERNAL;
    }

    public BusinessException(Type type) {
        super(type.getDefaultMessage());
        this.type = type;
    }

    public BusinessException(Type type, String message) {
        super(message != null && !message.isBlank() ? message : type.getDefaultMessage());
        this.type = type;
    }

    public BusinessException(Type type, String message, Throwable cause) {
        super(message != null && !message.isBlank() ? message : type.getDefaultMessage(), cause);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
