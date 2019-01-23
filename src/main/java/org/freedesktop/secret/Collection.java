package org.freedesktop.secret;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.ObjectPath;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Collection extends org.freedesktop.secret.interfaces.Collection {

    private Service service;
    private String name;

    public static final List<Class> signals = Arrays.asList(ItemCreated.class, ItemChanged.class, ItemDeleted.class);

    public Collection(DBusPath path, Service service) {
        super(service.getConnection(), signals,
                Static.Service.SECRETS,
                path.getPath(),
                Static.Interfaces.COLLECTION);
        this.service = service;
        String[] split = path.getPath().split("/");
        this.name = split[split.length - 1];
    }

    public Collection(String name, Service service) {
        super(service.getConnection(), signals,
                Static.Service.SECRETS,
                Static.ObjectPaths.collection(name),
                Static.Interfaces.COLLECTION);
        this.service = service;
        this.name = name;
    }

    @Override
    public ObjectPath delete() {
        Object[] result = send("Delete", "");
        ObjectPath prompt = (ObjectPath) result[0];
        return prompt;
    }

    @Override
    public List<ObjectPath> searchItems(Map<String, String> attributes) {
        Object[] response = send("SearchItems", "a{ss}", attributes);
        return (List<ObjectPath>) response[0];
    }

    @Override
    public Pair<ObjectPath, ObjectPath> createItem(Map<String, Variant> properties, Secret secret,
                                                   boolean replace) {
        Object[] response = send("CreateItem", "a{sv}(oayays)b", properties, secret, replace);
        return new Pair(response[0], response[1]);
    }

    @Override
    public List<ObjectPath> getItems() {
        Variant response = getProperty("Items");
        return (ArrayList<ObjectPath>) response.getValue();
    }

    @Override
    public String getLabel() {
        Variant response = getProperty("Label");
        return (String) response.getValue();
    }

    @Override
    public void setLabel(String label) {
        setProperty("Label", new Variant(label));
    }

    @Override
    public boolean isLocked() {
        Variant response = getProperty("Locked");
        return (boolean) response.getValue();
    }

    @Override
    public UInt64 created() {
        Variant response = getProperty("Created");
        return (UInt64) response.getValue();
    }

    @Override
    public UInt64 modified() {
        Variant response = getProperty("Modified");
        return (UInt64) response.getValue();
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public String getObjectPath() {
        return super.getObjectPath();
    }

    public Session getSession() {
        return service.getSession();
    }

    public String getName() {
        return name;
    }

}
