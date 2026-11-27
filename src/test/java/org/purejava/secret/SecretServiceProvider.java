package org.purejava.secret;

import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.interfaces.DBus;
import org.freedesktop.dbus.types.UInt32;

import java.nio.file.Files;
import java.nio.file.Path;

public enum SecretServiceProvider {
    KWALLET,
    GNOME_KEYRING,
    OO7,
    UNKNOWN;

    private static final String SERVICE = "org.freedesktop.secrets";

    public static SecretServiceProvider detect() {
        try (var connection = DBusConnectionBuilder.forSessionBus()
                .withShared(false)
                .build()) {

            var dbus = connection.getRemoteObject(
                    "org.freedesktop.DBus",
                    "/org/freedesktop/DBus",
                    DBus.class);

            if (!dbus.NameHasOwner(SERVICE)) {
                dbus.StartServiceByName(SERVICE, new UInt32(0));
            }

            var pid = dbus.GetConnectionUnixProcessID(SERVICE);

            var executable = Files.readSymbolicLink(
                    Path.of("/proc", pid.toString(), "exe"))
                    .getFileName()
                    .toString();

            return switch (executable) {
                case "kwalletd5", "kwalletd6", "ksecretd" -> KWALLET;
                case "gnome-keyring-daemon" -> GNOME_KEYRING;
                case "oo7-daemon" -> OO7;
                default -> UNKNOWN;
            };

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to detect Secret Service provider", e);
        }
    }

    public static boolean isProvider(SecretServiceProvider expected) {
        return detect() == expected;
    }
}
