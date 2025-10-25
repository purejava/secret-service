package org.purejava.secret.api;

import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.Properties;
import org.purejava.secret.api.errors.DBusCallException;
import org.purejava.secret.api.errors.SecretIsLockedException;
import org.purejava.secret.api.errors.SecretNoSessionException;
import org.purejava.secret.api.errors.SecretNoSuchObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Secret Service DBus objects such as Service, Session, Collection, Item and Prompt.
 * It centralizes logging.
 *
 * @param <T> the D-Bus interface type implemented by the remote object.
 */
public abstract class DBusMessageHandler<T extends DBusInterface> {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected final String busName;
    protected final String dbusPath;
    protected final Class<T> interfaceClass;

    protected T remote;
    protected Properties properties;

    protected DBusMessageHandler(String busName, String dbusPath, Class<T> iface) {
        if (null == dbusPath) {
            throw new IllegalArgumentException("DBusPath must not be null");
        }
        this.busName = busName;
        this.dbusPath = dbusPath;
        this.interfaceClass = iface;
    }

    protected boolean isUsable() {
        return remote != null;
    }

    protected abstract String getUnavailableMessage();

    public record DBusResult<T>(T value, DBusCallException error) {
        public boolean isSuccess() {
            return error == null;
        }
    }

    protected <R> DBusResult<R> dBusCall(String operation,
                                         String operator,
                                         DBusOperation<R> action) {

        if (!isUsable()) {
            DBusCallException ex = new DBusCallException(getUnavailableMessage(), null);
            return new DBusResult<>(null, ex);
        }

        try {

            return new DBusResult<>(action.call(), null);

        } catch (Exception e) {

            LOG.warn("DBus error on calling {} for {}: {}", operation, operator, e.getMessage());
            DBusCallException mapped = mapDBusError(operation, operator, e);
            return new DBusResult<>(null, mapped);

        }
    }

    private DBusCallException mapDBusError(String operation, String operator, Exception e) {
        String msg = e.getMessage();

        if (msg == null) {
            return new DBusCallException("Unknown DBus error", e);
        }

        if (msg.contains("org.freedesktop.Secret.Error.IsLocked")) {
            return new SecretIsLockedException(operation, operator, e);
        }
        if (msg.contains("org.freedesktop.Secret.Error.NoSession")) {
            return new SecretNoSessionException(operation, operator, e);
        }
        if (msg.contains("org.freedesktop.Secret.Error.NoSuchObject")) {
            return new SecretNoSuchObjectException(operation, operator, e);
        }

        return new DBusCallException(
                "DBus error on calling " + operation + " for " + operator + ": " + msg, e
        );
    }

    @FunctionalInterface
    protected interface DBusOperation<R> {
        R call() throws Exception;
    }
}

