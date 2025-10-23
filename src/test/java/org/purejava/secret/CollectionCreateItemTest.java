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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CollectionCreateItemTest {
    final String NAME = "TESTCreateItem";
    final String COLLECTION_PATH = "/org/freedesktop/secrets/collection/TESTCreateItem";
    private Context context;

    @BeforeEach
    public void beforeEach() {
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
        var sessionOpened = session.openSession();
        session.generateSessionKey();
        assertTrue(sessionOpened);
        var props = Collection.createProperties(NAME);
        var currentTime = new Date().getTime() / 1000L;
        var pair = context.service.createCollection(props, "");
        var path = pair.a.getPath();
        var promtp = pair.b;
        assertEquals("/", path);
        var result = Util.promptAndGetResultAsDBusPath(promtp);
        assertEquals(COLLECTION_PATH, result.getPath());
        var myCollection = new Collection(new DBusPath(Static.DBusPath.COLLECTION + "/" + NAME));
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        final DBusPath[] handlerItemPath = new DBusPath[1];

        myCollection.addItemCreatedHandler(item -> {
            handlerCalled.set(true);
            handlerItemPath[0] = item ;
        });
        Map<String, String> attribs = new HashMap<>();
        attribs.put("Attrib1", "Value1");
        attribs.put("Attrib2", "Value2");
        var itemProps = Item.createProperties("HelloItem", attribs);
        var secret = session.encrypt("passwd");
        pair = myCollection.createItem(itemProps, secret, false);
        assertTrue(pair.a.getPath().startsWith(COLLECTION_PATH + "/"));
        var found = myCollection.searchItems(attribs);
        assertTrue(found.getFirst().getPath().startsWith(COLLECTION_PATH + "/"));
        Thread.sleep(200);
        assertTrue(handlerCalled.get());
        assertTrue(handlerItemPath[0].getPath().startsWith(COLLECTION_PATH + "/"));
        assertEquals(pair.a.getPath(), handlerItemPath[0].getPath());
        found = myCollection.getItems();
        assertEquals(1, found.size());
        assertTrue(found.getFirst().getPath().startsWith(COLLECTION_PATH + "/"));
        assertTrue(myCollection.getCreated() > currentTime);
        assertTrue(myCollection.getModified() >= myCollection.getCreated());
        var serviceItems = context.service.searchItems(attribs);
        assertEquals(serviceItems.a.getFirst().getPath(), found.getFirst().getPath());
        var dBusPath = myCollection.delete();
        assertEquals("/", dBusPath.getPath());
    }
}
