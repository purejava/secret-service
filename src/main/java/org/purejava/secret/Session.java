package org.purejava.secret;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBus;
import org.purejava.secret.freedesktop.dbus.handlers.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Session extends Messaging implements org.purejava.secret.interfaces.Session {

    private static final Logger LOG = LoggerFactory.getLogger(Session.class);
    private static final String DBUS_PATH = "/org/freedesktop/secrets/session";
    private static DBusConnection connection;

    private org.purejava.secret.interfaces.Session session = null;

    static {
        try {
            connection = DBusConnectionBuilder.forSessionBus().withShared(false).build();
            connection.getRemoteObject("org.freedesktop.DBus",
                    "/org/freedesktop/DBus", DBus.class);
        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    public Session() {
        super(connection, Static.Service.SECRETS, DBUS_PATH, Static.Service.SECRETS);
        if (null != connection) {
            try {
                this.session = connection.getRemoteObject(Static.Service.SECRETS, DBUS_PATH, org.purejava.secret.interfaces.Session.class);
            } catch (DBusException e) {
                LOG.error(e.toString(), e.getCause());
            }
        } else {
            LOG.error("Dbus not available");
        }
    }

    private boolean isUsable() {
        return null != session;
    }

    /**
     * Close this session.
     */
    @Override
    public void close() {
        if (isUsable()) {
            session.close();
        }
        LOG.error("Session not available on DBus");
    }

    /**
     * @return The DBusPath of the session.
     */
    @Override
    public String getObjectPath() {
        return super.getDBusPath();
    }
}
