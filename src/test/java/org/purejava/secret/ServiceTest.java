package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.junit.jupiter.api.*;
import org.purejava.secret.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceTest.class);
    private Context context;

    @BeforeEach
    public void beforeEach(TestInfo info) {
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
    @Order(1)
    @DisplayName("Create collection (dismissed)")
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
    @Order(2)
    @DisplayName("Create collection (empty password)")
    @Disabled
    // one collection is dismissed, the other is created with an empty password
    void createCollection() {
        final String NAME = "TESTmyCollectionEmptyPassword";
        final String COLLECTION_PATH = "/org/freedesktop/secrets/collection/TESTmyCollectionEmptyPassword";
        var props = Collection.createProperties(NAME);
        var pair = context.service.createCollection(props, "");
        var path = pair.a.getPath();
        var promtp = pair.b;
        assertEquals("/", path);
        var result = Util.promptAndGetResult(promtp);
        assertEquals(COLLECTION_PATH, result.getPath());
        var collections = context.service.getCollections();
        List<String> paths = collections.stream()
                .map(DBusPath::getPath)
                .toList();
        for (String s : paths) {
            LOG.info(s);
        }
        var myCollection = new Collection(new DBusPath(Static.DBusPath.COLLECTION + "/" + NAME));
//        var label = myCollection.getLabel();
//        assertEquals("", label);
//        //assertEquals("/", alias.getPath());
        var dBusPath = myCollection.delete();
            assertEquals("/", dBusPath.getPath());
    }

}
