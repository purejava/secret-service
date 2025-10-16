package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.junit.jupiter.api.*;
import org.purejava.secret.api.Collection;
import org.purejava.secret.api.Static;
import org.purejava.secret.api.Util;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceTest {
    final String NAME = "TESTmyCollectionEmptyPassword";
    final String COLLECTION_PATH = "/org/freedesktop/secrets/collection/TESTmyCollectionEmptyPassword";
    private Context context;

    @BeforeEach
    public void beforeEach() {
        context = new Context();
        context.ensureService();
    }

    @Test
    @DisplayName("List collection(s)")
    void listCollections() {
        var collections = context.service.getCollections();
        List<String> paths = collections.stream()
                .map(DBusPath::getPath)
                .toList();
        assertTrue(List.of(
                Static.DBusPath.SESSION_COLLECTION,
                Static.DBusPath.LOGIN_COLLECTION,
                Static.DBusPath.KDEWALLET_COLLECTION
        ).contains(paths.getFirst()));
    }

    @Test
    @DisplayName("Create collection (dismissed)")
    @Disabled
    void createCollectionCanceled() {
        var props = Collection.createProperties("TESTmyCollection-dismissed");
        var pair = context.service.createCollection(props, "");
        var path = pair.a.getPath();
        var promtp = pair.b;
        assertEquals("/", path);
        var result = Util.promptAndGetResult(promtp);
        assertEquals("/", result.getPath());
    }

    @Test
    @DisplayName("Create collection (empty password)")
    @Disabled
    // one collection is dismissed, the other is created with an empty password
    void createCollectionEmptyPassword() {
        var props = Collection.createProperties(NAME);
        var pair = context.service.createCollection(props, "");
        var path = pair.a.getPath();
        var promtp = pair.b;
        assertEquals("/", path);
        var result = Util.promptAndGetResult(promtp);
        assertEquals(COLLECTION_PATH, result.getPath());
        var myCollection = new Collection(new DBusPath(Static.DBusPath.COLLECTION + "/" + NAME));
        var label = myCollection.getLabel();
        assertEquals(NAME, label);
        String newLabel = "testlabel";
        myCollection.setLabel(newLabel);
        label = myCollection.getLabel();
        assertEquals("testlabel", label);
        var dBusPath = myCollection.delete();
        assertEquals("/", dBusPath.getPath());
    }
}
