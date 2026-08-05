package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.purejava.secret.api.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CollectionCreateItemTest {

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

    final String NAME = "TESTCreateItem";
    final String COLLECTION_PATH = "/org/freedesktop/secrets/collection/TESTCreateItem";
    private Context context;

    @BeforeEach
    void beforeEach() {
        context = new Context();
        context.ensureService();
    }

    @Test
    @DisplayName("Create an item with an encrypted secret, search for it and test collections props")
    void createItemSecret() throws InvalidAlgorithmParameterException,
        NoSuchAlgorithmException,
        InvalidKeySpecException,
        InvalidKeyException,
        NoSuchPaddingException,
        IllegalBlockSizeException,
        BadPaddingException,
        InterruptedException {
        EncryptedSession session = new EncryptedSession();
        session.initialize();
        boolean sessionOpened = session.openSession();
        assertTrue(sessionOpened);
        session.generateSessionKey();
        var props = Collection.createProperties(NAME);
        long currentTime = new Date().getTime() / 1000L;
        var createCollectionResult = requireSuccess(
            context.service.createCollection(props, ""),
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
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        DBusPath[] handlerItemPath = new DBusPath[1];
        myCollection.addItemCreatedHandler(item -> {
            handlerCalled.set(true);
            handlerItemPath[0] = item;
        });
        Map<String, String> attributes = new HashMap<>();
        attributes.put("Attrib1", "Value1");
        attributes.put("Attrib2", "Value2");
        var itemProps = Item.createProperties("HelloItem", attributes);
        var secret = session.encrypt("passwd");
        var createItemResult = requireSuccess(
            myCollection.createItem(itemProps, secret, false),
            "Failed to create item"
        );
        DBusPath createdItemPath = createItemResult.a;
        assertTrue(
            createdItemPath.getPath().startsWith(COLLECTION_PATH + "/")
        );
        var foundItems = requireSuccess(
            myCollection.searchItems(attributes),
            "Failed to search collection items"
        );
        assertFalse(foundItems.isEmpty());
        assertTrue(
            foundItems.getFirst().getPath().startsWith(COLLECTION_PATH + "/")
        );
        Thread.sleep(200);
        assertTrue(handlerCalled.get());
        assertNotNull(handlerItemPath[0]);
        assertTrue(
            handlerItemPath[0].getPath().startsWith(COLLECTION_PATH + "/")
        );
        assertEquals(
            createdItemPath.getPath(),
            handlerItemPath[0].getPath()
        );
        var collectionItems = requireSuccess(
            myCollection.getItems(),
            "Failed to retrieve collection items"
        );
        assertEquals(1, collectionItems.size());
        assertTrue(
            collectionItems.getFirst()
                .getPath()
                .startsWith(COLLECTION_PATH + "/")
        );
        long created = requireSuccess(
            myCollection.getCreated(),
            "Failed to retrieve collection creation time"
        ).longValue();
        assertTrue(created > currentTime);
        long modified = requireSuccess(
            myCollection.getModified(),
            "Failed to retrieve collection modification time"
        ).longValue();
        if (ExpectedDesktop.isDesktop("KDE")) {
            assertTrue(modified >= created);
        }
        if (ExpectedDesktop.isDesktop("GNOME")) {
            assertEquals(0L, modified);
        }
        var serviceSearchResult = requireSuccess(
            context.service.searchItems(attributes),
            "Failed to search items through the service"
        );
        List<DBusPath> unlockedServiceItems = serviceSearchResult.a;
        assertFalse(unlockedServiceItems.isEmpty());
        assertEquals(
            unlockedServiceItems.getFirst().getPath(),
            collectionItems.getFirst().getPath()
        );
        DBusPath deletePrompt = requireSuccess(
            myCollection.delete(),
            "Failed to delete collection"
        );
        assertEquals("/", deletePrompt.getPath());
    }
}
