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
        byte[] bl;
        var response = service.openSession(EncryptedSession.Algorithm.PLAIN, new Variant<>(""));
        Variant<?> reaponsea = response.a;
        Object value = reaponsea.getValue();
        if (value instanceof ArrayList) {
            ArrayList<Byte> list = response.a.getValue();
            bl = new byte[list.size()];
            IntStream.range(0, list.size()).forEach(i -> bl[i] = list.get(i));
        } else if (value instanceof String) {
            bl = ((String) value).getBytes();
        } else {
            throw new IllegalStateException("Dbus returned unexpected result for openSession method call: " + value.getClass().getName());
        }
        DBusPath session = response.b;
        assertEquals(0, bl.length);
        assertFalse(session.getPath().isEmpty());
    }
}
