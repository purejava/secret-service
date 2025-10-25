package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.purejava.secret.api.handlers.CollectionChangedHandler;
import org.purejava.secret.api.handlers.CollectionCreatedHandler;
import org.purejava.secret.api.handlers.CollectionDeletedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Service extends DBusLoggingHandler<org.purejava.secret.interfaces.Service> {

    private static final Logger LOG = LoggerFactory.getLogger(Service.class);
    private static final String SERVICE_NOT_AVAILABLE = "Secret Service not available on DBus";
    private static final DBusConnection connection;

    private final List<CollectionCreatedHandler> collectionCreatedHandlers = new CopyOnWriteArrayList<>();
    private final List<CollectionChangedHandler> collectionChangedHandlers = new CopyOnWriteArrayList<>();
    private final List<CollectionDeletedHandler> collectionDeletedHandlers = new CopyOnWriteArrayList<>();

    static {
        connection = ConnectionManager.getInstance().getConnection();
    }

    public Service() {
        super(Static.Service.SECRETS, Static.DBusPath.SECRETS, org.purejava.secret.interfaces.Service.class);

        try {

            this.remote = Service.connection.getRemoteObject(Static.Service.SECRETS,
                    Static.DBusPath.SECRETS,
                    org.purejava.secret.interfaces.Service.class);

            this.properties = Service.connection.getRemoteObject(Static.Service.SECRETS,
                    Static.DBusPath.SECRETS,
                    Properties.class);

            Service.connection.addSigHandler(org.purejava.secret.interfaces.Service.CollectionCreated.class, this::notifyOnCollectionCreated);
            Service.connection.addSigHandler(org.purejava.secret.interfaces.Service.CollectionChanged.class, this::notifyOnCollectionChanged);
            Service.connection.addSigHandler(org.purejava.secret.interfaces.Service.CollectionDeleted.class, this::notifyOnCollectionDeleted);

        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    @Override
    protected String getUnavailableMessage() {
        return SERVICE_NOT_AVAILABLE;
    }

    /**
     * Open a unique session for the caller application.
     *
     * @param algorithm The algorithm the caller wishes to use.
     * @param input     Input arguments for the algorithm.
     * @return In case the DBus call succeeded: Pair&lt;output, result&gt;<br>
     * <br>
     * output   &mdash; Output of the session algorithm negotiation.<br>
     * <br>
     * result   &mdash; The object path of the session, if session was created,<br>
     * <br>
     * the DBus error otherwise.
     */
    public DBusResult<Pair<Variant<ArrayList<Byte>>, DBusPath>> openSession(String algorithm, Variant<?> input) {
        return dBusCall("OpenSession", getDBusPath(), () -> remote.OpenSession(algorithm, input));
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
     * @param alias      An alias for the collection.
     * @return In case the DBus call succeeded: Pair&lt;collection, prompt&gt;<br>
     * <br>
     * collection   &mdash; The new collection object, or '/' if prompting is necessary.<br>
     * <br>
     * prompt       &mdash; A prompt object if prompting is necessary, or '/' if no prompt was needed,<br>
     * <br>
     * the DBus error otherwise.
     */
    public DBusResult<Pair<DBusPath, DBusPath>> createCollection(Map<String, Variant<?>> properties, String alias) {
        return dBusCall("CreateCollection", getDBusPath(), () -> remote.CreateCollection(properties, alias));
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
     * @return In case the DBus call succeeded: Pair&lt;unlocked, locked&gt;<br>
     * <br>
     * unlocked      &mdash; Items found.<br>
     * <br>
     * locked        &mdash; Items found that require authentication,<br>
     * <br>
     * the DBus error otherwise.
     */
    public DBusResult<Pair<List<DBusPath>, List<DBusPath>>> searchItems(Map<String, String> attributes) {
        return dBusCall("SearchItems", getDBusPath(), () -> remote.SearchItems(attributes));
    }

    /**
     * Unlock the specified objects.
     *
     * @param objects Objects to unlock.
     * @return In case the DBus call succeeded: Pair&lt;unlocked, prompt&gt;<br>
     * <br>
     * unlocked     &mdash; Objects that were unlocked without a prompt.<br>
     * <br>
     * prompt       &mdash; A prompt object which can be used to unlock the remaining objects, or the special value '/' when no prompt is necessary.<br>
     * <br>
     * the DBus error otherwise.
     */
    public DBusResult<Pair<List<DBusPath>, DBusPath>> unlock(List<DBusPath> objects) {
        if (null == objects) {
            LOG.error("Cannot unlock as required objects to unlock are missing");
            return null;
        }
        return dBusCall("Unlock", getDBusPath(), () -> remote.Unlock(objects));
    }

    /**
     * Lock the items.
     *
     * @param objects Objects to lock.
     * @return In case the DBus call succeeded: Pair&lt;locked, prompt&gt;<br>
     * <br>
     * locked      &mdash; Objects that were locked without a prompt.<br>
     * <br>
     * prompt      &mdash; A prompt to lock the objects, or the special value '/' when no prompt is necessary.<br>
     * <br>
     * the DBus error otherwise.
     */
    public DBusResult<Pair<List<DBusPath>, DBusPath>> lock(List<DBusPath> objects) {
        if (null == objects) {
            LOG.error("Cannot lock as required objects to lock are missing");
            return null;
        }
        return dBusCall("Lock", getDBusPath(), () -> remote.Lock(objects));
    }

    /**
     * Retrieve multiple secrets from different items.
     *
     * @param items   Items to get secrets for.
     * @param session The session to use to encode the secrets.
     * @return secrets     &mdash; Secrets for the items, in case the DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<Map<DBusPath, Secret>> getSecrets(List<DBusPath> items, DBusPath session) {
        if (null == items) {
            LOG.error("Cannot getSecrets as required items are missing");
            return null;
        }
        if (Util.varIsEmpty(session.getPath())) {
            LOG.error("Cannot getSecrets as required session is missing");
            return null;
        }
        return dBusCall("GetSecrets", getDBusPath(), () -> remote.GetSecrets(items, session));
    }

    /**
     * Get the collection with the given alias.
     *
     * @param name An alias, such as 'default'.
     * @return collection   &mdash; The collection or the path '/' if no such collection exists, in case the DBus
     * call succeeded, the DBus error otherwise.
     */
    public DBusResult<DBusPath> readAlias(String name) {
        if (Util.varIsEmpty(name)) {
            LOG.error("Cannot readAlias as required name is missing");
            return null;
        }
        return dBusCall("ReadAlias", getDBusPath(), () -> remote.ReadAlias(name));
    }

    /**
     * Set up a collection alias.
     *
     * @param name       An alias, such as 'default'.
     * @param collection The collection to make the alias point to. To remove an alias use the special value '/'.
     */
    public void setAlias(String name, DBusPath collection) {
        if (Util.varIsEmpty(name)) {
            LOG.error("Cannot setAlias as required name is missing");
            return;
        }
        if (Util.varIsEmpty(collection.getPath())) {
            LOG.error("Cannot setAlias as required collection is missing");
            return;
        }
        dBusCall("SetAlias", getDBusPath(), () -> {
            remote.SetAlias(name, collection);
            return null;
        });
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return A list of present collections, in case the DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<List<DBusPath>> getCollections() {

        DBusResult<List<DBusPath>> result = dBusCall(
                "Get(Collections)",
                getDBusPath(),
                () -> properties.Get(Static.Interfaces.SERVICE, "Collections")
        );

        if (!result.isSuccess()) {
            // propagate error wrapped in the same container type
            return new DBusResult<>(null, result.error());
        }

        return new DBusResult<>(result.value(), null);
    }

    public String getDBusPath() {
        return Static.DBusPath.SECRETS;
    }

    private void notifyOnCollectionCreated(org.purejava.secret.interfaces.Service.CollectionCreated signal) {
        collectionCreatedHandlers.forEach(handler -> handler.onCollectionCreated(signal.collection));
    }

    private void notifyOnCollectionChanged(org.purejava.secret.interfaces.Service.CollectionChanged signal) {
        collectionChangedHandlers.forEach(handler -> handler.onCollectionChanged(signal.collection));
    }

    private void notifyOnCollectionDeleted(org.purejava.secret.interfaces.Service.CollectionDeleted signal) {
        collectionDeletedHandlers.forEach(handler -> handler.onCollectionDeleted(signal.collection));
    }

    public void addCollectionCreatedHandler(CollectionCreatedHandler handler) {
        collectionCreatedHandlers.add(handler);
    }

    public void removeCollectionCreatedHandler(CollectionCreatedHandler handler) {
        collectionCreatedHandlers.remove(handler);
    }

    public void addCollectionChangedHandler(CollectionChangedHandler handler) {
        collectionChangedHandlers.add(handler);
    }

    public void removeCollectionChangedHandler(CollectionChangedHandler handler) {
        collectionChangedHandlers.remove(handler);
    }

    public void addCollectionDeletedHandler(CollectionDeletedHandler handler) {
        collectionDeletedHandlers.add(handler);
    }

    public void removeCollectionDeletedHandler(CollectionDeletedHandler handler) {
        collectionDeletedHandlers.remove(handler);
    }
}
