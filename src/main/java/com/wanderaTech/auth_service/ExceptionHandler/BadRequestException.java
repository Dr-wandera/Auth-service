package com.wanderaTech.auth_service.ExceptionHandler;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String message) {
        super(message);

    }
}
