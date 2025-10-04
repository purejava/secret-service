package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;

@FunctionalInterface
public interface CollectionChangedHandler {
    void onCollectionChanged(DBusPath collection);
}
