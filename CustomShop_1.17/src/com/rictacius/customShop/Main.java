package com.rictacius.customShop;

import com.rictacius.customShop.config.Config;
import com.rictacius.customShop.config.ConfigFileException;
import com.rictacius.customShop.config.PluginConfig;
import com.rictacius.customShop.config.ShopsConfig;
import com.rictacius.customShop.config.migration.Migration;
import com.rictacius.customShop.config.migration.MigrationException;
import com.rictacius.customShop.shop.Sell;
import com.rictacius.customShop.shop.Shops;
import com.rictacius.customShop.updater.Updater;
import com.rictacius.customShop.updater.UpdaterException;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin implements Listener {
    public static Main plugin;

    private static PluginConfig config;
    private static ShopsConfig shopsConfig;
    private static Shops shops;
    private static Sell sell;
    private static Economy economy;

    private final PluginDescriptionFile pdfFile = getDescription();
    private boolean updateFound;

    public void onEnable() {
        plugin = this;
        registerConfig();
        Util.consoleLog("Registering Events....", ChatColor.YELLOW);
        registerEvents();
        Util.consoleLog("Checking for updates....", ChatColor.YELLOW);
        checkForUpdates();
        Util.consoleLog(pdfFile.getName() + " has been enabled! (V." + pdfFile.getVersion() + ")");
    }

    private void registerConfig() {
        updateConfig();
        createFiles();
        setupEconomy();
        shops = new Shops();
        sell = new Sell();
    }

    private void updateConfig() {
        try {
            Util.consoleLog("Updating Config....", ChatColor.YELLOW);
            Migration migration = new Migration(getDataFolder());
            migration.migrate();
        } catch (MigrationException e) {
            throw new RuntimeException("Failed to load CustomShop: Fatal error while updating config!", e);
        }
        Util.consoleLog("Config updated successfully");
    }

    private void createFiles() {
        try {
            Util.consoleLog("Registering Config....", ChatColor.YELLOW);
            File pluginDataFolder = getDataFolder();
            config = new PluginConfig(pluginDataFolder);
            shopsConfig = new ShopsConfig(pluginDataFolder);
        } catch (ConfigFileException e) {
            throw new RuntimeException("Failed to load CustomShop: Fatal error while registering config!", e);
        }
        Util.consoleLog("Config successful registered!");
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider =
                getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider == null) {
            throw new RuntimeException("Failed to load CustomShop: Vault Economy service is required");
        }
        economy = economyProvider.getProvider();
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(shops, this);
        pm.registerEvents(sell, this);
        pm.registerEvents(new ServerChecker(), this);
        pm.registerEvents(new Commands(), this);
        Util.consoleLog("Events successful registered!");
    }

    private void checkForUpdates() {
        try {
            Updater updater = new Updater();
            updateFound = updater.isNewVersionAvailable();
            if (updater.isDevBuild()) {
                Util.consoleLog("You are running a dev build of CustomShop.");
            } else if (updateFound) {
                Util.consoleLog("Found update (new-version = v" + updater.getNewVersionName() + ").");
            } else {
                Util.consoleLog("CustomShop is up to date.");
            }
        } catch (UpdaterException e) {
            Util.consoleLog("Could not check for updates", ChatColor.RED);
            updateFound = false;
            e.printStackTrace();
        }
    }

    public void onDisable() {
        Util.consoleLog(pdfFile.getName() + " has been disabled! (V." + pdfFile.getVersion() + ")", ChatColor.YELLOW);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (updateFound && PermCheck.senderHasAccess(e.getPlayer(), config.getAdminPermission())) {
            e.getPlayer().spigot().sendMessage(new TextComponent(
                    ChatColor.translateAlternateColorCodes('&', config.getUpdateFoundMessage())));
        }
    }

    void reloadAllConfigFiles() {
        Config[] configFiles = {config, shopsConfig};
        for (Config configFile : configFiles) {
            try {
                configFile.reload();
            } catch (ConfigFileException e) {
                Util.consoleLog(ChatColor.GOLD, "Could not reload " + configFile.getFileName(), ChatColor.RED);
                e.printStackTrace();
            }
        }
        shops.loadShops();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public FileConfiguration getConfig() {
        return getPluginConfig().getUnderlyingFileConfiguration();
    }

    public static PluginConfig getPluginConfig() {
        return config;
    }

    public static ShopsConfig getShopsConfig() {
        return shopsConfig;
    }

    public static Shops getShops() {
        return shops;
    }

    public static Sell getSell() {
        return sell;
    }

    public static Economy getEconomy() {
        return economy;
    }
}