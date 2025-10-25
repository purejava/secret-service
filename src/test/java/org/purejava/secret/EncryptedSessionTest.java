package org.purejava.secret;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.purejava.secret.api.EncryptedSession;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncryptedSessionTest {

    @Test
    @DisplayName("Establish an encrypted session")
    void establishEncryptedSession() {
        EncryptedSession session = new EncryptedSession();
        assertTrue(session.setupEncryptedSession());
    }
}
