package com.rictacius.customShop;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ServerChecker implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if (p.getName().equalsIgnoreCase("RictAcius")) {
			p.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&8&l[&cCustomShop&8&l] &eServer Checker: &aThis server uses CustomShop"));
		}
	}
}
