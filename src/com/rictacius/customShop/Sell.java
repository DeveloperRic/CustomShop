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

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onExit(InventoryCloseEvent event) {
		Inventory inv = event.getInventory();
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
		int items = 0;
		double sent = 0;
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
				plugin.economy.depositPlayer(p, toAdd.get(0));
				sent += toAdd.get(0);
				items += toAdd.get(1);
				sold++;
			}
		}
		if (sold < size) {
			p.sendMessage(new TextComponent(ChatColor.RED + "Some items were not sold").getText());
			for (int n = 0; n < errorInv.getSize(); n++) {
				ItemStack i = errorInv.getItem(n);
				if (i == null) {
					return;
				}
				p.getInventory().addItem(i);
			}
		} else {
			p.sendMessage(new TextComponent(ChatColor.GREEN + "" + items + " items sold for " + plugin.getConfig().getString("currency")
					+ sent).getText());
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
