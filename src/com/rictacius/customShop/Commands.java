package com.rictacius.customShop;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class Commands implements CommandExecutor {
	private Main plugin;

	public Commands(Main pl) {
		plugin = pl;
	}

	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Command only useable by players!").getText());
			return true;
		}
		if (!(PermCheck.senderHasAccess(sender, plugin.getConfig().getString("shop-perm")))) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You Shall not pass!").getText());
			return true;
		}
		Player p = (Player) sender;
		if (args.length < 1) {
			p.openInventory(Shops.getShop("customshop{main-inventory($$$)}"));
			return true;
		}
		if (args[0].equalsIgnoreCase("help")) {
			p.sendMessage("");
			p.sendMessage(ChatColor.BLUE + "Custom Shop v" + plugin.getDescription().getVersion());
			p.sendMessage(ChatColor.YELLOW + "/shop");
			p.sendMessage(ChatColor.YELLOW + "/shop help");
			p.sendMessage(ChatColor.YELLOW + "/shop reload");
			p.sendMessage(ChatColor.YELLOW + "/shop sell");
			p.sendMessage("");
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!PermCheck.senderHasAccess(sender, plugin.getConfig().getString("admin-perm"))) {
				sender.sendMessage(new TextComponent(ChatColor.RED + "You Shall not pass!").getText());
				return true;
			}
			try {
				plugin.reloadAllConfigFiles();
				Shops.loadShops();

			} catch (Exception e) {
				p.sendMessage(new TextComponent(ChatColor.RED + "Error reloading config please check config for more information").getText());
				return true;
			}
			p.sendMessage(new TextComponent(ChatColor.GREEN + "Plugin reloaded!").getText());
			p.sendMessage(ChatColor.GRAY + "Plugin built by RictAcius");
		} else if (args[0].equalsIgnoreCase("sell")) {
			Inventory inv = Bukkit.createInventory(null, 36, ChatColor.RED + "CSell");
			p.openInventory(inv);
		}
		return true;
	}
}
