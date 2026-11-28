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

class UnlockPromptTest {

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

    private static final String LABEL = "TESTPromtGivePWandDismissLater";
    private static final String NAME = "testpromtgivepwanddismisslater";

    private Context context;

    @BeforeEach
    void beforeEach() {
        context = new Context();
        context.ensureService();
    }

    @Test
    @DisplayName("Create collection, lock and unlock it, but dismiss the prompt")
        // This collection should be created with a password.
    void createCollectionAndTestPrompt() {
        var properties = Collection.createProperties(LABEL);
        var createCollectionResult = requireSuccess(
            context.service.createCollection(properties, ""),
            "Failed to create collection"
        );
        DBusPath collectionPath = createCollectionResult.a;
        DBusPath collectionPrompt = createCollectionResult.b;
        assertEquals("/", collectionPath.getPath());
        DBusPath promptResult =
            Util.promptAndGetResultAsDBusPath(collectionPrompt);
        assertEquals(Static.DBusPath.COLLECTION + "/" + NAME, promptResult.getPath());
        var myCollection = new Collection(
            new DBusPath(Static.DBusPath.COLLECTION + "/" + NAME)
        );
        boolean initiallyLocked = requireSuccess(
            myCollection.isLocked(),
            "Failed to determine initial collection lock state"
        );
        assertFalse(initiallyLocked);
        List<DBusPath> toLock = new ArrayList<>();
        toLock.add(new DBusPath(Static.DBusPath.COLLECTION + "/" + NAME));
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
        if (SecretServiceProvider.isProvider(SecretServiceProvider.KWALLET)) {
            assertNotEquals("/", unlockPrompt.getPath());
            var unlocked =
                Util.promptAndGetResultAsArrayList(unlockPrompt);
            assertFalse(unlocked.isEmpty());
            assertEquals("/", unlocked.getFirst().getPath());
        }
        if (SecretServiceProvider.isProvider(SecretServiceProvider.GNOME_KEYRING)) {
            assertNotEquals("/", unlockPrompt.getPath());
            var unlocked =
                Util.promptAndGetResultAsArrayList(unlockPrompt);
            assertTrue(unlocked.isEmpty());
        }
        DBusPath deletePrompt = requireSuccess(
            myCollection.delete(),
            "Failed to delete collection"
        );
        assertEquals("/", deletePrompt.getPath());
    }
}
