package org.purejava.secret.api;

import java.util.Objects;

import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.Properties;
import org.purejava.secret.api.errors.DBusCallException;
import org.purejava.secret.api.errors.SecretIsLockedException;
import org.purejava.secret.api.errors.SecretNoSessionException;
import org.purejava.secret.api.errors.SecretNoSuchObjectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Secret Service DBus objects such as Service, Session,
 * Collection, Item and Prompt.
 * <br>
 * It centralizes logging and maps DBus errors to the corresponding
 * Secret Service exceptions.
 *
 * @param <T> the D-Bus interface type implemented by the remote object
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

    /**
     * Result of a DBus operation.
     *
     * @param <T> type of the successful result
     */
    public sealed interface DBusResult<T>
        permits DBusResult.Success, DBusResult.Failure {

        /**
         * Successful DBus operation.
         *
         * @param value returned value
         * @param <T> type of the returned value
         */
        record Success<T>(T value) implements DBusResult<T> {}

        /**
         * Failed DBus operation.
         *
         * @param error mapped DBus error
         * @param <T> type that would have been returned on success
         */
        record Failure<T>(DBusCallException error) implements DBusResult<T> {

            public Failure {
                Objects.requireNonNull(error, "error must not be null");
            }
        }
    }

    /**
     * Executes a DBus operation and returns either its value or a mapped error.
     *
     * @param operation name of the DBus operation
     * @param operator object or entity on which the operation is performed
     * @param action operation to execute
     * @param <R> type of the returned value
     * @return success or failure result
     */
    protected <R> DBusResult<R> dBusCall(
        String operation,
        String operator,
        DBusOperation<R> action) {

        if (!isUsable()) {
            var error = new DBusCallException(getUnavailableMessage(), null);
            return new DBusResult.Failure<>(error);
        }

        try {
            return new DBusResult.Success<>(action.call());

        } catch (Exception e) {
            LOG.warn("DBus error on calling {} for {}: {}", operation, operator, e.getMessage());
            return new DBusResult.Failure<>(mapDBusError(operation, operator, e));
        }
    }

    private DBusCallException mapDBusError(
        String operation,
        String operator,
        Exception exception) {

        String message = exception.getMessage();

        if (message == null) {
            return new DBusCallException("Unknown DBus error", exception);
        }

        if (message.contains("org.freedesktop.Secret.Error.IsLocked")) {
            return new SecretIsLockedException(operation, operator, exception);
        }
        if (message.contains("org.freedesktop.Secret.Error.NoSession")) {
            return new SecretNoSessionException(operation, operator, exception);
        }
        if (message.contains("org.freedesktop.Secret.Error.NoSuchObject")) {
            return new SecretNoSuchObjectException(operation, operator, exception);
        }

        return new DBusCallException("DBus error on calling " + operation + " for " + operator + ": " + message, exception);
    }

    @FunctionalInterface
    protected interface DBusOperation<R> {
        R call() throws Exception;
    }
}
