package org.purejava.secret.api.errors;

public class SecretNoSuchObjectException extends DBusCallException {
    public SecretNoSuchObjectException(String operation, String operator, Throwable cause) {
        super("DBus error on calling " + operation + " as item or collection " + operator + " does not exist", cause);
    }
}
