package org.purejava.secret;

import java.util.List;

public class ExpectedDesktop {
    protected static boolean isDesktop(String expectedDesktop) {
        var currentDesktop = System.getenv("XDG_CURRENT_DESKTOP");
        if (currentDesktop == null || currentDesktop.isBlank()) {
            return false;
        }

        return List.of(currentDesktop.split(":")).stream()
            .anyMatch(desktop -> desktop.equalsIgnoreCase(expectedDesktop));
    }
}
