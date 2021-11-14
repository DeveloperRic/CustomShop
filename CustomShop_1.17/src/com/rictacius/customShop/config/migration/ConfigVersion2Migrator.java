package com.rictacius.customShop.config.migration;

import com.rictacius.customShop.config.Config;
import com.rictacius.customShop.config.ConfigFileException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

class ConfigVersion2Migrator extends Migrator {

    ConfigVersion2Migrator() {
        super(2, 3);
    }

    @Override
    void migrate(File inputFolder, File outputFolder) throws MigrationException {
        migrateShops(inputFolder, outputFolder);
        migratePluginConfig(inputFolder, outputFolder);
    }

    private void migrateShops(File inputFolder, File outputFolder) throws MigrationException {
        V2ShopsConfig v2ShopsConfig;
        try {
            v2ShopsConfig = new V2ShopsConfig(inputFolder, "shops.yml");
        } catch (ConfigFileException e) {
            throw new MigrationException("Failed to parse v2 shops.yml", e);
        }
        FileConfiguration v3ShopsConfig = v2ShopsConfig.getV3ShopsConfig();
        File outputFile = new File(outputFolder, "shops.yml");
        try {
            v3ShopsConfig.save(outputFile);
        } catch (IOException e) {
            throw new MigrationException("Failed to save v3 shops.yml", e);
        }
    }

    private void migratePluginConfig(File inputFolder, File outputFolder) throws MigrationException {
        try {
            Config pluginConfig = new Config(inputFolder, Migration.CONFIG_FILE_NAME) {
                @Override
                protected void onConfigFileLoaded() {
                }
            };
            FileConfiguration configYaml = pluginConfig.getUnderlyingFileConfiguration();
            configYaml.set(Migration.CONFIG_VERSION_KEY, getOutputVersion());
            File outputFile = new File(outputFolder, Migration.CONFIG_FILE_NAME);
            configYaml.save(outputFile);
        } catch (ConfigFileException | IOException e) {
            throw new MigrationException("Failed to migrate " + Migration.CONFIG_FILE_NAME, e);
        }
    }

}
