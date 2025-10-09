package org.purejava.secret;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.purejava.secret.freedesktop.dbus.handlers.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Session extends Messaging implements org.purejava.secret.interfaces.Session {

    private static final Logger LOG = LoggerFactory.getLogger(Session.class);
    private static final DBusConnection connection;

    private org.purejava.secret.interfaces.Session session = null;

    static {
        connection = ConnectionManager.getConnection();
    }

    public Session() {
        super(connection, Static.Service.SECRETS, Static.DBusPath.SESSION, Static.Interfaces.SESSION);
        if (null != connection) {
            try {
                this.session = connection.getRemoteObject(Static.Service.SECRETS, Static.DBusPath.SESSION, org.purejava.secret.interfaces.Session.class);
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
