package org.purejava.secret.interfaces;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("org.freedesktop.Secret.Session")
public interface Session extends DBusInterface {

    /**
     * Close this session.
     */
    void close();

}
