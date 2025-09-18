module org.purejava.secretservice {
    requires java.desktop;
    requires org.freedesktop.dbus;
    requires org.slf4j;

    exports org.purejava.secretservice;
    exports org.purejava.secretservice.freedesktop.dbus.handlers;
}