package com.rictacius.customShop.config;

import com.rictacius.customShop.Main;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public abstract class Config {
    private String fileName;
    private File configFile;
    protected FileConfiguration file;

    protected Config(File parentFolder, String fileName) throws ConfigFileException {
        this.fileName = fileName;
        configFile = new File(parentFolder, fileName);
        if (!configFile.exists()) {
            boolean folderCreated = parentFolder.mkdirs();
            if (!folderCreated) {
                throw new ConfigFileException("Failed to create the config file's parent folder.");
            }
            writeNewConfig(fileName, configFile);
        }
        reload();
    }

    private void writeNewConfig(String source, File destFile) {
        List<String> sourceLines = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream(source)));
            String line;
            while ((line = reader.readLine()) != null) {
                sourceLines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            sourceLines.clear();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Files.write(destFile.toPath(), sourceLines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() throws ConfigFileException {
        file = loadFile();
        onConfigFileLoaded();
    }

    private FileConfiguration loadFile() throws ConfigFileException {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new ConfigFileException("Failed to load config yaml", e);
        }
        return config;
    }

    protected abstract void onConfigFileLoaded() throws ConfigFileException;

    public String getFileName() {
        return fileName;
    }

    public FileConfiguration getUnderlyingFileConfiguration() {
        return file;
    }
}
