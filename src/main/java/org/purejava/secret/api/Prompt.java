package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.purejava.secret.api.handlers.CompletedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Prompt extends DBusMessageHandler<org.purejava.secret.interfaces.Prompt> {

    private static final Logger LOG = LoggerFactory.getLogger(Prompt.class);
    private static final String PROMPT_NOT_AVAILABLE = "Prompt not available on DBus";
    private static final DBusConnection connection;

    private final List<CompletedHandler> completedHandlers = new CopyOnWriteArrayList<>();
    private final DBusPath path;

    static {
        connection = ConnectionManager.getInstance().getConnection();
    }

    public Prompt(DBusPath path) {
        super(Static.Service.SECRETS, path.getPath(), org.purejava.secret.interfaces.Prompt.class);

        this.path = path;

        try {
            this.remote = Prompt.connection.getRemoteObject(Static.Service.SECRETS,
                    path.getPath(),
                    org.purejava.secret.interfaces.Prompt.class);

            Prompt.connection.addSigHandler(org.purejava.secret.interfaces.Prompt.Completed.class, this::notifyOnCompleted);

        } catch (DBusException e) {
            LOG.error(e.toString(), e.getCause());
        }
    }

    @Override
    protected String getUnavailableMessage() {
        return PROMPT_NOT_AVAILABLE;
    }

    /**
     * Perform the prompt.
     *
     * @param window_id Platform specific window handle to use for showing the prompt.
     */
    public void prompt(String window_id) {
        if (Util.varIsEmpty(window_id)) {
            LOG.error("Cannot prompt as required window_id is missing");
            return;
        }
        dBusCall("Prompt", getDBusPath(), () -> {
            remote.Prompt(window_id);
            return null;
        });
    }

    /**
     * Dismiss the prompt.
     */
    public void dismiss() {
        dBusCall("Dismiss", getDBusPath(), () -> {
            remote.Dismiss();
            return null;
        });
    }

    /**
     * @return The DBusPath of the prompt.
     */
    public String getDBusPath() {
        return path.getPath();
    }

    private void notifyOnCompleted(org.purejava.secret.interfaces.Prompt.Completed signal) {
        completedHandlers.forEach(handler -> handler.onCompleted(signal.dismissed, signal.result));
    }

    public void addCompletedHandler(CompletedHandler handler) {
        completedHandlers.add(handler);
    }

    public void removeCompletedHandler(CompletedHandler handler) {
        completedHandlers.remove(handler);
    }
}
