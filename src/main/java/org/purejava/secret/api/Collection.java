package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.types.Variant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Collection extends org.purejava.secret.impl.Collection {
    static String LABEL = "org.freedesktop.Secret.Collection.Label";

    public Collection() {
        super();
    }
    public Collection(DBusPath path) {
        super(path);
    }

    public static Map<String, Variant<?>> createProperties(String label) {
        HashMap<String, Variant<?>> properties = new HashMap<>();
        properties.put(LABEL, new Variant<>(label));
        return properties;
    }

    /**
     * Delete this collection.
     *
     * @return prompt &mdash; A prompt to delete the collection, or the special value '/' when no prompt is necessary.
     * @see DBusPath
     */
    public DBusPath delete() {
        return collection.Delete();
    }

    /**
     * Search for items in this collection matching the lookup attributes.
     *
     * @param attributes Attributes to match.
     * @return results     &mdash; Items that matched the attributes.
     * @see DBusPath
     */
    public List<DBusPath> searchItems(Map<String, String> attributes) {
        return collection.SearchItems(attributes);
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
    public Pair<DBusPath, DBusPath> createItem(Map<String, Variant<?>> properties, Secret secret, boolean replace) {
        return collection.CreateItem(properties, secret, replace);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Items in this collection.
     */
    public List<DBusPath> getItems() {
        return Items();
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
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Whether the collection is locked and must be authenticated by the client application.
     */
    public boolean isLocked() {
        return Locked();
    }

    /**
     * Read-only property "Created"
     *
     * @return The unix time when the collection was created.
     */
    public Long getCreated() {
        var c = Created();
        return null == c ? null : c.longValue();
    }

    /**
     * Read-only property "Modified"
     *
     * @return The unix time when the collection was last modified.
     */
    public Long getModified() {
        var m = Modified();
        return null == m ? null : m.longValue();
    }
}
