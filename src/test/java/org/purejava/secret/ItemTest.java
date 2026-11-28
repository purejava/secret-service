package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.purejava.secret.api.Collection;
import org.purejava.secret.api.DBusMessageHandler;
import org.purejava.secret.api.EncryptedSession;
import org.purejava.secret.api.Item;
import org.purejava.secret.api.Static;
import org.purejava.secret.api.Util;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ItemTest {

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

    private static final String LABEL = "TESTCreateItem";
    private static final String NAME = "testcreateitem";

    private Context context;

    @BeforeEach
    void beforeEach() {
        context = new Context();
        context.ensureService();
    }

    @Test
    @DisplayName("Create an item with an encrypted secret and manipulate that")
    void testItemInterfaces() throws InvalidAlgorithmParameterException,
        NoSuchAlgorithmException,
        InvalidKeySpecException,
        InvalidKeyException,
        NoSuchPaddingException,
        IllegalBlockSizeException,
        BadPaddingException {
        EncryptedSession session = new EncryptedSession();
        session.initialize();
        boolean sessionOpened = session.openSession();
        assertTrue(sessionOpened);
        session.generateSessionKey();
        var collectionProperties = Collection.createProperties(LABEL);
        var createCollectionResult = requireSuccess(
            context.service.createCollection(collectionProperties, ""),
            "Failed to create collection"
        );
        DBusPath collectionPath = createCollectionResult.a;
        DBusPath collectionPrompt = createCollectionResult.b;
        assertEquals("/", collectionPath.getPath());
        DBusPath promptResult =
            Util.promptAndGetResultAsDBusPath(collectionPrompt);
        var provider = SecretServiceProvider.detect();
        switch (provider) {
            case KWALLET, GNOME_KEYRING -> assertEquals(Static.DBusPath.COLLECTION + "/" + LABEL, promptResult.getPath());
            case OO7 -> assertEquals(Static.DBusPath.COLLECTION + "/" + NAME, promptResult.getPath());
            default -> fail("Unsupported Secret Service provider");
        }
        var myCollection = new Collection(promptResult);
        Map<String, String> attributes = new HashMap<>();
        attributes.put("Attrib1", "Value1");
        attributes.put("Attrib2", "Value2");
        var itemProperties = Item.createProperties("HelloItem", attributes);
        var secret = session.encrypt("passwd");
        var createItemResult = requireSuccess(
            myCollection.createItem(itemProperties, secret, false),
            "Failed to create item");
        DBusPath createdItemPath = createItemResult.a;
        switch (provider) {
            case KWALLET, GNOME_KEYRING -> assertTrue(createdItemPath.getPath().startsWith(Static.DBusPath.COLLECTION + "/" + LABEL + "/"));
            case OO7 -> assertTrue(createdItemPath.getPath().startsWith(Static.DBusPath.COLLECTION + "/" + NAME + "/"));
            default -> fail("Unsupported Secret Service provider");
        }
        var foundItems = requireSuccess(
            myCollection.searchItems(attributes),
            "Failed to search collection items"
        );
        assertFalse(foundItems.isEmpty());
        DBusPath relevantPath = foundItems.getFirst();
        switch (provider) {
            case KWALLET, GNOME_KEYRING -> assertTrue(relevantPath.getPath().startsWith(Static.DBusPath.COLLECTION + "/" + LABEL + "/"));
            case OO7 -> assertTrue(relevantPath.getPath().startsWith(Static.DBusPath.COLLECTION + "/" + NAME + "/"));
            default -> fail("Unsupported Secret Service provider");
        }
        var item = new Item(relevantPath);
        var encryptedSecret = item.getSecret(session.getSession());
        char[] decryptedSecret = session.decrypt(encryptedSecret);
        assertEquals("passwd", new String(decryptedSecret));
        secret = session.encrypt("PASSWD");
        new Item(new DBusPath(relevantPath.getPath())).setSecret(secret);
        encryptedSecret = item.getSecret(session.getSession());
        decryptedSecret = session.decrypt(encryptedSecret);
        assertEquals("PASSWD", new String(decryptedSecret));
        String label = requireSuccess(
            item.getLabel(),
            "Failed to retrieve item label"
        );
        assertEquals("HelloItem", label);
        DBusPath itemDeletePrompt = requireSuccess(
            item.delete(),
            "Failed to delete item"
        );
        assertEquals("/", itemDeletePrompt.getPath());
        foundItems = requireSuccess(
            myCollection.searchItems(attributes),
            "Failed to search collection items after deletion"
        );
        assertTrue(foundItems.isEmpty());
        DBusPath collectionDeletePrompt = requireSuccess(
            myCollection.delete(),
            "Failed to delete collection"
        );
        assertEquals("/", collectionDeletePrompt.getPath());
    }
}
