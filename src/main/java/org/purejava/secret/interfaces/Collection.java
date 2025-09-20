package org.purejava.secret.interfaces;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;
import org.purejava.secret.Pair;
import org.purejava.secret.Secret;

import java.util.List;
import java.util.Map;

@DBusInterfaceName("org.freedesktop.Secret.Collection")
public interface Collection extends DBusInterface {

    class ItemCreated extends DBusSignal {
        public final DBusPath item;

        /**
         * A new item in this collection was created.
         *
         * @param path  The path to the object this is emitted from.
         * @param item  The item that was created.
         * @throws DBusException Could not communicate properly with the D-Bus.
         */
        public ItemCreated(String path, DBusPath item) throws DBusException {
            super(path, item);
            this.item = item;
        }
    }

    class ItemDeleted extends DBusSignal {
        public final DBusPath item;

        /**
         * An item in this collection was deleted.
         *
         * @param path  The path to the object this is emitted from.
         * @param item  The item that was deleted.
         * @throws DBusException Could not communicate properly with the D-Bus.
         */
        public ItemDeleted(String path, DBusPath item) throws DBusException {
            super(path, item);
            this.item = item;
        }
    }

    class ItemChanged extends DBusSignal {
        public final DBusPath item;

        /**
         * An item in this collection changed.
         *
         * @param path  The path to the object this is emitted from.
         * @param item  The item that was changed.
         * @throws DBusException Could not communicate properly with the D-Bus.
         */
        public ItemChanged(String path, DBusPath item) throws DBusException {
            super(path, item);
            this.item = item;
        }
    }

    /**
     * Delete this collection.
     *
     * @return prompt &mdash; A prompt to delete the collection, or the special value '/' when no prompt is necessary.
     * @see DBusPath
     */
    DBusPath delete();

    /**
     * Search for items in this collection matching the lookup attributes.
     *
     * @param attributes   Attributes to match.
     * @return results     &mdash; Items that matched the attributes.
     * @see DBusPath
     */
    List<DBusPath> searchItems(Map<String, String> attributes);

    /**
     * Create an item with the given attributes, secret and label. If replace is set, then it replaces an item already
     * present with the same values for the attributes.
     *
     * @param  properties   The properties for the new item.
     *
     *                      <p>This allows setting the new item's properties upon its creation. All READWRITE properties
     *                      are useable. Specify the property names in full <code>interface.Property</code> form.</p>
     *
     *                      <p>
     *                          <b>Example 13.2. Example for properties of an item:</b><br>
     *                          <code>
     *                          properties = {<br>
     *                              &nbsp;&nbsp;"org.freedesktop.Secret.Item.Label": "MyItem",<br>
     *                              &nbsp;&nbsp;"org.freedesktop.Secret.Item.Attributes": {<br>
     *                              &nbsp;&nbsp;&nbsp;&nbsp;"Attribute1": "Value1",<br>
     *                              &nbsp;&nbsp;&nbsp;&nbsp;"Attribute2": "Value2"<br>
     *                              &nbsp;&nbsp;}<br>
     *                          }<br>
     *                      </code></p>
     *
     *                      <p>
     *                          <b>Note:</b>
     *                          Please note that there is a distinction between the terms <i>Property</i>, which refers
     *                          to D-Bus properties of an object, and <i>Attribute</i>, which refers to one of a
     *                          secret item's string-valued attributes.
     *                      </p>
     *
     * @param secret        The secret to store in the item, encoded with the included session.
     * @param replace       Whether to replace an item with the same attributes or not.
     * @return Pair&lt;item, prompt&gt;<br>
     * <br>
     * item                 &mdash; The item created, or the special value '/' if a prompt is necessary.<br>
     * <br>
     * prompt               &mdash; A prompt object, or the special value '/' if no prompt is necessary.<br>
     * @see DBusPath
     */
    Pair<DBusPath, DBusPath> createItem(Map<String, Variant<?>> properties, Secret secret, boolean replace);

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     * @return  Items in this collection.
     */
    List<DBusPath> items();

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     * @return  The displayable label of this collection.
     */
    String label();

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     * @return  Whether the collection is locked and must be authenticated by the client application.
     */
    boolean locked();

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     * @return  The unix time when the collection was created.
     */
    UInt64 created();

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     * @return  The unix time when the collection was last modified.
     */
    UInt64 modified();

}
