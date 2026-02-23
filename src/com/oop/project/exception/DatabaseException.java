package com.oop.project.exception;

/**
 * Custom exception wrapping SQLExceptions to avoid leaking database details.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}