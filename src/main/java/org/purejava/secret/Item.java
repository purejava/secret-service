package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBus;
import org.freedesktop.dbus.types.UInt64;
import org.purejava.secret.freedesktop.dbus.handlers.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.purejava.secret.Static.DBusPath.COLLECTION;

public class Item extends Messaging implements org.purejava.secret.interfaces.Item {

    private static final Logger LOG = LoggerFactory.getLogger(Item.class);
    private static final String ITEM_NOT_AVAILABLE = "Item not available on DBus";
    private static DBusConnection connection;

    private final String collection;
    private final String item_id;
    private org.purejava.secret.interfaces.Item item = null;

    static {
        try {
            connection = DBusConnectionBuilder.forSessionBus().withShared(false).build();
            connection.getRemoteObject("org.freedesktop.DBus",
                    "/org/freedesktop/DBus", DBus.class);
        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    public Item(String collection, String item_id) {
        super(connection, Static.Service.SECRETS, COLLECTION + "/" + collection + "/" + item_id, Static.Service.SECRETS);
        if (null != connection) {
            try {
                this.item = connection.getRemoteObject(Static.Service.SECRETS, COLLECTION + "/" + collection + "/" + item_id, org.purejava.secret.interfaces.Item.class);
            } catch (DBusException e) {
                LOG.error(e.toString(), e.getCause());
            }
        } else {
            LOG.error("Dbus not available");
        }
        this.collection = collection;
        this.item_id = item_id;
    }

    private boolean isUsable() {
        return null != item;
    }

    /**
     * Delete this item.
     *
     * @return Prompt   &mdash; A prompt dbuspath, or the special value '/' if no prompt is necessary.
     */
    @Override
    public DBusPath delete() {
        if (isUsable()) {
            return item.delete();
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
    @Override
    public Secret getSecret(DBusPath session) {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        if (null == session) {
            LOG.error("Cannot getSecret as required session is missing");
            return null;
        }
        return item.getSecret(session);
    }

    /**
     * Set the secret for this item.
     *
     * @param secret The secret to set, encoded for the included session.
     */
    @Override
    public void setSecret(Secret secret) {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return;
        }
        if (null == secret) {
            LOG.error("Cannot setSecret as required secret is missing");
            return;
        }
        item.setSecret(secret);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Whether the item is locked and requires authentication, or not.
     */
    @Override
    public boolean locked() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return true;
        }
        var response = getProperty("Locked");
        return null == response || (boolean) response.getValue();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The attributes of the item.
     */
    @Override
    public Map<String, String> attributes() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Attributes");
        return null == response ? null : (Map<String, String>) response.getValue();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The displayable label of this collection.
     */
    @Override
    public String label() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Label");
        return null == response ? null : (String) response.getValue();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The unix time when the item was created.
     */
    @Override
    public UInt64 created() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Created");
        return null == response ? null : (UInt64) response.getValue();
    }

    /**
     * Read-only property "Created"
     *
     * @return The unix time when the item was created.
     */
    public Long getCreated() {
        var c = created();
        return null == c ? null : c.longValue();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The unix time when the item was last modified.
     */
    @Override
    public UInt64 modified() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Modified");
        return null == response ? null : (UInt64) response.getValue();
    }

    /**
     * Read-only property "Modified"
     *
     * @return The unix time when the item was last modified.
     */
    public Long getModified() {
        var m = modified();
        return null == m ? null : m.longValue();
    }

    /**
     * @return The DBusPath of the item.
     */
    @Override
    public String getObjectPath() {
        return super.getDBusPath();
    }

    // Getter
    public String getItem_id() {
        return item_id;
    }

    public String getCollection() {
        return collection;
    }
}
