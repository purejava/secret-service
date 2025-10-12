package org.purejava.secret.impl;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;
import org.purejava.secret.api.ConnectionManager;
import org.purejava.secret.api.Pair;
import org.purejava.secret.api.Secret;
import org.purejava.secret.api.Static;
import org.purejava.secret.api.handlers.ItemChangedHandler;
import org.purejava.secret.api.handlers.ItemCreatedHandler;
import org.purejava.secret.api.handlers.ItemDeletedHandler;
import org.purejava.secret.freedesktop.dbus.handlers.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Collection extends Messaging implements org.purejava.secret.interfaces.Collection {

    private static final Logger LOG = LoggerFactory.getLogger(Collection.class);
    private static final String COLLECTION_NOT_AVAILABLE = "Collection not available on DBus";
    private static final DBusConnection connection;

    private final List<ItemCreatedHandler> itemCreatedHandlers = new CopyOnWriteArrayList<>();
    private final List<ItemChangedHandler> itemChangedHandlers = new CopyOnWriteArrayList<>();
    private final List<ItemDeletedHandler> itemDeletedHandlers = new CopyOnWriteArrayList<>();
    protected org.purejava.secret.interfaces.Collection collection = null;

    static {
        connection = ConnectionManager.getConnection();
    }

    public Collection() {
        super(connection, Static.Service.SECRETS, Static.DBusPath.DEFAULT_COLLECTION, Static.Interfaces.COLLECTION);
        if (null != connection) {
            try {
                this.collection = connection.getRemoteObject(Static.Service.SECRETS, Static.DBusPath.DEFAULT_COLLECTION, org.purejava.secret.interfaces.Collection.class);
                registerSignals();
            } catch (DBusException e) {
                LOG.error(e.toString(), e.getCause());
            }
        } else {
            LOG.error("Dbus not available");
        }
    }

    public Collection(DBusPath path) {
        super(connection, Static.Service.SECRETS, path.getPath(), Static.Service.SECRETS);
        if (null != connection) {
            try {
                this.collection = connection.getRemoteObject(Static.Service.SECRETS, path, org.purejava.secret.interfaces.Collection.class);
                registerSignals();
            } catch (DBusException e) {
                LOG.error(e.toString(), e.getCause());
            }
        } else {
            LOG.error("Dbus not available");
        }
    }

    private void registerSignals() throws DBusException {
        connection.addSigHandler(org.purejava.secret.interfaces.Collection.ItemCreated.class, this::notifyOnItemCreated);
        connection.addSigHandler(org.purejava.secret.interfaces.Collection.ItemChanged.class, this::notifyOnItemChanged);
        connection.addSigHandler(org.purejava.secret.interfaces.Collection.ItemDeleted.class, this::notifyOnItemDeleted);
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
    public DBusPath Delete() {
        if (isUsable()) {
            return collection.Delete();
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
    public List<DBusPath> SearchItems(Map<String, String> attributes) {
        if (isUsable()) {
            return collection.SearchItems(attributes);
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
    public Pair<DBusPath, DBusPath> CreateItem(Map<String, Variant<?>> properties, Secret secret, boolean replace) {
        if (!isUsable()) {
            LOG.error(COLLECTION_NOT_AVAILABLE);
            return null;
        }
        if (null == secret) {
            LOG.error("Cannot createItem as required secret is missing");
            return null;
        }
        return collection.CreateItem(properties, secret, replace);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Items in this collection.
     */
    @Override
    public List<DBusPath> Items() {
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
    public String Label() {
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
    public boolean Locked() {
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
    public UInt64 Created() {
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
    public UInt64 Modified() {
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

    private void notifyOnItemCreated(ItemCreated signal) {
        if (getObjectPath().equals(signal.item.getPath())) {
            for (ItemCreatedHandler handler : itemCreatedHandlers) {
                handler.onItemCreated(signal.item);
            }
        }
    }
    private void notifyOnItemChanged(ItemChanged signal) {
        if (getObjectPath().equals(signal.item.getPath())) {
            for (ItemChangedHandler handler : itemChangedHandlers) {
                handler.onItemChanged(signal.item);
            }
        }
    }
    private void notifyOnItemDeleted(ItemDeleted signal) {
        if (getObjectPath().equals(signal.item.getPath())) {
            for (ItemDeletedHandler handler : itemDeletedHandlers) {
                handler.onItemDeleted(signal.item);
            }
        }
    }

    public void addItemCreatedHandler(ItemCreatedHandler handler) {
        itemCreatedHandlers.add(handler);
    }

    public void removeItemCreatedHandler(ItemCreatedHandler handler) {
        itemCreatedHandlers.remove(handler);
    }

    public void addItemChangedHandler(ItemChangedHandler handler) {
        itemChangedHandlers.add(handler);
    }

    public void removeItemChangedHandler(ItemChangedHandler handler) {
        itemChangedHandlers.remove(handler);
    }

    public void addItemDeletedHandler(ItemDeletedHandler handler) {
        itemDeletedHandlers.add(handler);
    }

    public void removeItemDeletedHandler(ItemDeletedHandler handler) {
        itemDeletedHandlers.remove(handler);
    }
}
