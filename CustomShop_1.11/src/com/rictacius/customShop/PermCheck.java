package com.rictacius.customShop;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermCheck {

	public static boolean hasAccessPerm(Player player, String perm) {
		if (player.hasPermission("*")) {
			return true;
		}
		String[] parts = perm.split("\\.");
		String build = "";
		for (int i = 0; i < parts.length; i++) {
			build = build + parts[i] + ".";
			if (player.hasPermission(build + "*")) {
				return true;
			}
		}
		if (player.hasPermission(perm)) {
			return true;
		}
		return false;
	}

	public static boolean hasAccess(Player player, String perm) {
		if (player.isOp()) {
			return true;
		}
		if (player.hasPermission("*")) {
			return true;
		}
		String[] parts = perm.split(".");
		String build = "";
		for (int i = 0; i < parts.length; i++) {
			build = build + parts[i] + ".";
			if (player.hasPermission(build + "*")) {
				return true;
			}
		}
		if (player.hasPermission(perm)) {
			return true;
		}
		return false;
	}

	public static boolean senderHasAccess(CommandSender sender, String perm) {
		if (sender.isOp()) {
			return true;
		}
		if (sender.hasPermission("*")) {
			return true;
		}
		String[] parts = perm.split("\\.");
		String build = "";
		for (String bit : parts) {
			build = build + bit + ".";
			String temp = build + "*";
			if (sender.hasPermission(temp)) {
				return true;
			}
		}
		if (sender.hasPermission(perm)) {
			return true;
		}
		return false;
	}
}
