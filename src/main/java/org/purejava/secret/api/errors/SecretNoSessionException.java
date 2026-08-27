package org.purejava.secret.api.errors;

public class SecretNoSessionException extends DBusCallException {
    public SecretNoSessionException(String operation, String operator, Throwable cause) {
        super("DBus error on calling " + operation + " for " + operator + " as the session does not exist", cause);
    }
}
