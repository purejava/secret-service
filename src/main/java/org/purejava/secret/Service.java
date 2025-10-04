package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBus;
import org.freedesktop.dbus.types.Variant;
import org.purejava.secret.freedesktop.dbus.handlers.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Service extends Messaging implements org.purejava.secret.interfaces.Service {

    private static final Logger LOG = LoggerFactory.getLogger(Service.class);
    private static final String DBUS_PATH = "/org/freedesktop/secrets";
    private static final String SERVICE_NOT_AVAILABLE = "Secret Service not available on DBus";
    private static DBusConnection connection;

    private final List<CollectionCreatedHandler> collectionCreatedHandlers = new CopyOnWriteArrayList<>();
    private final List<CollectionChangedHandler> collectionChangedHandlers = new CopyOnWriteArrayList<>();
    private final List<CollectionDeletedHandler> collectionDeletedHandlers = new CopyOnWriteArrayList<>();
    private org.purejava.secret.interfaces.Service service = null;

    static {
        try {
            connection = DBusConnectionBuilder.forSessionBus().withShared(false).build();
            connection.getRemoteObject("org.freedesktop.DBus",
                    "/org/freedesktop/DBus", DBus.class);
        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    public Service() {
        super(connection, Static.Service.SECRETS, DBUS_PATH, Static.Service.SECRETS);
        if (null != connection) {
            try {
                this.service = connection.getRemoteObject(Static.Service.SECRETS, DBUS_PATH, org.purejava.secret.interfaces.Service.class);
                connection.addSigHandler(CollectionCreated.class, this::notifyOnCollectionCreated);
                connection.addSigHandler(CollectionChanged.class, this::notifyOnCollectionChanged);
                connection.addSigHandler(CollectionDeleted.class, this::notifyOnCollectionDeleted);
            } catch (DBusException e) {
                LOG.error(e.toString(), e.getCause());
            }
        } else {
            LOG.error("Dbus not available");
        }
    }

    private boolean isUsable() {
        return null != service;
    }

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
    @Override
    public Pair<Variant<byte[]>, DBusPath> openSession(String algorithm, Variant<?> input) {
        if (isUsable()) {
            return service.openSession(algorithm, input);
        }
        LOG.error(SERVICE_NOT_AVAILABLE);
        return null;
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
    @Override
    public Pair<DBusPath, DBusPath> createCollection(Map<String, Variant<?>> properties, String alias) {
        if (isUsable()) {
            return service.createCollection(properties, alias);
        }
        LOG.error(SERVICE_NOT_AVAILABLE);
        return null;
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
    @Override
    public Pair<List<DBusPath>, List<DBusPath>> searchItems(Map<String, String> attributes) {
        if (isUsable()) {
            return service.searchItems(attributes);
        }
        LOG.error(SERVICE_NOT_AVAILABLE);
        return null;
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
    @Override
    public Pair<List<DBusPath>, DBusPath> unlock(List<DBusPath> objects) {
        if (!isUsable()) {
            LOG.error(SERVICE_NOT_AVAILABLE);
            return null;
        }
        if (null == objects) {
            LOG.error("Cannot unlock as required objects to unlock are missing");
            return null;
        }
        return service.unlock(objects);
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
    @Override
    public Pair<List<DBusPath>, DBusPath> lock(List<DBusPath> objects) {
        if (!isUsable()) {
            LOG.error(SERVICE_NOT_AVAILABLE);
            return null;
        }
        if (null == objects) {
            LOG.error("Cannot lock as required objects to lock are missing");
            return null;
        }
        return service.lock(objects);
    }

    /**
     * Retrieve multiple secrets from different items.
     *
     * @param items   Items to get secrets for.
     * @param session The session to use to encode the secrets.
     * @return secrets     &mdash; Secrets for the items.
     */
    @Override
    public Map<DBusPath, Secret> getSecrets(List<DBusPath> items, DBusPath session) {
        if (!isUsable()) {
            LOG.error(SERVICE_NOT_AVAILABLE);
            return null;
        }
        if (null == items) {
            LOG.error("Cannot getSecrets as required items are missing");
            return null;
        }
        if (Util.varIsEmpty(session.getPath())) {
            LOG.error("Cannot getSecrets as required session is missing");
            return null;
        }
        return service.getSecrets(items, session);
    }

    /**
     * Get the collection with the given alias.
     *
     * @param name An alias, such as 'default'.
     * @return collection   &mdash; The collection or the path '/' if no such collection exists.
     */
    @Override
    public DBusPath readAlias(String name) {
        if (!isUsable()) {
            LOG.error(SERVICE_NOT_AVAILABLE);
            return null;
        }
        if (Util.varIsEmpty(name)) {
            LOG.error("Cannot readAlias as required name is missing");
            return null;
        }
        return service.readAlias(name);
    }

    /**
     * Set up a collection alias.
     *
     * @param name       An alias, such as 'default'.
     * @param collection The collection to make the alias point to. To remove an alias use the special value '/'.
     */
    @Override
    public void setAlias(String name, DBusPath collection) {
        if (!isUsable()) {
            LOG.error(SERVICE_NOT_AVAILABLE);
            return;
        }
        if (Util.varIsEmpty(name)) {
            LOG.error("Cannot setAlias as required name is missing");
            return;
        }
        if (Util.varIsEmpty(collection.getPath())) {
            LOG.error("Cannot setAlias as required collection is missing");
            return;
        }
        service.setAlias(name, collection);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return A list of present collections.
     */
    @Override
    public List<DBusPath> collections() {
        if (!isUsable()) {
            LOG.error(SERVICE_NOT_AVAILABLE);
            return null;
        }
        var response = getProperty("Collections");
        return null == response ? null : (ArrayList<DBusPath>) response.getValue();
    }

    /**
     * @return The DBusPath of the Service.
     */
    @Override
    public String getObjectPath() {
        return super.getDBusPath();
    }

    private void notifyOnCollectionCreated(CollectionCreated signal) {
        if (getObjectPath().equals(signal.collection.getPath())) {
            for (CollectionCreatedHandler handler : collectionCreatedHandlers) {
                handler.onCollectionCreated(signal.collection);
            }
        }
    }
    private void notifyOnCollectionChanged(CollectionChanged signal) {
        if (getObjectPath().equals(signal.collection.getPath())) {
            for (CollectionChangedHandler handler : collectionChangedHandlers) {
                handler.onCollectionChanged(signal.collection);
            }
        }
    }
    private void notifyOnCollectionDeleted(CollectionDeleted signal) {
        if (getObjectPath().equals(signal.collection.getPath())) {
            for (CollectionDeletedHandler handler : collectionDeletedHandlers) {
                handler.onCollectionDeleted(signal.collection);
            }
        }
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
