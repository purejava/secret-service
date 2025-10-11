package org.purejava.secret.impl;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.purejava.secret.api.ConnectionManager;
import org.purejava.secret.api.Static;
import org.purejava.secret.api.Util;
import org.purejava.secret.api.handlers.CompletedHandler;
import org.purejava.secret.freedesktop.dbus.handlers.Messaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Prompt extends Messaging implements org.purejava.secret.interfaces.Prompt {

    private static final Logger LOG = LoggerFactory.getLogger(Prompt.class);
    private static final String PROMPT_NOT_AVAILABLE = "Prompt not available on DBus";
    private static final DBusConnection connection;

    private final List<CompletedHandler> completedHandlers = new CopyOnWriteArrayList<>();
    protected org.purejava.secret.interfaces.Prompt prompt = null;

    static {
        connection = ConnectionManager.getConnection();
    }

    public Prompt(DBusPath path) {
        super(connection, Static.Service.SECRETS, path.getPath(), Static.Interfaces.PROMPT);
        if (null != connection) {
            try {
                this.prompt = connection.getRemoteObject(Static.Service.SECRETS, path.getPath(), org.purejava.secret.interfaces.Prompt.class);
                connection.addSigHandler(org.purejava.secret.interfaces.Prompt.Completed.class, this::notifyOnCompleted);
            } catch (DBusException e) {
                LOG.error(e.toString(), e.getCause());
            }
        } else {
            LOG.error("Dbus not available");
        }
    }

    private boolean isUnusable() {
        return null == prompt;
    }

    /**
     * Perform the prompt.
     *
     * @param window_id Platform specific window handle to use for showing the prompt.
     */
    @Override
    public void Prompt(String window_id) {
        if (isUnusable()) {
            LOG.error(PROMPT_NOT_AVAILABLE);
            return;
        }
        if (Util.varIsEmpty(window_id)) {
            LOG.error("Cannot prompt as required window_id is missing");
            return;
        }
        prompt.Prompt(window_id);
    }

    /**
     * Dismiss the prompt.
     */
    @Override
    public void Dismiss() {
        if (isUnusable()) {
            LOG.error(PROMPT_NOT_AVAILABLE);
            return;
        }
        prompt.Dismiss();
    }

    /**
     * @return The DBusPath of the prompt.
     */
    @Override
    public String getObjectPath() {
        return super.getDBusPath();
    }

    private void notifyOnCompleted(Completed signal) {
        for (CompletedHandler handler : completedHandlers) {
            handler.onCompleted(signal.dismissed, signal.result);
        }
    }

    public void addCompletedHandler(CompletedHandler handler) {
        completedHandlers.add(handler);
    }

    public void removeCompletedHandler(CompletedHandler handler) {
        completedHandlers.remove(handler);
    }
}
