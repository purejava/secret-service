package org.purejava.secret.api.errors;

public class DBusCallException extends RuntimeException {
    public DBusCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
