package com.rictacius.customShop;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class Sell implements Listener {

    public ArrayList<Number> checkShops(Inventory inv, Player p, ItemStack i, int id, int typeData, int slot) {
        ArrayList<Number> send = new ArrayList<>();
        for (String shop : Main.getShopsConfig().getConfigurationSection("shops").getKeys(false)) {
            if (!PermCheck.hasAccessPerm(p, Main.getShopsConfig().getString("shops." + shop + ".permission"))) {
                continue;
            }
            for (String item : Main.getShopsConfig().getConfigurationSection("shops." + shop + ".items")
                    .getKeys(false)) {
                String[] data = Main.getShopsConfig().getString("shops." + shop + ".items." + item).split(",");
                int sellId = Integer.parseInt(data[0].split(":")[0]);
                int sellTypeData = Integer.parseInt(data[0].split(":")[1]);
                if (sellId == id && typeData == sellTypeData) {
                    double cost;
                    try {
                        cost = Double.parseDouble(data[4]) / Double.parseDouble(data[3]);
                    } catch (Exception e) {
                        continue;
                    }
                    cost = cost * i.getAmount();
                    inv.setItem(slot, null);
                    send.add(cost);
                    send.add(i.getAmount());
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
        if (inv == null) {
            return;
        }
        if (inv.getName() == null) {
            return;
        }
        if (!inv.getName().equals(ChatColor.translateAlternateColorCodes('&', Main.config.getString("sell-inventory-title")))) {
            return;
        }
        Inventory backup = inv;
        List<ItemStack> errorItems = new ArrayList<>();
        Player p = (Player) event.getPlayer();
        int totalItemStacks = 0;
        int itemStacksSold = 0;
        int piecesSold = 0;
        double moneySent = 0;
        for (int n = 0; n < inv.getSize(); n++) {
            ItemStack i = inv.getItem(n);
            if (i == null) {
                continue;
            }
            totalItemStacks++;
            int id = Integer.parseInt(String.valueOf(i.getTypeId()));
            int typeData = i.getDurability();
            ArrayList<Number> shopsSearchResult = checkShops(inv, p, i, id, typeData, n);
            if (shopsSearchResult.size() == 2) {
                Main.economy.depositPlayer(p, shopsSearchResult.get(0).doubleValue());
                moneySent += shopsSearchResult.get(0).doubleValue();
                piecesSold += shopsSearchResult.get(1).intValue();
                inv.setItem(n, null);
                itemStacksSold++;
            } else {
                errorItems.add(i);
            }
        }
        if (itemStacksSold < totalItemStacks) {
            p.spigot().sendMessage(new TextComponent(ChatColor.RED + "Some items could not be sold"));
            if (totalItemStacks < getSize(inv)) {
                p.sendMessage(ChatColor.DARK_RED + "A Fatal error occured! Please report this! Restoring items...");
                for (int n = 0; n < backup.getSize(); n++) {
                    ItemStack i = backup.getItem(n);
                    if (i == null) {
                        continue;
                    }
                    p.getInventory().addItem(i);
                }
            } else {
                for (ItemStack i : errorItems) {
                    p.getInventory().addItem(i);
                }
            }
        }
        p.sendMessage(Main.replaceRegex(Main.config.getString("items-sold-message"), piecesSold, moneySent));
    }

    public static void onCommand(String command, String[] args, Player player) {
        if (command.equals("sell")) {
            if (!player.hasPermission(Main.config.getString("shop-perm"))) {
                player.spigot().sendMessage(new TextComponent(
                        ChatColor.translateAlternateColorCodes('&', Main.config.getString("no-permission-message"))));
                return;
            }
            if (args.length == 0) {
                Inventory inv = Bukkit.createInventory(null, 36, ChatColor.translateAlternateColorCodes('&', Main.config.getString("sell-inventory-title")));
                player.openInventory(inv);
            }
        }
    }
}
