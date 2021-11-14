package com.rictacius.customShop.updater;

import com.rictacius.customShop.Main;
import com.rictacius.customShop.Util;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class Updater {
    private static final String HOST = "http://victorolaitan.xyz/rictacius/download";
    private String newVersionName;
    private boolean devBuild;

    public boolean isNewVersionAvailable() throws UpdaterException {
        newVersionName = null;
        String latestVersionInfo = fetchCustomShopLatestVersionInfo();
        if (latestVersionInfo == null) {
            return false;
        }
        newVersionName = getNewVersionName(latestVersionInfo);
        return newVersionName != null;
    }

    private String fetchCustomShopLatestVersionInfo() throws UpdaterException {
        try {
            URL url = new URL(HOST);
            URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                // line = "<PluginName>:x.x.x"
                if (line.startsWith("CustomShop")) {
                    return line;
                }
            }
        } catch (IOException e) {
            throw new UpdaterException("Failed to fetch update info", e);
        }
        return null;
    }

    private String getNewVersionName(String latestVersionInfo) {
        String currentVersion = Main.plugin.getDescription().getVersion();
        String fetchedVersion = latestVersionInfo.split(":")[1];
        if (fetchedVersion.contains(".")) {
            String[] currentVersionParts = currentVersion.split("\\.");
            String[] fetchedVersionParts = fetchedVersion.split("\\.");
            if (fetchedVersionParts.length != currentVersionParts.length) {
                return fetchedVersion; // assume version on the web is more up-to-date
            }
            for (int i = 0; i < fetchedVersionParts.length; i++) {
                int fetchedVersionPartialNumber = Integer.parseInt(fetchedVersionParts[i]);
                int currentVersionPartialNumber = Integer.parseInt(currentVersionParts[i]);
                devBuild = fetchedVersionPartialNumber < currentVersionPartialNumber;
                if (devBuild) {
                    // running version is newer than version on the web, so we are running a dev build
                    return null;
                }
                if (fetchedVersionPartialNumber > currentVersionPartialNumber) {
                    // version on the web is more up-to-date
                    return fetchedVersion;
                }
            }
        } else if (!fetchedVersion.equals(currentVersion)) {
            return fetchedVersion;
        }
        return null;
    }

    public void downloadNewVersion() throws UpdaterException {
        if (newVersionName == null) {
            throw new UpdaterException("There is no new version to download!");
        }
        try {
            String urlStr = HOST + "/CustomShop/" + newVersionName + "/CustomShop.jar";
            URL website = new URL(urlStr);
            String pathToDownloadTo = "plugins/CustomShop-" + newVersionName + ".jar";
            FileUtils.copyURLToFile(website, new File(pathToDownloadTo), 30000, 30000);
            Util.consoleLog("Successfully downloaded update JAR to " + pathToDownloadTo);
        } catch (IOException e) {
            throw new UpdaterException("Failed to download update");
        }
    }

    public String getNewVersionName() {
        return newVersionName;
    }

    public boolean isDevBuild() {
        return devBuild;
    }
}
