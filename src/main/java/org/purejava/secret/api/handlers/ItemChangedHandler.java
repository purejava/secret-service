package org.purejava.secret.api.handlers;

import org.freedesktop.dbus.DBusPath;

@FunctionalInterface
public interface ItemChangedHandler {
    void onItemChanged(DBusPath item);
}
