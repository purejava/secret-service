package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.types.Variant;

import java.util.HashMap;
import java.util.Map;

public class Item extends org.purejava.secret.impl.Item {
    static String LABEL = "org.freedesktop.Secret.Item.Label";
    static String ATTRIBUTES = "org.freedesktop.Secret.Item.Attributes";

    public Item(String collection, String item_id) {
        super(collection, item_id);
    }

    public static Map<String, Variant<?>> createProperties(String label, Map<String, String> attributes) {
        Map<String, Variant<?>> properties = new HashMap<>();
        properties.put(LABEL, new Variant<>(label));
        if (attributes != null) {
            properties.put(ATTRIBUTES, new Variant<>(attributes, "a{ss}"));
        }
        return properties;
    }

    /**
     * Delete this item.
     *
     * @return Prompt   &mdash; A prompt dbuspath, or the special value '/' if no prompt is necessary.
     */
    public DBusPath delete() {
        return item.Delete();
    }

    /**
     * Retrieve the secret for this item.
     *
     * @param session The session to use to encode the secret.
     * @return secret   &mdash; The secret retrieved.
     */
    public Secret getSecret(DBusPath session) {
        return item.GetSecret(session);
    }

    /**
     * Set the secret for this item.
     *
     * @param secret The secret to set, encoded for the included session.
     */
    public void setSecret(Secret secret) {
        item.SetSecret(secret);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Whether the item is locked and requires authentication, or not.
     */
    public boolean isLocked() {
        return Locked();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The attributes of the item.
     */
    public Map<String, String> getAttributes() {
        return Attributes();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The displayable label of this collection.
     */
    public String getLabel() {
        return Label();
    }

    /**
     * Read-only property "Created"
     *
     * @return The unix time when the item was created.
     */
    public Long getCreated() {
        var c = Created();
        return null == c ? null : c.longValue();
    }

    /**
     * Read-only property "Modified"
     *
     * @return The unix time when the item was last modified.
     */
    public Long getModified() {
        var m = Modified();
        return null == m ? null : m.longValue();
    }
}
