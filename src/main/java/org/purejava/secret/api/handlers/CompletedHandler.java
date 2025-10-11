package org.purejava.secret.api.handlers;

import org.freedesktop.dbus.types.Variant;

@FunctionalInterface
public interface CompletedHandler {
    void onCompleted(boolean dismissed, Variant<?> result);
}
