package com.rictacius.customShop.config.migration;

import java.io.File;

abstract class Migrator {
    private final int inputVersion;
    private final int outputVersion;

    Migrator(int inputVersion, int outputVersion) {
        this.inputVersion = inputVersion;
        this.outputVersion = outputVersion;
    }

    abstract void migrate(File inputFolder, File outputFolder) throws MigrationException;

    public int getInputVersion() {
        return inputVersion;
    }

    public int getOutputVersion() {
        return outputVersion;
    }
}
