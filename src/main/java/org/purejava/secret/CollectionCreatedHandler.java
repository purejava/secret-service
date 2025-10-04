package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;

@FunctionalInterface
public interface CollectionCreatedHandler {
    void onCollectionCreated(DBusPath collection);
}
