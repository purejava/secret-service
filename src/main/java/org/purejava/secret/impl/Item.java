package org.purejava.secret.impl;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.UInt64;
import org.purejava.secret.api.ConnectionManager;
import org.purejava.secret.api.Secret;
import org.purejava.secret.api.Static;
import org.purejava.secret.freedesktop.dbus.handlers.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.purejava.secret.api.Static.DBusPath.COLLECTION;

public abstract class Item extends Messaging implements org.purejava.secret.interfaces.Item {

    private static final Logger LOG = LoggerFactory.getLogger(Item.class);
    private static final String ITEM_NOT_AVAILABLE = "Item not available on DBus";
    private static final DBusConnection connection;

    private final String collection;
    private final String item_id;
    protected org.purejava.secret.interfaces.Item item = null;

    static {
        connection = ConnectionManager.getInstance().getConnection();
    }

    public Item(String collection, String item_id) {
        super(connection, Static.Service.SECRETS, COLLECTION + "/" + collection + "/" + item_id, Static.Interfaces.ITEM);
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
    public DBusPath Delete() {
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
    @Override
    public Secret GetSecret(DBusPath session) {
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
    @Override
    public void SetSecret(Secret secret) {
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
    @Override
    public boolean Locked() {
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
    public Map<String, String> Attributes() {
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
    public String Label() {
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
    public UInt64 Created() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Created");
        return null == response ? null : (UInt64) response.getValue();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The unix time when the item was last modified.
     */
    @Override
    public UInt64 Modified() {
        if (!isUsable()) {
            LOG.error(ITEM_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Modified");
        return null == response ? null : (UInt64) response.getValue();
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
