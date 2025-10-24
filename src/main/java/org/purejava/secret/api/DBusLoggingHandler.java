package org.purejava.secret.api;

import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Secret Service DBus objects such as Service, Session, Collection, Item and Prompt.
 * It centralizes logging.
 *
 * @param <T> the D-Bus interface type implemented by the remote object.
 */
public abstract class DBusLoggingHandler<T extends DBusInterface> {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected final String busName;
    protected final String dbusPath;
    protected final Class<T> interfaceClass;

    protected T remote;
    protected Properties properties;

    protected DBusLoggingHandler(String busName, String dbusPath, Class<T> iface) {
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

    protected <R> R dBusCall(String operation, String operator, DBusOperation<R> action) {
        if (!isUsable()) {
            LOG.error(getUnavailableMessage());
            return null;
        }
        try {
            return action.call();
        } catch (Exception e) {
            LOG.error("DBus error on calling {} for {}: {}", operation, operator, e.getMessage(), e);
            return null;
        }
    }

    @FunctionalInterface
    protected interface DBusOperation<R> {
        R call() throws Exception;
    }
}

