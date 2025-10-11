package org.purejava.secret.api;

import org.freedesktop.dbus.DBusPath;

public class Prompt extends org.purejava.secret.impl.Prompt {

    public Prompt(DBusPath path) {
        super(path);
    }

    /**
     * Perform the prompt.
     *
     * @param window_id Platform specific window handle to use for showing the prompt.
     */
    public void prompt(String window_id) {
        prompt.Prompt(window_id);
    }

    /**
     * Dismiss the prompt.
     */
    public void dismiss() {
        prompt.Dismiss();
    }
}
