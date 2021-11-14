package com.rictacius.customShop.config.migration;

public class MigrationException extends Exception {
    MigrationException(String message) {
        super(message);
    }

    MigrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
