package org.purejava.secret.interfaces;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt64;
import org.purejava.secret.api.Secret;

import java.util.Map;

@DBusInterfaceName("org.freedesktop.Secret.Item")
public interface Item extends DBusInterface {

    /**
     * Delete this item.
     *
     * @return Prompt   &mdash; A prompt dbuspath, or the special value '/' if no prompt is necessary.
     */
    DBusPath Delete();

    /**
     * Retrieve the secret for this item.
     *
     * @param session   The session to use to encode the secret.
     * @return secret   &mdash; The secret retrieved.
     */
    Secret GetSecret(DBusPath session);

    /**
     * Set the secret for this item.
     *
     * @param secret The secret to set, encoded for the included session.
     */
    void SetSecret(Secret secret);

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     * @return Whether the item is locked and requires authentication, or not.
     */
    boolean Locked();

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     * @return The attributes of the item.
     */
    Map<String, String> Attributes();

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     * @return The displayable label of this collection.
     */
    String Label();

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     * @return The unix time when the item was created.
     */
    UInt64 Created();

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     * @return The unix time when the item was last modified.
     */
    UInt64 Modified();

}
