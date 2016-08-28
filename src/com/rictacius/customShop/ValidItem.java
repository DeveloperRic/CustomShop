package com.rictacius.customShop;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ValidItem {

	public static boolean hasCustomName(ItemStack item) {
		if (item == null) {
			return false;
		}
		try {
			String dname = item.getItemMeta().getDisplayName();
			if (dname == null) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean nameIs(ItemStack item, String name) {
		if (hasCustomName(item)) {
			if (item.getItemMeta().getDisplayName().equals(name)) {
				return true;
			}
			return false;
		}
		return false;
	}

	public static boolean nameStartsWith(ItemStack item, String name) {
		if (hasCustomName(item)) {
			if (item.getItemMeta().getDisplayName().startsWith(name)) {
				return true;
			}
			return false;
		}
		return false;
	}

	public static boolean nameContains(ItemStack item, String name) {
		if (hasCustomName(item)) {
			if (item.getItemMeta().getDisplayName().contains(name)) {
				return true;
			}
			return false;
		}
		return false;
	}

	public static boolean invNameIs(Inventory inv, String name) {
		if (inv == null) {
			return false;
		}
		if (inv.getName().equals(name)) {
			return true;
		}
		return false;
	}
}
