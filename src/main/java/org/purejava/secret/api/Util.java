package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.types.Variant;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Util {

    public static boolean varIsEmpty(String v) {
        return v == null || v.isBlank();
    }

    /**
     * Shows the prompt for the given path and waits, until the prompt finished or was dismissed.
     *
     * @param path The <code>DBusPath</code> of the prompt to show.
     * @return The <code>DBusPath</code> of the object the prompt was executed for, e.g. the path of the collection.
     * When something went wrong on executing the prompt or when the prompt was dismissed, "/" is returned.
     */
    public static DBusPath promptAndGetResult(DBusPath path) {
        if (!path.getPath().startsWith(Static.DBusPath.PROMPT + "/p")) {
            throw new IllegalArgumentException("Invalid DBusPath was provided for Prompt: " + path.getPath());
        }
        AtomicReference<Variant<?>> resultRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Prompt prompt = new Prompt(path);
        prompt.addCompletedHandler((dismissed, result) -> {
            resultRef.set(result);
            latch.countDown();
        });
        prompt.prompt("0");
        try {
            latch.await();
        } catch (InterruptedException i) {
            return new DBusPath("/");
        }
        return ((Variant<DBusPath>) resultRef.get()).getValue();
    }
}
