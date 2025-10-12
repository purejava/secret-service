package org.purejava.secret;

import org.purejava.secret.api.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {

    private static final Logger LOG = LoggerFactory.getLogger(Context.class);

    public Service service = null;

    public void ensureService() {
        service = new Service();
    }

}
