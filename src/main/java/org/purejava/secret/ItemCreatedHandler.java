package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;

@FunctionalInterface
public interface ItemCreatedHandler {
    void onItemCreated(DBusPath item);
}
