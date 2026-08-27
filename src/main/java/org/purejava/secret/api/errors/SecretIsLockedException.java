package org.purejava.secret.api.errors;

public class SecretIsLockedException extends DBusCallException {
    public SecretIsLockedException(String operation, String operator, Throwable cause) {
        super("DBus error on calling " + operation + " as item " + operator + " is locked", cause);
    }
}
