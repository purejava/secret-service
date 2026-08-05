package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.purejava.secret.api.Collection;
import org.purejava.secret.api.DBusMessageHandler;
import org.purejava.secret.api.Static;
import org.purejava.secret.api.Util;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ServiceLockTest {

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

    private static final String NAME = "TESTLock";
    private static final String COLLECTION_PATH =
        "/org/freedesktop/secrets/collection/TESTLock";

    private Context context;

    @BeforeEach
    void beforeEach() {
        context = new Context();
        context.ensureService();
    }

    @Test
    @DisplayName("Create collection, lock and unlock it")
        // This collection should be created with an empty password.
    void createCollectionAndLock() {
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
        boolean initiallyLocked = requireSuccess(
            myCollection.isLocked(),
            "Failed to determine initial collection lock state"
        );
        assertFalse(initiallyLocked);
        List<DBusPath> toLock = new ArrayList<>();
        toLock.add(new DBusPath(COLLECTION_PATH));
        requireSuccess(
            context.service.lock(toLock),
            "Failed to lock collection"
        );
        boolean locked = requireSuccess(
            myCollection.isLocked(),
            "Failed to determine collection lock state"
        );
        assertTrue(locked);
        var unlockResult = requireSuccess(
            context.service.unlock(toLock),
            "Failed to unlock collection"
        );
        DBusPath unlockPrompt = unlockResult.b;
        if (ExpectedDesktop.isDesktop("KDE")) {
            assertNotEquals("/", unlockPrompt.getPath());
            var unlocked =
                Util.promptAndGetResultAsArrayList(unlockPrompt);
            assertFalse(unlocked.isEmpty());
            assertEquals(
                COLLECTION_PATH,
                unlocked.getFirst().getPath()
            );
        }
        if (ExpectedDesktop.isDesktop("GNOME")) {
            assertEquals("/", unlockPrompt.getPath());
        }
        DBusPath deletePrompt = requireSuccess(
            myCollection.delete(),
            "Failed to delete collection"
        );
        assertEquals("/", deletePrompt.getPath());
    }
}
