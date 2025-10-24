package org.purejava.secret.api.errors;

public class SecretNoSuchObjectException extends DBusCallException {
    public SecretNoSuchObjectException(String operation, String operator, Throwable cause) {
        super("NoSuchObject on " + operator + " during " + operation, cause);
    }
}
