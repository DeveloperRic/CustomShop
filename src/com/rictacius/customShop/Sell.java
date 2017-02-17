package com.rictacius.customShop;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class Sell implements CommandExecutor, Listener {
	protected static Main plugin;

	public Sell(Main pl) {
		plugin = pl;
	}

	public ArrayList<Double> checkShops(Inventory inv, Player p, ItemStack i, int id, int iddata, int slot) {
		ArrayList<Double> send = new ArrayList<Double>();
		for (String shop : plugin.getShopsConfig().getConfigurationSection("shops").getKeys(false)) {
			if (!PermCheck.hasAccessPerm(p, plugin.getShopsConfig().getString("shops." + shop + ".permission"))) {
				continue;
			}
			for (String item : plugin.getShopsConfig().getConfigurationSection("shops." + shop + ".items")
					.getKeys(false)) {
				String[] data = plugin.getShopsConfig().getString("shops." + shop + ".items." + item).split(",");
				String[] itemdata = data[0].split(":");
				int sid = Integer.parseInt(itemdata[0]);
				int sdata = Integer.parseInt(itemdata[1]);
				if (sid == id && iddata == sdata) {
					double cost = 0;
					try {
						cost = Double.parseDouble(data[4]) / Double.parseDouble(data[3]);
					} catch (Exception e) {
						continue;
					}
					cost = cost * i.getAmount();
					inv.setItem(slot, null);
					send.add(cost);
					send.add((double) i.getAmount());
					return send;
				}
			}
		}
		return send;
	}

	private int getSize(Inventory inv) {
		int size = 0;
		for (int i = 0; i < inv.getSize(); i++) {
			if (inv.getItem(i) != null) {
				size++;
			}
		}
		return size;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onExit(InventoryCloseEvent event) {
		Inventory inv = event.getInventory();
		Inventory backup = inv;
		Inventory errorInv = inv;
		if (inv == null)
			return;
		if (inv.getName() == null)
			return;
		if (!inv.getName().equals(ChatColor.RED + "CSell"))
			return;
		Player p = (Player) event.getPlayer();
		int size = 0;
		int sold = 0;
		int itemsSold = 0;
		int moneySent = 0;
		for (int n = 0; n < inv.getSize(); n++) {
			ItemStack i = inv.getItem(n);
			if (i == null) {
				continue;
			}
			size++;
			int id = Integer.parseInt(String.valueOf(i.getTypeId()));
			int iddata = i.getDurability();
			ArrayList<Double> toAdd = checkShops(inv, p, i, id, iddata, n);
			if (toAdd.size() == 2) {
				Main.economy.depositPlayer(p, toAdd.get(0));
				moneySent += toAdd.get(0);
				itemsSold += toAdd.get(1);
				errorInv.setItem(n, null);
				sold++;
			}
		}
		if (sold < size) {
			p.spigot().sendMessage(new TextComponent(ChatColor.RED + "Some items were not sold"));
			if (size < getSize(backup)) {
				p.sendMessage(ChatColor.DARK_RED + "A Fatal error occured! Please report this! Restoring items...");
				for (int n = 0; n < backup.getSize(); n++) {
					ItemStack i = backup.getItem(n);
					if (i == null) {
						continue;
					}
					p.getInventory().addItem(i);
				}
			} else {
				for (int n = 0; n < errorInv.getSize(); n++) {
					ItemStack i = errorInv.getItem(n);
					if (i == null) {
						continue;
					}
					p.getInventory().addItem(i);
				}
			}
		} else {
			p.spigot().sendMessage(new TextComponent(ChatColor.GREEN + "" + itemsSold + " items sold for "
					+ plugin.getConfig().getString("currency") + moneySent));
		}
	}

	public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "Command only useable by players!").getText());
			return true;
		}
		if (!sender.hasPermission(plugin.getConfig().getString("shop-perm"))) {
			sender.sendMessage(new TextComponent(ChatColor.RED + "You Shall not pass!").getText());
			return true;
		}
		Player p = (Player) sender;
		if (args.length < 1) {
			Inventory inv = Bukkit.createInventory(null, 36, ChatColor.RED + "CSell");
			p.openInventory(inv);
		}
		return true;
	}
}
