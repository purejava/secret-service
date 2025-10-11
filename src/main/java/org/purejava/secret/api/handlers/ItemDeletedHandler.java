package org.purejava.secret.api.handlers;

import org.freedesktop.dbus.DBusPath;

@FunctionalInterface
public interface ItemDeletedHandler {
    void onItemDeleted(DBusPath item);
}
