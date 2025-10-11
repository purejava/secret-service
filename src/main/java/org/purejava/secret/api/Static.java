package org.purejava.secret.api;

public class Static {
    public static class Service {
        public static final String SECRETS = "org.freedesktop.secrets";
    }

    public static class DBusPath {
        public static final String SECRETS = "/org/freedesktop/secrets";
        public static final String SESSION = "/org/freedesktop/secrets/session";
        public static final String ALIASES = "/org/freedesktop/secrets/aliases";
        public static final String DEFAULT_COLLECTION = "/org/freedesktop/secrets/aliases/default";
        public static final String COLLECTION = "/org/freedesktop/secrets/collection";
        public static final String SESSION_COLLECTION = "/org/freedesktop/secrets/collection/session";
        public static final String LOGIN_COLLECTION = "/org/freedesktop/secrets/collection/login";
        public static final String KDEWALLET_COLLECTION = "/org/freedesktop/secrets/collection/kdewallet";
        public static final String PROMPT = "/org/freedesktop/secrets/prompt";
    }

    public static class Interfaces {
        public static final String SESSION = "org.freedesktop.Secret.Session";
        public static final String SERVICE = "org.freedesktop.Secret.Service";
        public static final String COLLECTION = "org.freedesktop.Secret.Collection";
        public static final String ITEM = "org.freedesktop.Secret.Item";
        public static final String PROMPT = "org.freedesktop.Secret.Prompt";
    }
}
