package org.example.authservice.exception;

public class AuthenticateException extends Exception {
    public AuthenticateException(String errorMessage){
        super(errorMessage);
    }
}
