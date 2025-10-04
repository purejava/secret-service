package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;

@FunctionalInterface
public interface CollectionDeletedHandler {
    void onCollectionDeleted(DBusPath collection);
}
