package org.purejava.secret.api;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);
    private static ConnectionManager instance;
    private DBusConnection connection;

    private ConnectionManager() {
        this.connection = createConnection();
    }

    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public DBusConnection getConnection() {
        return connection;
    }

    private static DBusConnection createConnection() {
        try {
            DBusConnection conn = DBusConnectionBuilder.forSessionBus().withShared(false).build();
            conn.getRemoteObject("org.freedesktop.DBus",
                    "/org/freedesktop/DBus", DBus.class);
            return conn;
        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
            return null;
        }
    }
}
