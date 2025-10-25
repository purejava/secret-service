package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;
import org.purejava.secret.api.handlers.ItemChangedHandler;
import org.purejava.secret.api.handlers.ItemCreatedHandler;
import org.purejava.secret.api.handlers.ItemDeletedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Collection extends DBusMessageHandler<org.purejava.secret.interfaces.Collection> {

    private static final Logger LOG = LoggerFactory.getLogger(Collection.class);
    private static final String LABEL = "org.freedesktop.Secret.Collection.Label";
    private static final String COLLECTION_NOT_AVAILABLE = "Collection not available on DBus";
    private static final DBusConnection connection;

    private final List<ItemCreatedHandler> itemCreatedHandlers = new CopyOnWriteArrayList<>();
    private final List<ItemChangedHandler> itemChangedHandlers = new CopyOnWriteArrayList<>();
    private final List<ItemDeletedHandler> itemDeletedHandlers = new CopyOnWriteArrayList<>();
    private final DBusPath path;

    static {
        connection = ConnectionManager.getInstance().getConnection();
    }

    public Collection(DBusPath path) {
        super(Static.Service.SECRETS, path.getPath(), org.purejava.secret.interfaces.Collection.class);

        this.path = path;

        try {

            this.remote = Collection.connection.getRemoteObject(Static.Service.SECRETS,
                    path.getPath(),
                    org.purejava.secret.interfaces.Collection.class);

            this.properties = Collection.connection.getRemoteObject(Static.Service.SECRETS,
                    path.getPath(),
                    Properties.class);

            Collection.connection.addSigHandler(org.purejava.secret.interfaces.Collection.ItemCreated.class, this::notifyOnItemCreated);
            Collection.connection.addSigHandler(org.purejava.secret.interfaces.Collection.ItemChanged.class, this::notifyOnItemChanged);
            Collection.connection.addSigHandler(org.purejava.secret.interfaces.Collection.ItemDeleted.class, this::notifyOnItemDeleted);

        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }


    @Override
    protected String getUnavailableMessage() {
        return COLLECTION_NOT_AVAILABLE;
    }

    public static Map<String, Variant<?>> createProperties(String label) {
        HashMap<String, Variant<?>> properties = new HashMap<>();
        properties.put(LABEL, new Variant<>(label));
        return properties;
    }

    /**
     * Delete this collection.
     *
     * @return In case the DBus call succeeded: prompt &mdash; A prompt to delete the collection, or the special
     * value '/' when no prompt is necessary, the DBus error otherwise.
     * @see DBusPath
     */
    public DBusResult<DBusPath> delete() {
        return dBusCall("Delete", getDBusPath(), () -> remote.Delete());
    }

    /**
     * Search for items in this collection matching the lookup attributes.
     *
     * @param attributes Attributes to match.
     * @return In case the DBus call succeeded: results     &mdash; Items that matched the attributes, the DBus error
     * otherwise.
     * @see DBusPath
     */
    public DBusResult<List<DBusPath>> searchItems(Map<String, String> attributes) {
        return dBusCall("SearchItems", getDBusPath(), () -> remote.SearchItems(attributes));
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
     * @return In case the DBus call succeeded: Pair&lt;item, prompt&gt;<br>
     * <br>
     * item                 &mdash; The item created, or the special value '/' if a prompt is necessary.<br>
     * <br>
     * prompt               &mdash; A prompt object, or the special value '/' if no prompt is necessary,<br>
     * <br>
     * the DBus error otherwise.
     * @see DBusPath
     */
    public DBusResult<Pair<DBusPath, DBusPath>> createItem(Map<String, Variant<?>> properties, Secret secret, boolean replace) {
        if (null == secret) {
            LOG.error("Cannot createItem as required secret is missing");
            return null;
        }
        return dBusCall("CreateItem", getDBusPath(), () -> remote.CreateItem(properties, secret, replace));
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Items in this collection, in case the DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<List<DBusPath>> getItems() {

        DBusResult<List<DBusPath>> result = dBusCall(
                "Get(Items)",
                getDBusPath(),
                () -> properties.Get(Static.Interfaces.COLLECTION, "Items")
        );

        if (!result.isSuccess()) {
            // propagate error wrapped in the same container type
            return new DBusResult<>(null, result.error());
        }

        return new DBusResult<>(result.value(), null);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The displayable label of this collection, in case the DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<String> getLabel() {

        DBusResult<String> result = dBusCall(
                "Get(Label)",
                getDBusPath(),
                () -> properties.Get(Static.Interfaces.COLLECTION, "Label")
        );

        if (!result.isSuccess()) {
            // propagate error wrapped in the same container type
            return new DBusResult<>(null, result.error());
        }

        return new DBusResult<>(result.value(), null);
    }

    public void setLabel(String value) {
        dBusCall("Set(Label)", getDBusPath(), () -> {
            properties.Set(Static.Interfaces.COLLECTION, "Label", new Variant<>(value));
            return null;
        });
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Whether the collection is locked and must be authenticated by the client application, in case the
     * DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<Boolean> isLocked() {
        return dBusCall("Get(Locked)", getDBusPath(), () ->
                properties.Get(Static.Interfaces.COLLECTION, "Locked"));
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The unix time when the collection was created, in case the DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<UInt64> getCreated() {

        DBusResult<UInt64> result = dBusCall(
                "Get(Created)",
                getDBusPath(),
                () -> properties.Get(Static.Interfaces.COLLECTION, "Created")
        );

        if (!result.isSuccess()) {
            return new DBusResult<>(null, result.error());
        }

        return new DBusResult<>(result.value(), null);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The unix time when the collection was last modified, in case the DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<UInt64> getModified() {

        DBusResult<UInt64> result = dBusCall(
                "Get(Modified)",
                getDBusPath(),
                () -> properties.Get(Static.Interfaces.COLLECTION, "Modified")
        );

        if (!result.isSuccess()) {
            return new DBusResult<>(null, result.error());
        }

        return new DBusResult<>(result.value(), null);
    }

    public String getDBusPath() {
        return path.getPath();
    }

    private void notifyOnItemCreated(org.purejava.secret.interfaces.Collection.ItemCreated signal) {
        if (signal.item.getPath().startsWith(getDBusPath())) {
            itemCreatedHandlers.forEach(handler -> handler.onItemCreated(signal.item));
        }
    }
    private void notifyOnItemChanged(org.purejava.secret.interfaces.Collection.ItemChanged signal) {
        if (signal.item.getPath().startsWith(getDBusPath())) {
            itemChangedHandlers.forEach(handler -> handler.onItemChanged(signal.item));
        }
    }
    private void notifyOnItemDeleted(org.purejava.secret.interfaces.Collection.ItemDeleted signal) {
        if (signal.item.getPath().startsWith(getDBusPath())) {
            itemDeletedHandlers.forEach(handler -> handler.onItemDeleted(signal.item));
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
