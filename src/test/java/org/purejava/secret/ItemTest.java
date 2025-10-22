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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItemTest {
    final String NAME = "TESTCreateItem";
    final String COLLECTION_PATH = "/org/freedesktop/secrets/collection/TESTCreateItem";
    private Context context;

    @BeforeEach
    public void beforeEach() {
        context = new Context();
        context.ensureService();
    }

    @Test
    @DisplayName("Create an item with an encrypted secret and manipulate that")
    void testItemInterfaces() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        EncryptedSession session = new EncryptedSession();
        session.initialize();
        var sessionOpened = session.openSession();
        session.generateSessionKey();
        assertTrue(sessionOpened);
        var props = Collection.createProperties(NAME);
        var pair = context.service.createCollection(props, "");
        var path = pair.a.getPath();
        var promtp = pair.b;
        assertEquals("/", path);
        var result = Util.promptAndGetResultAsDBusPath(promtp);
        assertEquals(COLLECTION_PATH, result.getPath());
        var myCollection = new Collection(new DBusPath(Static.DBusPath.COLLECTION + "/" + NAME));
        Map<String, String> attribs = new HashMap<>();
        attribs.put("Attrib1", "Value1");
        attribs.put("Attrib2", "Value2");
        var itemProps = Item.createProperties("HelloItem", attribs);
        var secret = session.encrypt("passwd");
        pair = myCollection.createItem(itemProps, secret, false);
        assertTrue(pair.a.getPath().startsWith(COLLECTION_PATH + "/"));
        var found = myCollection.searchItems(attribs);
        assertTrue(found.getFirst().getPath().startsWith(COLLECTION_PATH + "/"));
        var relevantPath = found.getFirst().getPath();
        var newSecret = new Item(new DBusPath(relevantPath)).getSecret(session.getSession());
        var fin = session.decrypt(newSecret);
        assertEquals("passwd", new String(fin));
        secret = session.encrypt("PASSWD");
        new Item(new DBusPath(relevantPath)).setSecret(secret);
        newSecret = new Item(new DBusPath(relevantPath)).getSecret(session.getSession());
        fin = session.decrypt(newSecret);
        assertEquals("PASSWD", new String(fin));
        assertEquals("HelloItem", new Item(new DBusPath(relevantPath)).getLabel());
        var promptRequired = new Item(new DBusPath(relevantPath)).delete();
        assertEquals("/", promptRequired.getPath());
        found = myCollection.searchItems(attribs);
        assertEquals(0, found.size());
        var dBusPath = myCollection.delete();
        assertEquals("/", dBusPath.getPath());
    }
}
