package com.rictacius.customShop;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public final class Util {

	public static void consoleLog(String message) {
		consoleLog(ChatColor.AQUA, message, ChatColor.GREEN);
	}

	public static void consoleLog(String message, ChatColor messageColour) {
		consoleLog(ChatColor.AQUA, message, messageColour);
	}

	public static void consoleLog(ChatColor prefixColour, String message, ChatColor messageColour) {
		ConsoleCommandSender c = Main.plugin.getServer().getConsoleSender();
		c.sendMessage(
				prefixColour + "[" + Main.plugin.getDescription().getName() + "] " +
						messageColour + new TextComponent(message).getText()
		);
	}

}
