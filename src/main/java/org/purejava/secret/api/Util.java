package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.types.Variant;

import java.util.ArrayList;
import java.util.List;
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
    public static DBusPath promptAndGetResultAsDBusPath(DBusPath path) {
        if (!path.getPath().startsWith(Static.DBusPath.PROMPT + "/p")) {
            throw new IllegalArgumentException("Invalid DBusPath was provided for Prompt: " + path.getPath());
        }
        AtomicReference<Variant<?>> resultRef = new AtomicReference<>();
        var latch = new CountDownLatch(1);
        var prompt = new Prompt(path);
        prompt.addCompletedHandler((dismissed, result) -> {
            resultRef.set(result);
            latch.countDown();
        });
        prompt.prompt("0");
        try {
            latch.await();
        } catch (InterruptedException i) {
            Thread.currentThread().interrupt();
            return new DBusPath("/");
        }

        Variant<?> variant = resultRef.get();
        if (variant == null) {
            return new DBusPath("/");
        }

        Object value = variant.getValue();

        if (value instanceof DBusPath) {
            @SuppressWarnings("unchecked")
            Variant<DBusPath> dBusPathVariant = ((Variant<DBusPath>) resultRef.get());
            return dBusPathVariant.getValue();
        }

        throw new IllegalStateException("Unexpected result type from Prompt: " + value.getClass());
    }

    /**
     * Shows the prompt for the given path and waits, until the prompt finished or was dismissed.
     *
     * @param path The <code>DBusPath</code> of the prompt to show.
     * @return The <code>DBusPath</code> of the object the prompt was executed for, e.g. an
     * <code>ArrayList<DbusPaths></code> of the objects, the prompt was executed for, like the
     * list paths of the Collections that were unlocked.<br>
     * <p>When something went wrong on executing the prompt or when the prompt was dismissed, "/" is returned.</p>
     */
    public static ArrayList<DBusPath> promptAndGetResultAsArrayList(DBusPath path) {
        if (!path.getPath().startsWith(Static.DBusPath.PROMPT + "/p")) {
            throw new IllegalArgumentException("Invalid DBusPath was provided for Prompt: " + path.getPath());
        }

        AtomicReference<Variant<?>> resultRef = new AtomicReference<>();
        var latch = new CountDownLatch(1);
        var prompt = new Prompt(path);
        prompt.addCompletedHandler((dismissed, result) -> {
            resultRef.set(result);
            latch.countDown();
        });

        prompt.prompt("0");

        try {
            latch.await();
        } catch (InterruptedException i) {
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        }

        Variant<?> variant = resultRef.get();
        if (variant == null) {
            return new ArrayList<>();
        }

        Object value = variant.getValue();

        if (value instanceof List<?> list) {
            // Ensure all elements are DBusPath
            if (!list.isEmpty() && list.getFirst() instanceof DBusPath) {
                @SuppressWarnings("unchecked")
                List<DBusPath> dbusPaths = (List<DBusPath>) list;
                return new ArrayList<>(dbusPaths);
            }
        }

        throw new IllegalStateException("Unexpected result type from Prompt: " + value.getClass());
    }
}
