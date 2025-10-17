package org.purejava.secret.api;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Session {

    private static final Logger LOG = LoggerFactory.getLogger(Session.class);
    private static final DBusConnection connection;

    private org.purejava.secret.interfaces.Session session = null;

    static {
        connection = ConnectionManager.getInstance().getConnection();
    }

    public Session() {
        try {
            this.session = Session.connection.getRemoteObject(Static.Service.SECRETS,
                    Static.Interfaces.SESSION,
                    org.purejava.secret.interfaces.Session.class);

        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    private boolean isUsable() {
        return null != session;
    }

    /**
     * Close this session.
     */
    public void close() {
        if (isUsable()) {
            session.Close();
        }
        LOG.error("Session not available on DBus");
    }

}
