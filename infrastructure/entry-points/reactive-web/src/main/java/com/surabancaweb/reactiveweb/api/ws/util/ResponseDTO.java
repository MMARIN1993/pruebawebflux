package com.surabancaweb.reactiveweb.api.ws.util;

public record ResponseDTO<T> (
        int status,
        String message,
        T data,
        int size) {
}
