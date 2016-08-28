package com.rictacius.chatMute;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	PluginDescriptionFile pdfFile = getDescription();
	Logger logger = getLogger();

	public void onEnable() {
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Commands...."), ChatColor.YELLOW);
		registerCommands();
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Events...."), ChatColor.YELLOW);
		registerEvents();
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Config...."), ChatColor.YELLOW);
		createFiles();
		Methods.sendColoredMessage(this, ChatColor.AQUA,
				(pdfFile.getName() + " has been enabled! (V." + pdfFile.getVersion() + ")"), ChatColor.GREEN);
	}

	public void onDisable() {

		Methods.sendColoredMessage(this, ChatColor.AQUA,
				(pdfFile.getName() + " has been disabled! (V." + pdfFile.getVersion() + ")"), ChatColor.YELLOW);
	}

	public void registerCommands() {
		try {
			getCommand("chatmute").setExecutor(new ChatMute(this));
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

			pm.registerEvents(new ChatMute(this), this);
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

	public static Plugin getPlugin() {
		return Bukkit.getServer().getPluginManager().getPlugin("Exteria_Utilities");
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private File configf;
	private FileConfiguration config;

	private void createFiles() {
		try {
			configf = new File(getDataFolder(), "config.yml");

			if (!configf.exists()) {
				configf.getParentFile().mkdirs();
				saveResource("config.yml", false);
			}

			config = new YamlConfiguration();
			try {
				config.load(configf);
			} catch (Exception e) {
				Methods.sendColoredMessage(this, ChatColor.LIGHT_PURPLE, ("Error while registering config!"),
						ChatColor.RED);
				e.printStackTrace();
			}
		} catch (Exception e) {
			Methods.sendColoredMessage(this, ChatColor.LIGHT_PURPLE, ("Error while registering config!"),
					ChatColor.RED);
			e.printStackTrace();
		}
	}
}
