package com.rictacius.customShop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.tr7zw.itemnbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;

public class Shops implements Listener {
	private static HashMap<String, Inventory> shops = new HashMap<String, Inventory>();
	protected static Main plugin;
	public static String version = "1.8";

	public Shops(Main pl) {
		plugin = pl;
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
		int rawsize = plugin.getShopsConfig().getConfigurationSection("shops").getKeys(false).size();
		for (int i = 0; i >= 0; i++) {
			if (size > 54) {
				break;
			}
			if (size >= rawsize) {
				break;
			}
			size = size + 9;
		}
		Inventory mainInv = Bukkit.createInventory(null, size, ChatColor.RED + "CS Shop");
		for (String shop : plugin.getShopsConfig().getConfigurationSection("shops").getKeys(false)) {
			String name = "";
			try {
				name = ChatColor.translateAlternateColorCodes('&',
						plugin.getShopsConfig().getString("shops." + shop + ".name"));
			} catch (Exception e) {
				name = plugin.getShopsConfig().getString("shops." + shop + ".name");
			}
			name = ChatColor.RED + "CS " + name;
			int count = 0;
			ItemStack logo = null;
			size = 0;
			rawsize = plugin.getShopsConfig().getConfigurationSection("shops." + shop + ".items").getKeys(false).size()
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
			ItemStack back = new ItemStack(Material.BED);
			ItemMeta backmeta = back.getItemMeta();
			backmeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Go Back");
			backmeta.setLore(Arrays.asList(ChatColor.GOLD + "Return to Main"));
			back.setItemMeta(backmeta);
			NBTItem bnbt = new NBTItem(back);
			bnbt.setString("shop", "customshop{main-inventory($$$)}");
			back = bnbt.getItem();
			shopInv.addItem(back);
			for (String item : plugin.getShopsConfig().getConfigurationSection("shops." + shop + ".items")
					.getKeys(false)) {
				try {
					ItemStack invItem = null;
					String[] data = plugin.getShopsConfig().getString("shops." + shop + ".items." + item).split(",");
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
					String cur = plugin.getConfig().getString("currency");
					List<String> lore = new ArrayList<String>();
					int buyAmount = Integer.parseInt(data[1]);
					boolean canBuy = true;
					int buyPrice = 0;
					try {
						buyPrice = Integer.parseInt(data[2]);
						lore.add(ChatColor.GRAY + "Buy " + ChatColor.RED + buyAmount + ChatColor.GRAY + " for "
								+ ChatColor.RED + cur + buyPrice);
					} catch (Exception e) {
						lore.add(ChatColor.RED + "Item Cannot be bought");
						canBuy = false;
					}
					int sellAmount = Integer.parseInt(data[3]);
					int sellPrice = 0;
					try {
						sellPrice = Integer.parseInt(data[4]);
						lore.add(ChatColor.GRAY + "Sell " + ChatColor.RED + sellAmount + ChatColor.GRAY + " for "
								+ ChatColor.RED + cur + sellPrice);
					} catch (Exception e) {
						lore.add(ChatColor.RED + "Item Cannot be sold");
					}
					im.setLore(lore);
					invItem.setItemMeta(im);
					NBTItem nbt = new NBTItem(invItem);
					if (canBuy) {
						nbt.setString("buy", buyAmount + "," + buyPrice);
					}
					invItem = nbt.getItem();
					invItem = removeAttributes(invItem);
					shopInv.addItem(invItem);
					count++;
				} catch (Exception e) {
					Bukkit.getConsoleSender().sendMessage("shop " + shop);
					Bukkit.getConsoleSender().sendMessage("item " + item);
					e.printStackTrace();
				}
			}
			ItemMeta im = logo.getItemMeta();
			im.setDisplayName(name.replaceAll("CS ", ""));
			im.setLore(new ArrayList<String>());
			logo.setItemMeta(im);
			NBTItem nbt = new NBTItem(logo);
			nbt.setString("shop", shop);
			logo = nbt.getItem();
			mainInv.addItem(logo);
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
		ItemStack item = event.getCurrentItem();
		if (item == null)
			return;
		NBTItem nbt = new NBTItem(item);
		if (nbt.getString("shop") != null) {
			if (!(nbt.getString("shop").equals(""))) {
				event.setCancelled(true);
				String shop = nbt.getString("shop");
				if (PermCheck.hasAccessPerm(p, plugin.getShopsConfig().getString("shops." + shop + ".permission"))) {
					event.getWhoClicked().openInventory(getShop(shop));
				} else {
					p.sendMessage(ChatColor.RED + "You may not access this shop!");
				}
			}
		}
		if (nbt.getString("buy") != null) {
			if (!(nbt.getString("buy").equals(""))) {
				event.setCancelled(true);
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
				if (plugin.economy.getBalance(p) < price) {
					p.sendMessage(ChatColor.RED + "You do not have enough money to buy this!");
					return;
				}
				p.getInventory().addItem(ditem);
				plugin.economy.withdrawPlayer(p, price);
			}
		}
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

	private static ItemStack removeAttributes(ItemStack i) {
		if (i == null || i.getType() == Material.BOOK_AND_QUILL)
			return i;

		ItemStack item = i.clone();

		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag;
		if (!nmsStack.hasTag()) {
			tag = new NBTTagCompound();
			nmsStack.setTag(tag);
		} else
			tag = nmsStack.getTag();

		NBTTagList am = new NBTTagList();

		tag.set("AttributeModifiers", am);
		nmsStack.setTag(tag);

		return CraftItemStack.asBukkitCopy(nmsStack);
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
