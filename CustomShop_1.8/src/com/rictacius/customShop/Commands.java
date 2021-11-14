package com.rictacius.customShop;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class Commands implements Listener {

    @EventHandler
    public void processCommands(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().substring(1);
        String[] args = command.contains(" ") ? command.substring(command.indexOf(' ') + 1).split(" ") : new String[0];
        command = command.contains(" ") ? command.split(" ")[0] : command;
        Sell.onCommand(command, args, e.getPlayer());
        Commands.onCommand(command, args, e.getPlayer());
    }

    private static void onCommand(String command, String[] args, Player player) {
        if (command.equals("shop")) {
            if (!(PermCheck.senderHasAccess(player, Main.config.getString("shop-perm")))) {
                player.spigot().sendMessage(new TextComponent(
                        ChatColor.translateAlternateColorCodes('&', Main.config.getString("no-permission-message"))));
            } else if (args.length < 1) {
                player.openInventory(Shops.getShop("customshop{main-inventory($$$)}"));
            } else if (args[0].equalsIgnoreCase("help")) {
                player.sendMessage("");
                player.sendMessage(ChatColor.DARK_GREEN + "Searching for updates...");
                player.sendMessage("");
                Updater.check();
                String update = ChatColor.GREEN + (Updater.newVersion == null ? " Up to date" : " Update available!");
                player.sendMessage(ChatColor.BLUE + "Custom Shop v" + Main.pl.getDescription().getVersion() + update);
                player.sendMessage(ChatColor.YELLOW + "/shop");
                player.sendMessage(ChatColor.YELLOW + "/shop help");
                player.sendMessage(ChatColor.YELLOW + "/shop reload");
                player.sendMessage(ChatColor.YELLOW + "/shop sell");
                player.sendMessage(ChatColor.YELLOW + "/shop update");
                player.sendMessage("");
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!PermCheck.senderHasAccess(player, Main.config.getString("admin-perm"))) {
                    player.spigot().sendMessage(new TextComponent(ChatColor.RED + "You Shall not pass!"));
                } else {
                    try {
                        Main.pl.saveConfig();
                        Main.pl.reloadAllConfigFiles();
                        Shops.loadShops();
                    } catch (Exception e) {
                        player.sendMessage(new TextComponent(
                                ChatColor.RED + "Error reloading config please check config for more information").getText());
                        return;
                    }
                    player.sendMessage(new TextComponent(ChatColor.GREEN + "Plugin reloaded!").getText());
                    player.sendMessage(ChatColor.GRAY + "Plugin built by RictAcius");
                }
            } else if (args[0].equalsIgnoreCase("sell")) {
                Inventory inv = Bukkit.createInventory(null, 36,
                        ChatColor.translateAlternateColorCodes('&', Main.config.getString("sell-inventory-title")));
                player.openInventory(inv);
            } else if (args[0].equalsIgnoreCase("update")) {
                player.sendMessage("");
                player.sendMessage("");
                player.sendMessage(ChatColor.GRAY + "Checking for Updates...");
                boolean check = Updater.check();
                if (check) {
                    player.sendMessage(ChatColor.GREEN + "Updates Available -> " + ChatColor.GOLD + "v" + Updater.newVersion);
                    player.sendMessage(ChatColor.GRAY + "Downloading Updates...");
                    boolean downloaded = Updater.download();
                    if (downloaded) {
                        player.sendMessage(
                                ChatColor.GREEN + "Downloaded Updates -> " + ChatColor.GOLD + "v" + Updater.newVersion);
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You MUST restart your server NOW to install them.");
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "Could not download updates! Check console.");
                    }
                } else {
                    player.sendMessage(ChatColor.GREEN + "CustomShop is up to date");
                }
            }
        }
    }
}
