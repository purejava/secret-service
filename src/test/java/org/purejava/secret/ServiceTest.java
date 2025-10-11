package org.purejava.secret;

import org.freedesktop.dbus.DBusPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.purejava.secret.api.Service;
import org.purejava.secret.api.Static;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceTest {

    @Test
    @DisplayName("List collection(s)")
    void listCollections() {
        Service service = new Service();
        var collections = service.getCollections();
        List<String> paths = collections.stream()
                .map(DBusPath::getPath)
                .toList();
        assertTrue(List.of(
                Static.DBusPath.SESSION_COLLECTION,
                Static.DBusPath.LOGIN_COLLECTION,
                Static.DBusPath.KDEWALLET_COLLECTION
        ).contains(paths.getFirst()));
    }
}
