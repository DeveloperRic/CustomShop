package com.rictacius.customShop.updater;

public class UpdaterException extends Exception {
    UpdaterException(String message) {
        super(message);
    }

    UpdaterException(String message, Throwable cause) {
        super(message, cause);
    }
}
