package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.purejava.secret.api.Collection;
import org.purejava.secret.api.Static;
import org.purejava.secret.api.Util;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceLockTest {
    final String NAME = "TESTLock";
    final String COLLECTION_PATH = "/org/freedesktop/secrets/collection/TESTLock";
    private Context context;

    @BeforeEach
    public void beforeEach() {
        context = new Context();
        context.ensureService();
    }

    @Test
    @DisplayName("Create collection, lock and unlock it")
    // this collection should be created with an empty password
    void createCollectionAndLock() {
        var props = Collection.createProperties(NAME);
        var pair = context.service.createCollection(props, "");
        var path = pair.a.getPath();
        var promtp = pair.b;
        assertEquals("/", path);
        var result = Util.promptAndGetResultAsDBusPath(promtp);
        assertEquals(COLLECTION_PATH, result.getPath());
        var myCollection = new Collection(new DBusPath(Static.DBusPath.COLLECTION + "/" + NAME));
        assertFalse(myCollection.isLocked());
        List<DBusPath> toLock = new ArrayList<>();
        toLock.add(new DBusPath(COLLECTION_PATH));
        context.service.lock(toLock);
        assertTrue(myCollection.isLocked());
        var prompt = context.service.unlock(toLock);
        var promptb = prompt.b;
        assertNotEquals("/", promptb.getPath());
        var unlocked = Util.promptAndGetResultAsArrayList(promptb);
        assertEquals(COLLECTION_PATH, unlocked.getFirst().getPath());
        var dBusPath = myCollection.delete();
        assertEquals("/", dBusPath.getPath());
    }
}