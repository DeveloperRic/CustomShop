package com.rictacius.customShop;

import com.rictacius.customShop.config.PluginConfig;
import com.rictacius.customShop.updater.Updater;
import com.rictacius.customShop.updater.UpdaterException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Commands implements Listener {

    @EventHandler
    public void processCommands(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage().substring(1);
        String[] args = command.contains(" ") ? command.substring(command.indexOf(' ') + 1).split(" ") : new String[0];
        command = command.contains(" ") ? command.split(" ")[0] : command;
        onCommand(command, args, e.getPlayer());
    }

    private void onCommand(String command, String[] args, Player player) {
        if (command.equals("shop")) {
            processShopCommand(args, player);
        }
        if (command.equals("sell")) {
            Main.getSell().onCommand(args, player);
        }
    }

    private void processShopCommand(String[] args, Player player) {
        PluginConfig pluginConfig = Main.getPluginConfig();
        if (!(PermCheck.senderHasAccess(player, pluginConfig.getShopPermission()))) {
            TextComponent message = new TextComponent(
                    ChatColor.translateAlternateColorCodes('&', pluginConfig.getNoPermissionMessage())
            );
            player.spigot().sendMessage(message);
            return;
        }
        if (args.length == 0) {
            player.openInventory(Main.getShops().getMainMenu().getInventory());
        } else if (args[0].equalsIgnoreCase("help")) {
            showHelp(player);
        } else if (args[0].equalsIgnoreCase("reload")) {
            reloadPlugin(player, pluginConfig);
        } else if (args[0].equalsIgnoreCase("sell")) {
            Main.getSell().openInventory(player);
        } else if (args[0].equalsIgnoreCase("update")) {
            updatePlugin(player);
        }
    }

    private void showHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GREEN + "Searching for updates...");
        player.sendMessage("");
        String update;
        try {
            Updater updater = new Updater();
            update = ChatColor.GREEN + (updater.isNewVersionAvailable() ? " Update available!" : " Up to date");
        } catch (UpdaterException e) {
            update = ChatColor.GREEN + " Failed to check for updates!";
            e.printStackTrace();
        }
        player.sendMessage(ChatColor.BLUE + "Custom Shop v" + Main.plugin.getDescription().getVersion() + update);
        player.sendMessage(ChatColor.YELLOW + "/shop");
        player.sendMessage(ChatColor.YELLOW + "/shop help");
        player.sendMessage(ChatColor.YELLOW + "/shop reload");
        player.sendMessage(ChatColor.YELLOW + "/shop sell");
        player.sendMessage(ChatColor.YELLOW + "/shop update");
        player.sendMessage(ChatColor.YELLOW + "* NOTE: server must be restarted after CustomShop update");
        player.sendMessage("");
    }

    private void reloadPlugin(Player player, PluginConfig pluginConfig) {
        if (!PermCheck.senderHasAccess(player, pluginConfig.getAdminPermission())) {
            player.spigot().sendMessage(new TextComponent(ChatColor.RED + "You Shall not pass!"));
        } else {
            try {
                Main.plugin.saveConfig();
                Main.plugin.reloadAllConfigFiles();
            } catch (Exception e) {
                player.sendMessage(new TextComponent(
                        ChatColor.RED + "Error reloading config please check config for more information").getText());
                return;
            }
            player.sendMessage(new TextComponent(ChatColor.GREEN + "Plugin reloaded!").getText());
            player.sendMessage(ChatColor.GRAY + "Plugin built by RictAcius");
        }
    }

    private void updatePlugin(Player player) {
        player.sendMessage("");
        player.sendMessage("");
        Updater updater = new Updater();
        boolean updatesAvailable = false;
        try {
            player.sendMessage(ChatColor.GRAY + "Checking for Updates...");
            updatesAvailable = updater.isNewVersionAvailable();
        } catch (UpdaterException e) {
            player.sendMessage(ChatColor.DARK_RED + "Could not check for updates! Check console.");
            e.printStackTrace();
        }
        if (updatesAvailable) {
            player.sendMessage(ChatColor.GREEN + "Updates Available -> " + ChatColor.GOLD + "v" + updater.getNewVersionName());
            player.sendMessage(ChatColor.GRAY + "Downloading Updates...");
            try {
                updater.downloadNewVersion();
                player.sendMessage(ChatColor.GREEN + "Downloaded Updates -> " + ChatColor.GOLD + "v" + updater.getNewVersionName());
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You MUST restart your server NOW to install them.");
            } catch (UpdaterException e) {
                player.sendMessage(ChatColor.DARK_RED + "Could not download update! Check console.");
                e.printStackTrace();
            }
        } else {
            player.sendMessage(ChatColor.GREEN + "CustomShop is up to date");
        }
    }
}
