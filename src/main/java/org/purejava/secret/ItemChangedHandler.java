package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;

@FunctionalInterface
public interface ItemChangedHandler {
    void onItemChanged(DBusPath item);
}
