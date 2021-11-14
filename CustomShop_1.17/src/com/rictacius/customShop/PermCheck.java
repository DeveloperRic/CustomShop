package com.rictacius.customShop;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermCheck {

	public static boolean hasAccessPerm(Player player, String perm) {
		if (player.hasPermission("*")) {
			return true;
		}
		String[] parts = perm.split("\\.");
		StringBuilder build = new StringBuilder();
		for (String part : parts) {
			build.append(part).append(".");
			if (player.hasPermission(build + "*")) {
				return true;
			}
		}
		return player.hasPermission(perm);
	}

	static boolean senderHasAccess(CommandSender sender, String perm) {
		if (sender.isOp()) {
			return true;
		}
		if (sender.hasPermission("*")) {
			return true;
		}
		String[] parts = perm.split("\\.");
		StringBuilder build = new StringBuilder();
		for (String bit : parts) {
			build.append(bit).append(".");
			String temp = build + "*";
			if (sender.hasPermission(temp)) {
				return true;
			}
		}
		return sender.hasPermission(perm);
	}
}
