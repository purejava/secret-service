package org.purejava.secret.api.handlers;

import org.freedesktop.dbus.DBusPath;

@FunctionalInterface
public interface CollectionChangedHandler {
    void onCollectionChanged(DBusPath collection);
}
