package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.purejava.secret.api.Collection;
import org.purejava.secret.api.DBusMessageHandler;
import org.purejava.secret.api.Static;
import org.purejava.secret.api.Util;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ServiceTest {

    private static <T> T requireSuccess(
        DBusMessageHandler.DBusResult<T> result,
        String message) {

        return switch (result) {
            case DBusMessageHandler.DBusResult.Success<T> success ->
                success.value();

            case DBusMessageHandler.DBusResult.Failure<T> failure ->
                fail(message, failure.error());
        };
    }

    private static final String NAME = "TESTmyCollectionEmptyPassword";

    private static final String COLLECTION_PATH =
        "/org/freedesktop/secrets/collection/TESTmyCollectionEmptyPassword";

    private Context context;

    @BeforeEach
    void beforeEach() {
        context = new Context();
        context.ensureService();
    }

    @Test
    @EnabledIfEnvironmentVariable(
        named = "XDG_CURRENT_DESKTOP",
        matches = ".*KDE.*"
    )
    @DisplayName("List collections on KDE")
    void listCollectionsKDE() {
        var collections = requireSuccess(
            context.service.getCollections(),
            "Failed to retrieve collections"
        );
        List<String> paths = collections.stream()
            .map(DBusPath::getPath)
            .toList();
        assertFalse(paths.isEmpty());
        assertTrue(List.of(
            Static.DBusPath.SESSION_COLLECTION,
            Static.DBusPath.LOGIN_COLLECTION,
            Static.DBusPath.KDEWALLET_COLLECTION
        ).contains(paths.getFirst()));
    }

    @Test
    @EnabledIfEnvironmentVariable(
        named = "XDG_CURRENT_DESKTOP",
        matches = ".*GNOME.*"
    )
    @DisplayName("List collections on GNOME")
    void listCollectionsGNOME() {
        var collections = requireSuccess(
            context.service.getCollections(),
            "Failed to retrieve collections"
        );
        List<String> paths = collections.stream()
            .map(DBusPath::getPath)
            .toList();
        assertFalse(paths.isEmpty());
        assertTrue(List.of(
            Static.DBusPath.SESSION_COLLECTION,
            Static.DBusPath.LOGIN_COLLECTION
        ).contains(paths.getFirst()));
    }

    @Test
    @DisplayName("Create collection (dismissed)")
        // This collection should be dismissed.
    void createCollectionCanceled() {
        var properties =
            Collection.createProperties("TESTmyCollection-dismissed");
        var createCollectionResult = requireSuccess(
            context.service.createCollection(properties, ""),
            "Failed to create collection"
        );
        DBusPath collectionPath = createCollectionResult.a;
        DBusPath collectionPrompt = createCollectionResult.b;
        assertEquals("/", collectionPath.getPath());
        if (ExpectedDesktop.isDesktop("KDE")) {
            var result =
                Util.promptAndGetResultAsArrayList(collectionPrompt);
            assertFalse(result.isEmpty());
            assertEquals("/", result.getFirst().getPath());
        }
        if (ExpectedDesktop.isDesktop("GNOME")) {
            DBusPath result =
                Util.promptAndGetResultAsDBusPath(collectionPrompt);
            assertEquals("/", result.getPath());
        }
    }

    @Test
    @DisplayName("Create collection (empty password)")
        // This collection should be created with an empty password.
    void createCollectionEmptyPassword() throws InterruptedException {
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        DBusPath[] handlerCollectionPath = new DBusPath[1];
        context.service.addCollectionCreatedHandler(collection -> {
            handlerCalled.set(true);
            handlerCollectionPath[0] = collection;
        });
        var properties = Collection.createProperties(NAME);
        var createCollectionResult = requireSuccess(
            context.service.createCollection(properties, ""),
            "Failed to create collection"
        );
        DBusPath collectionPath = createCollectionResult.a;
        DBusPath collectionPrompt = createCollectionResult.b;
        assertEquals("/", collectionPath.getPath());
        DBusPath promptResult =
            Util.promptAndGetResultAsDBusPath(collectionPrompt);
        assertEquals(COLLECTION_PATH, promptResult.getPath());
        var myCollection = new Collection(
            new DBusPath(Static.DBusPath.COLLECTION + "/" + NAME)
        );
        Thread.sleep(200);
        assertTrue(handlerCalled.get());
        assertNotNull(handlerCollectionPath[0]);
        assertEquals(
            COLLECTION_PATH,
            handlerCollectionPath[0].getPath()
        );
        String label = requireSuccess(
            myCollection.getLabel(),
            "Failed to retrieve collection label"
        );
        assertEquals(NAME, label);
        String newLabel = "testlabel";
        myCollection.setLabel(newLabel);
        label = requireSuccess(
            myCollection.getLabel(),
            "Failed to retrieve updated collection label"
        );
        assertEquals(newLabel, label);
        DBusPath deletePrompt = requireSuccess(
            myCollection.delete(),
            "Failed to delete collection"
        );
        assertEquals("/", deletePrompt.getPath());
    }
}
