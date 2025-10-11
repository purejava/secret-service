package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.types.Variant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.purejava.secret.api.EncryptedSession;
import org.purejava.secret.api.Service;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class PlainSessionTest {

    @Test
    @DisplayName("Establish a plain session")
    void establishPlainSession() {
        Service service = new Service();
        var response = service.openSession(EncryptedSession.Algorithm.PLAIN, new Variant<>(""));
        ArrayList<Byte> list = response.a.getValue();
        byte[] b = new byte[list.size()];
        IntStream.range(0, list.size()).forEach(i -> b[i] = list.get(i));
        DBusPath session = response.b;
        assertEquals(0, b.length);
        assertFalse(session.getPath().isEmpty());
    }
}
