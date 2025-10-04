package org.purejava.secret;

import org.freedesktop.dbus.types.Variant;

@FunctionalInterface
public interface CompletedHandler {
    void onCompleted(boolean dismissed, Variant<?> result);
}
