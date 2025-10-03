package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBus;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;
import org.purejava.secret.freedesktop.dbus.handlers.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Collection extends Messaging implements org.purejava.secret.interfaces.Collection {

    private static final Logger LOG = LoggerFactory.getLogger(Collection.class);
    private static final String BUS_NAME = "org.freedesktop.secrets";
    private static final String DEFAULT_COLLECTION = "/org/freedesktop/secrets/aliases/default";
    private static final String COLLECTION_NOT_AVAILABLE = "Collection not available on DBus";
    private static DBusConnection connection;

    private org.purejava.secret.interfaces.Collection collection = null;

    static {
        try {
            connection = DBusConnectionBuilder.forSessionBus().withShared(false).build();
            connection.getRemoteObject("org.freedesktop.DBus",
                    "/org/freedesktop/DBus", DBus.class);
        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    public Collection() {
        super(connection, BUS_NAME, DEFAULT_COLLECTION, BUS_NAME);
        if (null != connection) {
            try {
                this.collection = connection.getRemoteObject(BUS_NAME, DEFAULT_COLLECTION, org.purejava.secret.interfaces.Collection.class);
            } catch (DBusException e) {
                LOG.error(e.toString(), e.getCause());
            }
        } else {
            LOG.error("Dbus not available");
        }
    }

    public Collection(DBusPath path) {
        super(connection, BUS_NAME, path.getPath(), BUS_NAME);
        if (null != connection) {
            try {
                this.collection = connection.getRemoteObject(BUS_NAME, path, org.purejava.secret.interfaces.Collection.class);
            } catch (DBusException e) {
                LOG.error(e.toString(), e.getCause());
            }
        } else {
            LOG.error("Dbus not available");
        }
    }

    private boolean isUsable() {
        return null != collection;
    }

    /**
     * Delete this collection.
     *
     * @return prompt &mdash; A prompt to delete the collection, or the special value '/' when no prompt is necessary.
     * @see DBusPath
     */
    @Override
    public DBusPath delete() {
        if (isUsable()) {
            return collection.delete();
        }
        LOG.error(COLLECTION_NOT_AVAILABLE);
        return null;
    }

    /**
     * Search for items in this collection matching the lookup attributes.
     *
     * @param attributes Attributes to match.
     * @return results     &mdash; Items that matched the attributes.
     * @see DBusPath
     */
    @Override
    public List<DBusPath> searchItems(Map<String, String> attributes) {
        if (isUsable()) {
            return collection.searchItems(attributes);
        }
        LOG.error(COLLECTION_NOT_AVAILABLE);
        return null;
    }

    /**
     * Create an item with the given attributes, secret and label. If replace is set, then it replaces an item already
     * present with the same values for the attributes.
     *
     * @param properties The properties for the new item.
     *
     *                   <p>This allows setting the new item's properties upon its creation. All READWRITE properties
     *                   are usable. Specify the property names in full <code>interface.Property</code> form.</p>
     *
     *                   <p>
     *                   <b>Example 13.2. Example for properties of an item:</b><br>
     *                   <code>
     *                   properties = {<br>
     *                   &nbsp;&nbsp;"org.freedesktop.Secret.Item.Label": "MyItem",<br>
     *                   &nbsp;&nbsp;"org.freedesktop.Secret.Item.Attributes": {<br>
     *                   &nbsp;&nbsp;&nbsp;&nbsp;"Attribute1": "Value1",<br>
     *                   &nbsp;&nbsp;&nbsp;&nbsp;"Attribute2": "Value2"<br>
     *                   &nbsp;&nbsp;}<br>
     *                   }<br>
     *                   </code></p>
     *
     *                   <p>
     *                   <b>Note:</b>
     *                   Please note that there is a distinction between the terms <i>Property</i>, which refers
     *                   to D-Bus properties of an object, and <i>Attribute</i>, which refers to one of a
     *                   secret item's string-valued attributes.
     *                   </p>
     * @param secret     The secret to store in the item, encoded with the included session.
     * @param replace    Whether to replace an item with the same attributes or not.
     * @return Pair&lt;item, prompt&gt;<br>
     * <br>
     * item                 &mdash; The item created, or the special value '/' if a prompt is necessary.<br>
     * <br>
     * prompt               &mdash; A prompt object, or the special value '/' if no prompt is necessary.<br>
     * @see DBusPath
     */
    @Override
    public Pair<DBusPath, DBusPath> createItem(Map<String, Variant<?>> properties, Secret secret, boolean replace) {
        if (!isUsable()) {
            LOG.error(COLLECTION_NOT_AVAILABLE);
            return null;
        }
        if (null == secret) {
            LOG.error("Cannot createItem as required secret is missing");
            return null;
        }
        return collection.createItem(properties, secret, replace);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Items in this collection.
     */
    @Override
    public List<DBusPath> items() {
        if (!isUsable()) {
            LOG.error(COLLECTION_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Items");
        return null == response ? null : (ArrayList<DBusPath>) response.getValue();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The displayable label of this collection.
     */
    @Override
    public String label() {
        if (!isUsable()) {
            LOG.error(COLLECTION_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Label");
        return null == response ? null : (String) response.getValue();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Whether the collection is locked and must be authenticated by the client application.
     */
    @Override
    public boolean locked() {
        if (!isUsable()) {
            LOG.error(COLLECTION_NOT_AVAILABLE);
            return true;
        }
        var response = getProperty("Locked");
        return null == response || (boolean) response.getValue();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The unix time when the collection was created.
     */
    @Override
    public UInt64 created() {
        if (!isUsable()) {
            LOG.error(COLLECTION_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Created");
        return null == response ? null : (UInt64) response.getValue();
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The unix time when the collection was last modified.
     */
    @Override
    public UInt64 modified() {
        if (!isUsable()) {
            LOG.error(COLLECTION_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Modified");
        return null == response ? null : (UInt64) response.getValue();
    }

    /**
     * @return The DBusPath of the collection.
     */
    @Override
    public String getObjectPath() {
        return super.getDBusPath();
    }
}
