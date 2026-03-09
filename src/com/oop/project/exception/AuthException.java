package com.oop.project.exception;

/**
 * Custom exception for authentication errors.
 */
public class AuthException extends Exception {
    public AuthException(String message) {
        super(message);
    }
}