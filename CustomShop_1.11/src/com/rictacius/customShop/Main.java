package com.rictacius.customShop;

import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class Main extends JavaPlugin implements Listener {
    PluginDescriptionFile pdfFile = getDescription();

    static Main pl;
    static String cur;
    boolean updateFound;
    final int CONFIG_VERSION = 2;

    public void onEnable() {
        pl = this;
        Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Config...."), ChatColor.YELLOW);
        createFiles();
        cur = getConfig().getString("currency");
        setupEconomy();
        Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Events...."), ChatColor.YELLOW);
        registerEvents();
        ChestShops.loadShops();
        Methods.sendColoredMessage(this, ChatColor.AQUA, ("Checking for updates...."), ChatColor.YELLOW);
        updateFound = Updater.check();
        if (updateFound) {
            Methods.sendColoredMessage(this, ChatColor.AQUA, ("Found update (v" + Updater.newVersion + ")."),
                    ChatColor.GREEN);
        } else {
            if (Updater.devBuild) {
                Methods.sendColoredMessage(this, ChatColor.AQUA, ("You are running a dev build of CustomShop."), ChatColor.GREEN);
            } else {
                Methods.sendColoredMessage(this, ChatColor.AQUA, ("CustomShop is up to date."), ChatColor.GREEN);
            }
        }
        Methods.sendColoredMessage(this, ChatColor.AQUA,
                (pdfFile.getName() + " has been enabled! (V." + pdfFile.getVersion() + ")"), ChatColor.GREEN);
    }

    public void onDisable() {
        ChestShops.saveShops();
        ChestShops.shutdownShops();
        Methods.sendColoredMessage(this, ChatColor.AQUA,
                (pdfFile.getName() + " has been disabled! (V." + pdfFile.getVersion() + ")"), ChatColor.YELLOW);
    }

    public void registerEvents() {
        try {
            PluginManager pm = getServer().getPluginManager();

            pm.registerEvents(new Shops(), this);
            pm.registerEvents(new Sell(), this);
            pm.registerEvents(new ServerChecker(), this);
            pm.registerEvents(new ChestShops(), this);
            pm.registerEvents(new Commands(), this);
        } catch (Exception e) {
            Methods.sendColoredMessage(this, ChatColor.AQUA, ("Error while registering events!"), ChatColor.RED);
            Methods.sendColoredMessage(this, ChatColor.AQUA, ("Trace:"), ChatColor.RED);
            e.printStackTrace();
        }
        Methods.sendColoredMessage(this, ChatColor.AQUA, ("Events successful registered!"), ChatColor.GREEN);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (updateFound && PermCheck.senderHasAccess(e.getPlayer(), config.getString("admin-perm"))) {
            e.getPlayer().spigot().sendMessage(new TextComponent(
                    ChatColor.translateAlternateColorCodes('&', config.getString("update-found"))));
        }
    }

    public static Economy economy = null;

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    private File shopsFile;
    public static FileConfiguration config, shops;

    public static FileConfiguration getShopsConfig() {
        return shops;
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    public int reloadAllConfigFiles() {
        int errors = 0;
        ArrayList<String> errorFiles = new ArrayList<String>();
        String file;
        ArrayList<StackTraceElement[]> traces = new ArrayList<StackTraceElement[]>();
        StackTraceElement[] trace;
        try {
            this.reloadConfig();
        } catch (Exception e) {
            errors++;
            trace = e.getStackTrace();
            traces.add(trace);
            file = "Main Config File";
            errorFiles.add(file);
        }
        try {
            shops = YamlConfiguration.loadConfiguration(shopsFile);
        } catch (Exception e) {
            errors++;
            trace = e.getStackTrace();
            traces.add(trace);
            file = "Shops Config File";
            errorFiles.add(file);
        }
        cur = getConfig().getString("currency");
        if (errors > 0) {
            Methods.sendColoredMessage(this, ChatColor.GOLD, ("Could not reload all config files!"), ChatColor.RED);
            Methods.sendColoredMessage(this, ChatColor.GOLD, ("The following files generated erros:"), ChatColor.RED);
            for (String fileName : errorFiles) {
                Methods.sendColoredMessage(this, ChatColor.GOLD, (ChatColor.GRAY + " - " + ChatColor.RED + fileName),
                        ChatColor.RED);
            }
            Methods.sendColoredMessage(this, ChatColor.GOLD, ("Trace(s):"), ChatColor.RED);
            for (StackTraceElement[] currentTrace : traces) {
                int i = 0;
                Methods.sendColoredMessage(this, ChatColor.GOLD,
                        (ChatColor.GRAY + "* " + ChatColor.RED + errorFiles.get(i)), ChatColor.RED);
                for (StackTraceElement printTrace : currentTrace) {
                    Methods.sendColoredMessage(this, ChatColor.GOLD, (printTrace.toString()), ChatColor.RED);
                }
                i++;
            }
        }
        return errors;
    }

    public void saveAllConfigFiles() {
        saveConfig();
        try {
            getShopsConfig().save(shopsFile);
        } catch (Exception ex) {
            Methods.sendColoredMessage(this, ChatColor.GOLD, ("Could not save config to " + shopsFile), ChatColor.RED);
            Methods.sendColoredMessage(this, ChatColor.GOLD, ("Trace:"), ChatColor.RED);
            ex.printStackTrace();
        }
    }

    public void saveShopsFile() {
        try {
            getShopsConfig().save(shopsFile);
        } catch (Exception ex) {
            Methods.sendColoredMessage(this, ChatColor.GOLD, ("Could not save config to " + shopsFile), ChatColor.RED);
            Methods.sendColoredMessage(this, ChatColor.GOLD, ("Trace:"), ChatColor.RED);
            ex.printStackTrace();
        }
    }

    private void createFiles() {
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            shopsFile = new File(getDataFolder(), "shops.yml");

            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                writeNewConfig("config.yml", configFile);
            }
            if (!shopsFile.exists()) {
                shopsFile.getParentFile().mkdirs();
                writeNewConfig("shops.yml", shopsFile);
            }

            config = new YamlConfiguration();
            shops = new YamlConfiguration();
            try {
                config.load(configFile);
                shops.load(shopsFile);
            } catch (Exception e) {
                Methods.sendColoredMessage(this, ChatColor.LIGHT_PURPLE, ("Could not load config!"),
                        ChatColor.RED);
                e.printStackTrace();
            }
            if (config.getString("config-version") == null) {
                FileUtils.copyFile(configFile, new File(getDataFolder(), checkOldConfigFileName("config", 0)));
                writeNewConfig("config.yml", configFile);
                FileUtils.copyFile(configFile, new File(getDataFolder(), checkOldConfigFileName("shops", 0)));
                writeNewConfig("shops.yml", configFile);
                try {
                    config.load(configFile);
                    shops.load(shopsFile);
                } catch (Exception e) {
                    Methods.sendColoredMessage(this, ChatColor.LIGHT_PURPLE, ("Could not load config!"),
                            ChatColor.RED);
                    e.printStackTrace();
                }
            } else if (Integer.parseInt(config.getString("config-version")) < CONFIG_VERSION) {
                FileUtils.copyFile(configFile, new File(getDataFolder(), checkOldConfigFileName("config", 0)));
                writeNewConfig("config.yml", configFile);
                FileUtils.copyFile(configFile, new File(getDataFolder(), checkOldConfigFileName("shops", 0)));
                writeNewConfig("shops.yml", configFile);
                try {
                    config.load(configFile);
                    shops.load(shopsFile);
                } catch (Exception e) {
                    Methods.sendColoredMessage(this, ChatColor.LIGHT_PURPLE, ("Could not load config!"),
                            ChatColor.RED);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Methods.sendColoredMessage(this, ChatColor.LIGHT_PURPLE, ("Error while registering config!"),
                    ChatColor.RED);
            e.printStackTrace();
        }
        Methods.sendColoredMessage(this, ChatColor.AQUA, ("Config successful registered!"), ChatColor.GREEN);
    }

    static String replaceRegex(String original, Integer amount, Double price) {
        original = ChatColor.translateAlternateColorCodes('&', original);
        original = original.replaceAll("<amount>", "" + amount).replaceAll("<price>", "" + price)
                .replaceAll("<currency>", Matcher.quoteReplacement(Main.cur));
        return original;
    }

    private String checkOldConfigFileName(String base, int value) {
        if (value == 0) {
            File file = new File(getDataFolder(), "old-" + base + ".yml");
            if (file.exists()) {
                return checkOldConfigFileName(base, value + 1);
            } else {
                return "old-" + base + ".yml";
            }
        } else {
            File file = new File(getDataFolder(), "old-" + base + value + ".yml");
            if (file.exists()) {
                return checkOldConfigFileName(base, value + 1);
            } else {
                return "old-" + base + value + ".yml";
            }
        }
    }

    private void writeNewConfig(String source, File destFile) {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream(source)));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            lines.clear();
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
            Files.write(destFile.toPath(), lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}