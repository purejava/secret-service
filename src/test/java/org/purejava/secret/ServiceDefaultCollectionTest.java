package org.purejava.secret;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceDefaultCollectionTest {
    private Context context;

    @BeforeEach
    void beforeEach() {
        context = new Context();
        context.ensureService();
    }

    @Test
    @DisplayName("Test for default connection")
        // this collection should be created with an empty password
    void checkDefaultConnection() {
        assertTrue(context.service.hasDefaultCollection());
    }
}
