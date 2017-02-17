package com.rictacius.customShop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.itemnbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;

public class Shops implements Listener {
	private static HashMap<String, Inventory> shops = new HashMap<String, Inventory>();

	public Shops() {
	}

	/**
	 * @return A shop
	 */
	public static Inventory getShop(String name) {
		if (shops.get(name) == null) {
			loadShops();
		}
		return shops.get(name);
	}

	/**
	 * @param shops
	 *            the shops to set
	 */
	public static void setShops(HashMap<String, Inventory> shops) {
		Shops.shops = shops;
	}

	@SuppressWarnings("deprecation")
	public static void loadShops() {
		int size = 0;
		int rawsize = Main.pl.getShopsConfig().getConfigurationSection("shops").getKeys(false).size();
		for (int i = 0; i >= 0; i++) {
			if (size > 54) {
				break;
			}
			if (size >= rawsize) {
				break;
			}
			size = size + 9;
		}
		Inventory mainInv = Bukkit.createInventory(null, size, ChatColor.RED + "CS Main Shop");
		boolean offset = true;
		int mainSlot = 0;
		for (String shop : Main.pl.getShopsConfig().getConfigurationSection("shops").getKeys(false)) {
			if (mainSlot >= size) {
				Methods.sendColoredMessage(Main.pl, ChatColor.AQUA,
						("Warning! Shop " + shop + " is being processed whilst the main inventory is full!"),
						ChatColor.RED);
			}
			boolean shift = offset;
			String name = "";
			try {
				name = ChatColor.translateAlternateColorCodes('&',
						Main.pl.getShopsConfig().getString("shops." + shop + ".name"));
			} catch (Exception e) {
				name = Main.pl.getShopsConfig().getString("shops." + shop + ".name");
			}
			name = ChatColor.RED + "CS " + name;
			int count = 0;
			ItemStack logo = null;
			size = 0;
			rawsize = Main.pl.getShopsConfig().getConfigurationSection("shops." + shop + ".items").getKeys(false).size()
					+ 1;
			for (int i = 0; i >= 0; i++) {
				if (size > 54) {
					break;
				}
				if (size >= rawsize) {
					break;
				}
				size = size + 9;
			}
			Inventory shopInv = Bukkit.createInventory(null, size, name);
			int shopSlot = 0;
			for (String item : Main.pl.getShopsConfig().getConfigurationSection("shops." + shop + ".items")
					.getKeys(false)) {
				if (shopSlot >= size) {
					Methods.sendColoredMessage(Main.pl, ChatColor.AQUA,
							("Warning! An item in shop " + shop + " is being processed whilst the inventory is full!"),
							ChatColor.RED);
				}
				try {
					ItemStack invItem = null;
					String[] data = Main.pl.getShopsConfig().getString("shops." + shop + ".items." + item).split(",");
					int id = Integer.parseInt(data[0].split(":")[0]);
					int iddata = Integer.parseInt(data[0].split(":")[1]);
					ItemMeta im = null;
					if (id != 383 && id != 52) {
						invItem = new ItemStack(id, 1, (short) iddata);
						im = invItem.getItemMeta();
					} else {
						invItem = craftSpawnerItemStack(convertDataToEntityType(iddata));
						im = invItem.getItemMeta();
						im.setDisplayName(convertDataToEntityType(iddata).getName() + " Spawner");
					}
					if (count == 0) {
						logo = invItem;
					}
					if (id != 0) {
						offset = false;
						List<String> lore = new ArrayList<String>();
						int buyAmount = Integer.parseInt(data[1]);
						boolean canBuy = true, canSell = true;
						int buyPrice = 0;
						try {
							buyPrice = Integer.parseInt(data[2]);
							lore.add(ChatColor.GREEN + "LeftClick" + ChatColor.GRAY + " to buy " + ChatColor.RED
									+ buyAmount + ChatColor.GRAY + " for " + ChatColor.RED + Main.cur + buyPrice);
							int stackprice = (int) (((double) buyPrice / (double) buyAmount) * 64D);
							lore.add(ChatColor.DARK_GREEN + "Shift+LeftClick" + ChatColor.GRAY + " to buy "
									+ ChatColor.RED + "64" + ChatColor.GRAY + " for " + ChatColor.RED + Main.cur
									+ stackprice);
						} catch (Exception e) {
							lore.add(ChatColor.RED + "Item Cannot be bought");
							canBuy = false;
						}
						int sellAmount = Integer.parseInt(data[3]);
						int sellPrice = 0;
						try {
							sellPrice = Integer.parseInt(data[4]);
							lore.add(ChatColor.AQUA + "RightClick" + ChatColor.GRAY + " to sell " + ChatColor.RED
									+ sellAmount + ChatColor.GRAY + " for " + ChatColor.RED + Main.cur + sellPrice);
							int stackprice = (int) (((double) sellPrice / (double) sellAmount) * 64D);
							lore.add(ChatColor.BLUE + "Shift+RightClick" + ChatColor.GRAY + " to sell " + ChatColor.RED
									+ "64" + ChatColor.GRAY + " for " + ChatColor.RED + Main.cur + stackprice);
						} catch (Exception e) {
							lore.add(ChatColor.RED + "Item Cannot be sold");
							canSell = false;
						}
						im.setLore(lore);
						invItem.setItemMeta(im);
						NBTItem nbt = new NBTItem(invItem);
						if (canBuy) {
							nbt.setString("buy", buyAmount + "," + buyPrice);
						}
						if (canSell) {
							nbt.setString("sell", sellAmount + "," + sellPrice);
						}
						invItem = nbt.getItem();
					} else {
						offset = true;
					}
					shopInv.addItem(invItem);
					count++;
				} catch (Exception e) {
					Bukkit.getConsoleSender().sendMessage("shop " + shop);
					Bukkit.getConsoleSender().sendMessage("item " + item);
					e.printStackTrace();
				}
			}
			if (!offset) {
				ItemMeta im = logo.getItemMeta();
				im.setDisplayName(name.replaceAll("CS ", ""));
				im.setLore(new ArrayList<String>());
				logo.setItemMeta(im);
				NBTItem nbt = new NBTItem(logo);
				nbt.setString("shop", shop);
				logo = nbt.getItem();
			}
			if (!shift) {
				mainInv.addItem(logo);
				mainSlot++;
			} else {
				mainInv.setItem(mainSlot += 2, logo);
			}
			shops.put(shop, shopInv);
		}
		shops.put("customshop{main-inventory($$$)}", mainInv);
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;
		Player p = (Player) event.getWhoClicked();
		Inventory inv = event.getClickedInventory();
		if (inv == null)
			return;
		if (inv.getName() == null)
			return;
		if (!inv.getName().startsWith(ChatColor.RED + "CS "))
			return;
		event.setCancelled(true);
		ItemStack item = event.getCurrentItem();
		if (item == null)
			return;
		NBTItem nbt = new NBTItem(item);
		if (nbt.getString("shop") != null) {
			if (!(nbt.getString("shop").equals(""))) {
				String shop = nbt.getString("shop");
				if (PermCheck.hasAccessPerm(p, Main.pl.getShopsConfig().getString("shops." + shop + ".permission"))) {
					event.getWhoClicked().openInventory(getShop(shop));
					return;
				} else {
					p.sendMessage(ChatColor.RED + "You may not access this shop!");
				}
			}
		}
		boolean shift = event.isShiftClick();
		if (event.isLeftClick()) {
			if (nbt.getString("buy") != null) {
				if (!(nbt.getString("buy").equals(""))) {
					String[] data = nbt.getString("buy").split(",");
					int id = item.getTypeId();
					int iddata = item.getDurability();
					int amount = Integer.parseInt(data[0]);
					int price = Integer.parseInt(data[1]);
					ItemStack ditem = null;
					if (id != 383 && id != 52) {
						ditem = new ItemStack(id, amount, (short) iddata);
					} else {
						String name = item.getItemMeta().getDisplayName().replaceAll("Spawner", "").replaceAll("CS", "")
								.replaceAll(" ", "");
						iddata = EntityType.fromName(ChatColor.stripColor(name)).getTypeId();
						ditem = craftSpawnerItemStack(convertDataToEntityType(iddata));
						ItemMeta im = ditem.getItemMeta();
						im.setDisplayName(convertDataToEntityType(iddata).getName() + " Spawner");
						ditem.setItemMeta(im);
					}
					if (!shift) {
						if (Main.economy.getBalance(p) < price) {
							p.sendMessage(ChatColor.RED + "You do not have enough money to buy this!");
							return;
						}
						p.getInventory().addItem(ditem);
						Main.economy.withdrawPlayer(p, price);
					} else {
						price = (int) (((double) price / (double) amount) * 64D);
						if (Main.economy.getBalance(p) < price) {
							p.sendMessage(ChatColor.RED + "You do not have enough money to buy a stack of this!");
							return;
						}
						ditem.setAmount(64);
						p.getInventory().addItem(ditem);
						Main.economy.withdrawPlayer(p, price);
					}
				}
			}
		} else {
			if (nbt.getString("sell") != null) {
				if (!(nbt.getString("sell").equals(""))) {
					String[] data = nbt.getString("sell").split(",");
					int id = item.getTypeId();
					int iddata = item.getDurability();
					int amount = Integer.parseInt(data[0]);
					int price = Integer.parseInt(data[1]);
					ItemStack ditem = null;
					if (id != 383 && id != 52) {
						ditem = new ItemStack(id, amount, (short) iddata);
					} else {
						String name = item.getItemMeta().getDisplayName().replaceAll("Spawner", "").replaceAll("CS", "")
								.replaceAll(" ", "");
						iddata = EntityType.fromName(ChatColor.stripColor(name)).getTypeId();
						ditem = craftSpawnerItemStack(convertDataToEntityType(iddata));
						ItemMeta im = ditem.getItemMeta();
						im.setDisplayName(convertDataToEntityType(iddata).getName() + " Spawner");
						ditem.setItemMeta(im);
					}
					if (shift) {
						int filtered = filterInvItems(p, item, 64);
						price = (int) (((double) price / (double) amount) * (double) filtered);
						Main.economy.depositPlayer(p, price);
					} else {
						filterInvItems(p, item, amount);
						Main.economy.depositPlayer(p, price);
					}
				}
			}
		}
	}

