package org.purejava.secret.api.errors;

public class SecretNoSessionException extends DBusCallException {
    public SecretNoSessionException(String operation, String operator, Throwable cause) {
        super("NoSession on " + operator + " during " + operation, cause);
    }
}
