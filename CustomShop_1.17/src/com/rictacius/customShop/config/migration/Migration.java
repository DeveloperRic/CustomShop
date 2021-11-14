package com.rictacius.customShop.config.migration;

import com.rictacius.customShop.Util;
import com.rictacius.customShop.config.Config;
import com.rictacius.customShop.config.ConfigFileException;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Migration {
    public static final String CONFIG_FILE_NAME = "config.yml";
    public static final String CONFIG_VERSION_KEY = "config-version";
    public static final String SHOPS_CONFIG_FILE_NAME = "shops.yml";
    private static final int MIN_CONFIG_VERSION_TO_MIGRATE = 2; // cannot automatically migrate if < 2
    public static final int JAR_CONFIG_VERSION = 3;

    private final File dataFolder;
    private final int sourceConfigVersion;

    public Migration(File dataFolder) throws MigrationException {
        this.dataFolder = dataFolder;
        try {
            Config pluginConfigOfUnknownVersion = new Config(dataFolder, CONFIG_FILE_NAME) {
                @Override
                protected void onConfigFileLoaded() {
                }
            };
            sourceConfigVersion = pluginConfigOfUnknownVersion.getUnderlyingFileConfiguration().getInt(CONFIG_VERSION_KEY);
        } catch (ConfigFileException e) {
            throw new MigrationException("Failed to load " + CONFIG_FILE_NAME, e);
        }
    }

    public void migrate() throws MigrationException {
        if (sourceConfigVersion == JAR_CONFIG_VERSION) {
            return; // config version successfully migrated
        }
        Migrator migrator = getMigrator(sourceConfigVersion);
        Util.consoleLog("Migrating config files from v" + migrator.getInputVersion() + " to v" + migrator.getOutputVersion());
        File outputFolder = createOutputFolder(dataFolder, sourceConfigVersion, migrator.getOutputVersion());
        Util.consoleLog("  new files will be staged in " + outputFolder.getName() + " while migration is in progress.");
        File backupFolder = createBackupFolder(dataFolder, outputFolder.getName());
        Util.consoleLog("  Backing up config files to " + backupFolder.getName());
        String[] filesToMigrate = new String[]{CONFIG_FILE_NAME, SHOPS_CONFIG_FILE_NAME};
        backupFiles(dataFolder, backupFolder, filesToMigrate);
        migrator.migrate(dataFolder, outputFolder);
        Util.consoleLog("  Copying new files back to " + dataFolder.getName());
        copyNewFilesToDataFolder(outputFolder, dataFolder, filesToMigrate);
        try {
            FileUtils.deleteDirectory(outputFolder);
        } catch (IOException e) {
            throw new MigrationException("Failed to remove migration staging folder", e);
        }
    }

    private Migrator getMigrator(int sourceConfigVersion) throws MigrationException {
        if (sourceConfigVersion > JAR_CONFIG_VERSION) {
            throw new MigrationException(
                    "Your config version (" + sourceConfigVersion + ") is too high for the JAR_CONFIG_VERSION (" +
                            JAR_CONFIG_VERSION + ") in this version of CustomShop"
            );
        } else if (sourceConfigVersion < MIN_CONFIG_VERSION_TO_MIGRATE) {
            throw new MigrationException(
                    "Your config version (" + sourceConfigVersion + ") is too low to be updated automatically, " +
                            "please manually update your config at least to version " + MIN_CONFIG_VERSION_TO_MIGRATE
            );
        } else if (sourceConfigVersion == 2) {
            return new ConfigVersion2Migrator();
        }
        throw new MigrationException("Unfortunately, your config version (" + sourceConfigVersion + ") cannot be updated automatically");
    }

    private File createOutputFolder(File dataFolder, int sourceConfigVersion, int targetConfigVersion) throws MigrationException {
        return createMigrationFolder(dataFolder, ".migration-v" + sourceConfigVersion + "-to-v" + targetConfigVersion);
    }

    private File createBackupFolder(File dataFolder, String outputFolderName) throws MigrationException {
        return createMigrationFolder(dataFolder, outputFolderName + "--pre-migration-backup");
    }

    private File createMigrationFolder(File parent, String folderName) throws MigrationException {
        File folder = new File(parent, folderName);
        if (folder.exists()) {
            throw new MigrationException(
                    "Files from a previous migration attempt (" + folderName + ") exist. " +
                            "Please check that those files aren't needed, then delete them"
            );
        }
        if (!folder.mkdirs()) {
            throw new MigrationException("Failed to create " + folder.getPath());
        }
        return folder;
    }

    private void backupFiles(File dataFolder, File backupFolder, String[] fileNames) throws MigrationException {
        try {
            copyFiles(dataFolder, backupFolder, fileNames);
        } catch (IOException e) {
            throw new MigrationException("Failed to backup existing files", e);
        }
    }

    private void copyNewFilesToDataFolder(File outputFolder, File dataFolder, String[] fileNames) throws MigrationException {
        try {
            copyFiles(outputFolder, dataFolder, fileNames);
        } catch (IOException e) {
            throw new MigrationException("Failed to copy new files out of " + outputFolder.getName(), e);
        }
    }

    private void copyFiles(File srcFolder, File destFolder, String[] fileNames) throws IOException {
        for (String fileName : fileNames) {
            File srcFile = new File(srcFolder, fileName);
            File destFile = new File(destFolder, fileName);
            FileUtils.copyFile(srcFile, destFile);
        }
    }
}
