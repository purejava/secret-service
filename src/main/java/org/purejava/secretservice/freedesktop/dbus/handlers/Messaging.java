package org.purejava.secretservice.freedesktop.dbus.handlers;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class Messaging {

    private static final Logger LOG = LoggerFactory.getLogger(Messaging.class);
    private final DBusConnection connection;
    private final MessageHandler msg;
    private final String serviceName;
    private final String dbusPath;
    private final String interfaceName;

    public Messaging(DBusConnection connection,
                     String serviceName, String dbusPath, String interfaceName) {
        this.connection = connection;
        this.msg = new MessageHandler(connection);
        this.serviceName = serviceName;
        this.dbusPath = dbusPath;
        this.interfaceName = interfaceName;
    }

    public Object[] send(String method) {
        return msg.send(serviceName, dbusPath, interfaceName, method, "");
    }

    public Object[] send(String method, String signature, Object... arguments) {
        return msg.send(serviceName, dbusPath, interfaceName, method, signature, arguments);
    }

    protected Variant getProperty(String property) {
        return msg.getProperty(serviceName, dbusPath, interfaceName, property);
    }

    protected Variant getAllProperties() {
        return msg.getAllProperties(serviceName, dbusPath, interfaceName);
    }

    protected void setProperty(String property, Variant value) {
        msg.setProperty(serviceName, dbusPath, interfaceName, property, value);
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDBusPath() {
        return dbusPath;
    }

    public DBusPath getPath() {
        return new DBusPath("", dbusPath);
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public MessageHandler getMessageHandler() {
        return msg;
    }

    public DBusConnection getConnection() {
        return connection;
    }

}
