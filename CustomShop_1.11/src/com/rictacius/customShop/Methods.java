package com.rictacius.customShop;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public final class Methods {

	public static void sendColoredMessage(Main plugin, ChatColor ecolor, String message, ChatColor color) {
		ecolor = ChatColor.LIGHT_PURPLE;
		ConsoleCommandSender c = plugin.getServer().getConsoleSender();
		c.sendMessage(
				ecolor + "[" + plugin.getDescription().getName() + "] " + color + new TextComponent(message).getText());

	}

	public static void sendColoredMessage(Main plugin, net.md_5.bungee.api.ChatColor ecolor, String message,
			net.md_5.bungee.api.ChatColor color) {
		ecolor = net.md_5.bungee.api.ChatColor.LIGHT_PURPLE;
		ConsoleCommandSender c = plugin.getServer().getConsoleSender();
		c.sendMessage(
				ecolor + "[" + plugin.getDescription().getName() + "] " + color + new TextComponent(message).getText());

	}

	public static void sendColoredConsoleMessage(ChatColor ecolor, String message) {
		ConsoleCommandSender c = Bukkit.getServer().getConsoleSender();
		c.sendMessage(ecolor + message);

	}

	public static void sendColoredConsoleMessage(net.md_5.bungee.api.ChatColor ecolor, String message) {
		ConsoleCommandSender c = Bukkit.getServer().getConsoleSender();
		c.sendMessage(ecolor + message);
	}
}
