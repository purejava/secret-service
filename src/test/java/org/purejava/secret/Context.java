package org.purejava.secret;

import org.purejava.secret.api.Service;

public class Context {

    public Service service = null;

    public void ensureService() {
        service = new Service();
    }

}
