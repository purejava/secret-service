package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Item {
    private static final Logger LOG = LoggerFactory.getLogger(Item.class);
    private static final String ITEM_NOT_AVAILABLE = "Item not available on DBus";
    private static final DBusConnection connection;

    private static final String LABEL = "org.freedesktop.Secret.Item.Label";
    private static final String ATTRIBUTES = "org.freedesktop.Secret.Item.Attributes";

    private final DBusPath path;
    private org.purejava.secret.interfaces.Item item = null;
    private Properties properties = null;

    static {
        connection = ConnectionManager.getInstance().getConnection();
    }

    public Item(DBusPath path) {
        if (null == path) {
            throw new IllegalArgumentException("DBusPath must not be null");
        }

        this.path = path;

        try {

            this.item = Item.connection.getRemoteObject(Static.Service.SECRETS,
                    path.getPath(),
                    org.purejava.secret.interfaces.Item.class);

            this.properties = Item.connection.getRemoteObject(Static.Service.SECRETS,
                    path.getPath(),
                    Properties.class);

        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    public static Map<String, Variant<?>> createProperties(String label, Map<String, String> attributes) {
        Map<String, Variant<?>> properties = new HashMap<>();
        properties.put(LABEL, new Variant<>(label));
        if (attributes != null) {
            properties.put(ATTRIBUTES, new Variant<>(attributes, "a{ss}"));
        }
        return properties;
    }

    private boolean isUsable() {
        return null != item;
    }

    /**
     * Delete this item.
     *
     * @return Prompt   &mdash; A prompt dbuspath, or the special value '/' if no prompt is necessary.
     */
    public DBusPath delete() {
        if (isUsable()) {
            return item.Delete();
        }
        LOG.error(ITEM_NOT_AVAILABLE);
        return null;
    }

    /**
     * Retrieve the secret for this item.
     *
     * @param session The session to use to encode the secret.
     * @return secret   &mdash; The secret retrieved.
     */
    public Secret getSecret(DBusPath session) {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        if (null == session) {
            LOG.error("Cannot getSecret as required session is missing");
            return null;
        }
        return item.GetSecret(session);
    }

    /**
     * Set the secret for this item.
     *
     * @param secret The secret to set, encoded for the included session.
     */
    public void setSecret(Secret secret) {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return;
        }
        if (null == secret) {
            LOG.error("Cannot setSecret as required secret is missing");
            return;
        }
        item.SetSecret(secret);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Whether the item is locked and requires authentication, or not.
     */
    public boolean isLocked() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return true;
        }
        return properties.Get(Static.Interfaces.ITEM, "Locked");
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The attributes of the item.
     */
    public Map<String, String> getAttributes() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        return properties.Get(Static.Interfaces.ITEM, "Attributes");
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The displayable label of this collection.
     */
    public String getLabel() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        return properties.Get(Static.Interfaces.ITEM, "Label");
    }

    /**
     * Read-only property "Created"
     *
     * @return The unix time when the item was created.
     */
    public Long getCreated() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        return properties.Get(Static.Interfaces.ITEM, "Created");
    }

    /**
     * Read-only property "Modified"
     *
     * @return The unix time when the item was last modified.
     */
    public Long getModified() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        return properties.Get(Static.Interfaces.ITEM, "Modified");
    }

    public String getDBusPath() {
        return path.getPath();
    }
}
