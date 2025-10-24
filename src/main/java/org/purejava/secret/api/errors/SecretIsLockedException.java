package org.purejava.secret.api.errors;

public class SecretIsLockedException extends DBusCallException {
    public SecretIsLockedException(String operation, String operator, Throwable cause) {
        super("IsLocked on " + operator + " during " + operation, cause);
    }
}
