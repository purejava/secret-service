package org.purejava.secret;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncryptedSessionTest {

    @Test
    @DisplayName("Establish an encrypted session")
    void establishEncryptedSession() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            InvalidKeyException {
        EncryptedSession session = new EncryptedSession();
        session.initialize();
        var sessionOpened = session.openSession();
        session.generateSessionKey();
        assertTrue(sessionOpened);
    }
}