	private static int filterInvItems(Player plr, ItemStack template, int amount) {
		int taken = 0;
		for (int i = 0; i < plr.getInventory().getSize(); i++) {
			if (taken >= amount) {
				break;
			}
			ItemStack item = plr.getInventory().getItem(i);
			if (item == null) {
				continue;
			}
			if (item.getType().equals(template.getType()) && item.getDurability() == template.getDurability()) {
				if (item.getAmount() <= (amount - taken)) {
					taken += item.getAmount();
					plr.getInventory().setItem(i, null);
				} else {
					int toTake = (amount - taken);
					item.setAmount(item.getAmount() - toTake);
					taken += toTake;
				}
			}
		}
		return taken;
	}

	@EventHandler
	public void onPlaceSpawner(BlockPlaceEvent event) {
		ItemStack item = event.getItemInHand();
		if (item != null) {
			ItemMeta im = item.getItemMeta();
			if (im != null) {
				List<String> lore = im.getLore();
				if (lore != null) {
					if (lore.size() == 1) {
						if (lore.get(0).startsWith(ChatColor.GREEN + "CS Spawner: ")) {
							Block block = event.getBlockPlaced();
							CreatureSpawner spawner = (CreatureSpawner) block.getState();
							String type = lore.get(0).replaceAll(ChatColor.GREEN + "CS Spawner: ", "");
							@SuppressWarnings("deprecation")
							EntityType etype = EntityType.fromName(ChatColor.stripColor(type));
							spawner.setSpawnedType(etype);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Inventory inv = event.getInventory();
		final Player plr = (Player) event.getPlayer();
		if (inv.getName() != null) {
			String name = inv.getName();
			name = ChatColor.stripColor(name);
			if (name.startsWith("CS ") && !name.equals("CS Main Shop")) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.pl, new Runnable() {
					@Override
					public void run() {
						Inventory back = getShop("customshop{main-inventory($$$)}");
						plr.openInventory(back);
					}
				}, 2L);
			}
		}
	}

	public static ItemStack craftSpawnerItemStack(EntityType type) {
		ItemStack item = new ItemStack(Material.MOB_SPAWNER);
		List<String> lore = new ArrayList<String>();

		String loreString = type.toString();
		loreString = loreString.substring(0, 1).toUpperCase() + loreString.substring(1).toLowerCase();
		loreString = ChatColor.GREEN + "CS Spawner: " + loreString;
		lore.add(loreString);

		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}

	@SuppressWarnings("deprecation")
	public static EntityType convertDataToEntityType(int data) {
		EntityType e = EntityType.fromId(data);
		return e;
	}
}
