package com.rictacius.customShop.shop;

import com.rictacius.customShop.Main;
import com.rictacius.customShop.Util;
import com.rictacius.customShop.config.ItemConfig;
import com.rictacius.customShop.config.PluginConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Sell implements Listener {

    public void openInventory(Player player) {
        String inventoryTitle = ChatColor.translateAlternateColorCodes(
                '&',
                Main.getPluginConfig().getSellInventoryTitle()
        );
        Inventory inv = Bukkit.createInventory(null, 36, inventoryTitle);
        player.openInventory(inv);
    }

    public void onCommand(String[] args, Player player) {
        PluginConfig pluginConfig = Main.getPluginConfig();
        if (!player.hasPermission(pluginConfig.getShopPermission())){
            TextComponent message = new TextComponent(
                    ChatColor.translateAlternateColorCodes('&', pluginConfig.getNoPermissionMessage())
            );
            player.spigot().sendMessage(message);
            return;
        }
        if (args.length == 0) {
            String inventoryTitle = ChatColor.translateAlternateColorCodes('&', pluginConfig.getSellInventoryTitle());
            Inventory inv = Bukkit.createInventory(null, 36, inventoryTitle);
            player.openInventory(inv);
        }
    }

    @EventHandler
    public void onExit(InventoryCloseEvent event) {
        PluginConfig pluginConfig = Main.getPluginConfig();
        String expectedInventoryTitle = ChatColor.translateAlternateColorCodes(
                '&', pluginConfig.getSellInventoryTitle()
        );
        if (!event.getView().getTitle().equals(expectedInventoryTitle)) {
            return;
        }
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();
        int numItemsSold = 0;
        double totalEarnings = 0;
        boolean someItemsNotSold = false;
        Shops shops = Main.getShops();
        for (ItemStack item : inventory) {
            if (item == null) {
                continue;
            }
            ItemConfig itemConfig = shops.getConfigForMaterial(item.getType());
            if (itemConfig == null) {
                player.getInventory().addItem(item);
                someItemsNotSold = true;
                continue;
            }
            double earnings = (itemConfig.getSellPrice() / itemConfig.getSellSize()) * item.getAmount();
            numItemsSold += item.getAmount();
            totalEarnings += earnings;
        }
        Main.getEconomy().depositPlayer(player, totalEarnings);
        if (someItemsNotSold) {
            player.spigot().sendMessage(new TextComponent(ChatColor.RED + "Some items could not be sold"));
        }
        player.sendMessage(Shop.replaceAmountCurrencyPricePlaceholders(pluginConfig.getItemsSoldMessage(), numItemsSold, totalEarnings));
    }
}
