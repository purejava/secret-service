package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.types.Variant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Service extends org.purejava.secret.impl.Service {

    /**
     * Open a unique session for the caller application.
     *
     * @param algorithm The algorithm the caller wishes to use.
     * @param input     Input arguments for the algorithm.
     * @return Pair&lt;output, result&gt;<br>
     * <br>
     * output   &mdash; Output of the session algorithm negotiation.<br>
     * <br>
     * result   &mdash; The object path of the session, if session was created.<br>
     */
    public Pair<Variant<ArrayList<Byte>>, DBusPath> openSession(String algorithm, Variant<?> input) {
        return service.OpenSession(algorithm, input);
    }

    /**
     * Create a new collection with the specified properties.
     *
     * @param properties Properties for the new collection. This allows setting the new collection's properties
     *                   upon its creation. All READWRITE properties are usable. Specify the property names in
     *                   full interface.Property form.<br>
     *                   <br>
     *                   Example for properties:
     *                   <p>
     *                   <code>properties = { "org.freedesktop.Secret.Collection.Label": "MyCollection" }</code>
     *                   </p>
     * @param alias An alias for the collection.
     * @return Pair&lt;collection, prompt&gt;<br>
     * <br>
     * collection   &mdash; The new collection object, or '/' if prompting is necessary.<br>
     * <br>
     * prompt       &mdash; A prompt object if prompting is necessary, or '/' if no prompt was needed.<br>
     */
    public Pair<DBusPath, DBusPath> createCollection(Map<String, Variant<?>> properties, String alias) {
        return service.CreateCollection(properties, alias);
    }

    /**
     * Find items in any collection.
     *
     * @param attributes Find secrets in any collection.
     *
     *                   <p>
     *                   <b>Example:</b>
     *                   <code>{
     *                   "Attribute1": "Value1",
     *                   "Attribute2": "Value2"
     *                   }</code>
     *                   </p>
     *
     *                   <p>
     *                   <b>Note:</b>
     *                   Please note that there is a distinction between the terms <i>Property</i>, which refers
     *                   to D-Bus properties of an object, and <i>Attribute</i>, which refers to one of a
     *                   secret item's string-valued attributes.
     *                   </p>
     * @return Pair&lt;unlocked, locked&gt;<br>
     * <br>
     * unlocked      &mdash; Items found.<br>
     * <br>
     * locked        &mdash; Items found that require authentication.<br>
     */
    public Pair<List<DBusPath>, List<DBusPath>> searchItems(Map<String, String> attributes) {
        return service.SearchItems(attributes);
    }

    /**
     * Unlock the specified objects.
     *
     * @param objects Objects to unlock.
     * @return Pair&lt;unlocked, prompt&gt;<br>
     * <br>
     * unlocked     &mdash; Objects that were unlocked without a prompt.<br>
     * <br>
     * prompt       &mdash; A prompt object which can be used to unlock the remaining objects, or the special value '/' when no prompt is necessary.<br>
     */
    public Pair<List<DBusPath>, DBusPath> unlock(List<DBusPath> objects) {
        return service.Unlock(objects);
    }

    /**
     * Lock the items.
     *
     * @param objects Objects to lock.
     * @return Pair&lt;locked, prompt&gt;<br>
     * <br>
     * locked      &mdash; Objects that were locked without a prompt.<br>
     * <br>
     * prompt      &mdash; A prompt to lock the objects, or the special value '/' when no prompt is necessary.<br>
     */
    public Pair<List<DBusPath>, DBusPath> lock(List<DBusPath> objects) {
        return service.Lock(objects);
    }

    /**
     * Retrieve multiple secrets from different items.
     *
     * @param items   Items to get secrets for.
     * @param session The session to use to encode the secrets.
     * @return secrets     &mdash; Secrets for the items.
     */
    public Map<DBusPath, Secret> getSecrets(List<DBusPath> items, DBusPath session) {
        return service.GetSecrets(items, session);
    }

    /**
     * Get the collection with the given alias.
     *
     * @param name An alias, such as 'default'.
     * @return collection   &mdash; The collection or the path '/' if no such collection exists.
     */
    public DBusPath readAlias(String name) {
        return service.ReadAlias(name);
    }

    /**
     * Set up a collection alias.
     *
     * @param name       An alias, such as 'default'.
     * @param collection The collection to make the alias point to. To remove an alias use the special value '/'.
     */
    public void setAlias(String name, DBusPath collection) {
        service.SetAlias(name, collection);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return A list of present collections.
     */
    public List<DBusPath> getCollections() {
        return Collections();
    }
}
