module org.purejava.secret {
    requires java.desktop;
    requires org.freedesktop.dbus;
    requires org.slf4j;

    exports org.purejava.secret;
    exports org.purejava.secret.freedesktop.dbus.handlers;
    exports org.purejava.secret.interfaces;
}