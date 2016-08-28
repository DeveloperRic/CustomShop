package com.rictacius.chatMute;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatMute implements CommandExecutor, Listener {
	private static Main plugin;

	public ChatMute(Main pl) {
		plugin = pl;
	}

	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Command only useable by players!");
			return true;
		}
		Player p = (Player) sender;
		if (!(sender.hasPermission(plugin.getConfig().getString("mute-perm")))) {
			p.sendMessage(ChatColor.RED + "You shall not pass!");
			return true;
		}
		if (args.length < 1) {
			p.sendMessage(ChatColor.GREEN + "Chat Mute");
			p.sendMessage(ChatColor.GOLD + "/chatmute mute");
			p.sendMessage(ChatColor.GOLD + "/chatmute bypass <player>");
			p.sendMessage(ChatColor.GOLD + "/chatmute reload");
			p.sendMessage(ChatColor.DARK_GRAY + "Plugin built by RictAcius :)");
			return true;
		} else if (args[0].equalsIgnoreCase("mute")) {
			if (Boolean.parseBoolean(plugin.getConfig().getString("mute-active")) == false) {
				plugin.getConfig().set("mute-active", true);
				plugin.saveConfig();
				for (Player online : Bukkit.getOnlinePlayers()) {
					for (String message : plugin.getConfig().getStringList("activate-messages")) {
						try {
							online.sendMessage(ChatColor.translateAlternateColorCodes('&',
									message.replaceAll("%player%", p.getName())));
						} catch (Exception e) {
							online.sendMessage(message.replaceAll("%player%", p.getName()));
						}
					}
					if (online.hasPermission(plugin.getConfig().getString("bypass-perm"))) {
						online.sendMessage(ChatColor.GREEN + "You have been allowed to bypass this chat mute!");
						online.sendMessage(ChatColor.GREEN + "You will be able to chat as normal but "
								+ ChatColor.UNDERLINE + "only others like you will see your message!");
						online.sendMessage(ChatColor.GREEN + "To send your message to everyone prefix it with a "
								+ ChatColor.GOLD + "'>'");
					}
				}
			} else {
				plugin.getConfig().set("mute-active", false);
				plugin.saveConfig();
				for (Player online : Bukkit.getOnlinePlayers()) {
					online.sendMessage(ChatColor.GREEN + "Chat is no longer muted!");
				}
			}
		} else if (args[0].equalsIgnoreCase("bypass")) {
			Player dp = null;
			try {
				dp = Bukkit.getPlayer(args[1]);
			} catch (Exception e) {
				p.sendMessage(ChatColor.RED + "Player is not online!");
				return true;
			}
			String command = plugin.getConfig().getString("perm-add-command").replaceAll("%player%", dp.getName())
					.replaceAll("%perm%", plugin.getConfig().getString("bypass-perm"));
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
			p.sendMessage(ChatColor.GREEN + "Added " + dp.getName() + " to bypass players.");
		} else if (args[0].equalsIgnoreCase("reload")) {
			plugin.reloadConfig();
			p.sendMessage(ChatColor.GREEN + "Plugin reloaded!");
			p.sendMessage(ChatColor.DARK_GRAY + "Plugin built by RictAcius :)");
		} else {
			p.sendMessage(ChatColor.GREEN + "Chat Mute");
			p.sendMessage(ChatColor.GOLD + "/chatmute mute");
			p.sendMessage(ChatColor.GOLD + "/chatmute bypass <player>");
			p.sendMessage(ChatColor.GOLD + "/chatmute reload");
			p.sendMessage(ChatColor.DARK_GRAY + "Plugin built by RictAcius :)");
		}
		return true;
	}

	@EventHandler
	public static void onChat(AsyncPlayerChatEvent event) {
		if (Boolean.parseBoolean(plugin.getConfig().getString("mute-active")) == false)
			return;
		Player p = event.getPlayer();
		if (!(p.hasPermission(plugin.getConfig().getString("bypass-perm")))) {
			try {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&',
						plugin.getConfig().getString("mute-message").replaceAll("%player%", p.getName())));
			} catch (Exception e) {
				p.sendMessage(plugin.getConfig().getString("mute-message").replaceAll("%player%", p.getName()));
			}
			event.setCancelled(true);
			return;
		}
		String message = event.getMessage();
		if (message.charAt(0) == '>') {
			message = message.substring(1);
			event.setMessage(message);
			return;
		} else {
			event.setCancelled(true);
			message = ChatColor.GOLD + "ChatMute: " + p.getName() + " [" + ChatColor.DARK_GREEN + message
					+ ChatColor.GOLD + "]";
			for (Player o : p.getWorld().getPlayers()) {
				o.sendMessage(message);
			}
		}
	}

}
