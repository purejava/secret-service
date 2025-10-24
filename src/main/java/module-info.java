module org.purejava.secret {
    requires java.desktop;
    requires org.freedesktop.dbus;
    requires org.slf4j;
    requires at.favre.lib.hkdf;

    exports org.purejava.secret.api.handlers;
    exports org.purejava.secret.api.errors;
    exports org.purejava.secret.api;
}