package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;

@FunctionalInterface
public interface ItemDeletedHandler {
    void onItemDeleted(DBusPath item);
}
