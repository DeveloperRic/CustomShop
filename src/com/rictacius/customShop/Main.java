package com.rictacius.customShop;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener {
	PluginDescriptionFile pdfFile = getDescription();
	Logger logger = getLogger();

	public static Main pl;
	public static String cur;

	public void onEnable() {
		pl = this;
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Config...."), ChatColor.YELLOW);
		createFiles();
		cur = getConfig().getString("currency");
		setupEconomy();
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Commands...."), ChatColor.YELLOW);
		registerCommands();
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Events...."), ChatColor.YELLOW);
		registerEvents();
		ChestShops.loadShops();
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Checking for updates...."), ChatColor.YELLOW);
		boolean update = Updater.check();
		if (update) {
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Found update (v" + Updater.newVersion + ")."),
					ChatColor.GREEN);
			if (config.getString("auto-update") == null) {
				config.set("auto-update", true);
				saveConfig();
			}
			if (Boolean.parseBoolean(config.getString("auto-update"))) {
				Methods.sendColoredMessage(this, ChatColor.AQUA, ("Auto-updating CustomShop..."), ChatColor.YELLOW);
				Updater.download();
				Methods.sendColoredMessage(this, ChatColor.AQUA,
						("Downloaded update (v" + Updater.newVersion + ") Please restart your server to install it!"),
						ChatColor.GREEN);
			}
		} else {
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("CustomShop is up to date."), ChatColor.GREEN);
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

	public void registerCommands() {
		try {
			getCommand("shop").setExecutor(new Commands(this));
			getCommand("sell").setExecutor(new Sell(this));
		} catch (Exception e) {
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Error while registering commands!"), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Trace:"), ChatColor.RED);
			e.printStackTrace();
		}
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Commands successfuly registered!"), ChatColor.LIGHT_PURPLE);
	}

	public void registerEvents() {
		try {
			PluginManager pm = getServer().getPluginManager();

			pm.registerEvents(new Shops(), this);
			pm.registerEvents(new Sell(this), this);
			pm.registerEvents(new ServerChecker(), this);
			pm.registerEvents(new ChestShops(), this);
		} catch (Exception e) {
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Error while registering events!"), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Trace:"), ChatColor.RED);
			e.printStackTrace();
		}
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Events successfuly registered!"), ChatColor.LIGHT_PURPLE);
	}

	public void registerConfig() {
		try {
			getConfig().options().copyDefaults(true);
			saveConfig();

		} catch (Exception e) {
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Error while registering config!"), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Trace:"), ChatColor.RED);
			e.printStackTrace();
		}
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Config successfuly registered!"), ChatColor.LIGHT_PURPLE);
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

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private File configf, shopsf;
	public static FileConfiguration config, shops;

	public FileConfiguration getShopsConfig() {
		return shops;
	}

	@Override
	public FileConfiguration getConfig() {
		return config;
	}

	public int reloadAllConfigFiles() {
		int errors = 0;
		ArrayList<String> errorFiles = new ArrayList<String>();
		String file = "";
		ArrayList<StackTraceElement[]> traces = new ArrayList<StackTraceElement[]>();
		StackTraceElement[] trace = null;
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
			shops = YamlConfiguration.loadConfiguration(shopsf);
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
			getShopsConfig().save(shopsf);
		} catch (Exception ex) {
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Could not save config to " + shopsf), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Trace:"), ChatColor.RED);
			ex.printStackTrace();
		}
	}

	public void saveShopsFile() {
		try {
			getShopsConfig().save(shopsf);
		} catch (Exception ex) {
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Could not save config to " + shopsf), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Trace:"), ChatColor.RED);
			ex.printStackTrace();
		}
	}

	private void createFiles() {
		try {
			configf = new File(getDataFolder(), "config.yml");
			shopsf = new File(getDataFolder(), "shops.yml");

			if (!configf.exists()) {
				configf.getParentFile().mkdirs();
				saveResource("config.yml", false);
			}
			if (!shopsf.exists()) {
				shopsf.getParentFile().mkdirs();
				saveResource("shops.yml", false);
			}

			config = new YamlConfiguration();
			shops = new YamlConfiguration();
			try {
				config.load(configf);
				shops.load(shopsf);
			} catch (Exception e) {
				Methods.sendColoredMessage(this, ChatColor.LIGHT_PURPLE, ("Error while registering config!"),
						ChatColor.RED);
				e.printStackTrace();
			}
			config.addDefault("currency", '$');
			config.addDefault("shop-perm", "customshop.use");
			config.addDefault("admin-perm", "customshop.admin");
			config.addDefault("chestshop-perm", "customshop.chestshop");
			config.addDefault("auto-update", true);
		} catch (Exception e) {
			Methods.sendColoredMessage(this, ChatColor.LIGHT_PURPLE, ("Error while registering config!"),
					ChatColor.RED);
			e.printStackTrace();
		}
	}
}