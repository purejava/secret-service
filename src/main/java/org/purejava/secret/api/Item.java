package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Item extends DBusLoggingHandler<org.purejava.secret.interfaces.Item> {
    private static final Logger LOG = LoggerFactory.getLogger(Item.class);
    private static final String ITEM_NOT_AVAILABLE = "Item not available on DBus";
    private static final DBusConnection connection;

    private static final String LABEL = "org.freedesktop.Secret.Item.Label";
    private static final String ATTRIBUTES = "org.freedesktop.Secret.Item.Attributes";

    private final DBusPath path;

    static {
        connection = ConnectionManager.getInstance().getConnection();
    }

    public Item(DBusPath path) {
        super(Static.Service.SECRETS, path.getPath(), org.purejava.secret.interfaces.Item.class);

        this.path = path;

        try {

            this.remote = Item.connection.getRemoteObject(Static.Service.SECRETS,
                    path.getPath(),
                    org.purejava.secret.interfaces.Item.class);

            this.properties = Item.connection.getRemoteObject(Static.Service.SECRETS,
                    path.getPath(),
                    Properties.class);

        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    @Override
    protected String getUnavailableMessage() {
        return ITEM_NOT_AVAILABLE;
    }

    public static Map<String, Variant<?>> createProperties(String label, Map<String, String> attributes) {
        Map<String, Variant<?>> properties = new HashMap<>();
        properties.put(LABEL, new Variant<>(label));
        if (attributes != null) {
            properties.put(ATTRIBUTES, new Variant<>(attributes, "a{ss}"));
        }
        return properties;
    }

    /**
     * Delete this item.
     *
     * @return Prompt   &mdash; A prompt dbuspath, or the special value '/' if no prompt is necessary, in case the
     * DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<DBusPath> delete() {
        return dBusCall("Delete", getDBusPath(), () -> remote.Delete());
    }

    /**
     * Retrieve the secret for this item.
     *
     * @param session The session to use to encode the secret.
     * @return secret   &mdash; The secret retrieved, in case the DBus call succeeded, null otherwise.
     */
    public Secret getSecret(DBusPath session) {
        if (null == session) {
            LOG.error("Cannot getSecret as required session is missing");
            return null;
        }
        var secret = dBusCall("GetSecret", getDBusPath(), () -> remote.GetSecret(session));
        if (!secret.isSuccess()) {
            return null;
        } else {
            var contentType = secret.value().getContentType();
            var sessionPath = secret.value().getSession();
            var parameters = secret.value().getSecretParameters();
            var value = secret.value().getSecretValue();
            if (contentType.equals(Secret.TEXT_PLAIN) || contentType.equals(Secret.TEXT_PLAIN_CHARSET_UTF_8)) {
                // replace the content-type "text/plain" with default "text/plain; charset=utf8"
                return new Secret(sessionPath, parameters, value);
            } else {
                // use given non default content-type
                return new Secret(sessionPath, parameters, value, contentType);
            }
        }
    }

    /**
     * Set the secret for this item.
     *
     * @param secret The secret to set, encoded for the included session.
     */
    public void setSecret(Secret secret) {
        if (null == secret) {
            LOG.error("Cannot setSecret as required secret is missing");
            return;
        }
        dBusCall("SetSecret", getDBusPath(), () -> {
            remote.SetSecret(secret);
            return null;
        });
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return Whether the item is locked and requires authentication, or not, in case the DBus call succeeded,
     * the DBus error otherwise.
     */
    public DBusResult<Boolean> isLocked() {
        return dBusCall("Get(Locked)", getDBusPath(), () ->
                properties.Get(Static.Interfaces.ITEM, "Locked"));
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The attributes of the item, in case the DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<Map<String, String>> getAttributes() {

        DBusResult<Variant<?>> result = dBusCall(
                "Get(Attributes)",
                getDBusPath(),
                () -> properties.Get(Static.Interfaces.ITEM, "Attributes")
        );

        if (!result.isSuccess()) {
            return new DBusResult<>(null, result.error());
        }

        @SuppressWarnings("unchecked")
        Variant<Map<String, String>> variant = (Variant<Map<String, String>>) result.value();
        Map<String, String> attributes = variant.getValue();

        return new DBusResult<>(attributes, null);
    }

    /**
     * <p>It is accessed using the <code>org.freedesktop.DBus.Properties</code> interface.</p>
     *
     * @return The displayable label of this collection, in case the DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<String> getLabel() {

        DBusResult<Variant<?>> result = dBusCall(
                "Get(Label)",
                getDBusPath(),
                () -> properties.Get(Static.Interfaces.ITEM, "Label")
        );

        if (!result.isSuccess()) {
            // propagate error wrapped in the same container type
            return new DBusResult<>(null, result.error());
        }

        @SuppressWarnings("unchecked")
        String label = ((Variant<String>) result.value()).getValue();

        return new DBusResult<>(label, null);
    }

    /**
     * Read-only property "Created"
     *
     * @return The unix time when the item was created, in case the DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<Long> getCreated() {

        DBusResult<Variant<?>> result = dBusCall(
                "Get(Created)",
                getDBusPath(),
                () -> properties.Get(Static.Interfaces.ITEM, "Created")
        );

        if (!result.isSuccess()) {
            return new DBusResult<>(null, result.error());
        }

        @SuppressWarnings("unchecked")
        Variant<UInt64> variant = (Variant<UInt64>) result.value();
        Long created = variant.getValue().longValue();

        return new DBusResult<>(created, null);
    }

    /**
     * Read-only property "Modified"
     *
     * @return The unix time when the item was last modified, in case the DBus call succeeded, the DBus error otherwise.
     */
    public DBusResult<Long> getModified() {

        DBusResult<Variant<?>> result = dBusCall(
                "Get(Modified)",
                getDBusPath(),
                () -> properties.Get(Static.Interfaces.ITEM, "Modified")
        );

        if (!result.isSuccess()) {
            return new DBusResult<>(null, result.error());
        }

        @SuppressWarnings("unchecked")
        Variant<UInt64> variant = (Variant<UInt64>) result.value();
        Long modified = variant.getValue().longValue();

        return new DBusResult<>(modified, null);
    }

    public String getDBusPath() {
        return path.getPath();
    }
}
