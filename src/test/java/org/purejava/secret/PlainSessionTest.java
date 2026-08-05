package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.types.Variant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.purejava.secret.api.DBusMessageHandler;
import org.purejava.secret.api.EncryptedSession;
import org.purejava.secret.api.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

class PlainSessionTest {

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

    @Test
    @DisplayName("Establish a plain session")
    void establishPlainSession() {
        Service service = new Service();
        var openSessionResult = requireSuccess(
            service.openSession(
                EncryptedSession.Algorithm.PLAIN,
                new Variant<>("")
            ),
            "Failed to establish plain session"
        );
        Variant<?> responseVariant = openSessionResult.a;
        Object value = responseVariant.getValue();
        byte[] responseBytes;
        if (value instanceof ArrayList) {
            @SuppressWarnings("unchecked")
            List<Byte> list = (ArrayList<Byte>) value;
            responseBytes = new byte[list.size()];
            IntStream.range(0, list.size()).forEach(i -> responseBytes[i] = list.get(i));
        } else if (value instanceof String string) {
            responseBytes = string.getBytes(StandardCharsets.UTF_8);
        } else {
            throw new IllegalStateException(
                "DBus returned an unexpected result for openSession: "
                    + value.getClass().getName()
            );
        }
        DBusPath sessionPath = openSessionResult.b;
        assertEquals(0, responseBytes.length);
        assertFalse(sessionPath.getPath().isEmpty());
    }
}
