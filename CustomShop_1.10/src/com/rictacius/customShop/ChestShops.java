package com.rictacius.customShop;

import de.tr7zw.itemnbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ChestShops implements Listener {
	private static List<ChestShop> shops = new ArrayList<ChestShop>();
	private static File shopsF;
	private static FileConfiguration shopsc;

	private static class ChestShop implements Listener {
		Location location;
		Location blockloc;
		ItemStack item;
		double price;
		boolean isBuy;
		UUID id;
		Item i;
		private int task;
		int stock;

		ChestShop(Location location, Location blockloc, ItemStack item, double price, boolean isBuy, UUID id) {
			if (id == null) {
				id = UUID.randomUUID();
				boolean unique = false;
				int tries = 0;
				while (!unique && tries <= 100) {
					for (ChestShop s : shops) {
						if (s.id.equals(id)) {
							unique = false;
							break;
						} else {
							unique = true;
						}
					}
					tries++;
				}
			}
			this.id = id;
			this.location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(),
					location.getBlockZ());
			this.blockloc = new Location(blockloc.getWorld(), blockloc.getBlockX(), blockloc.getBlockY(),
					blockloc.getBlockZ());
			this.item = item;
			this.price = price;
			this.isBuy = isBuy;
			reset();
		}

		void reset() {
			cancelUpdate();
			if (!blockloc.getBlock().getType().equals(Material.CHEST))
				blockloc.getBlock().setType(Material.CHEST);
			if (i != null) {
				i.remove();
			}
			Location temp = blockloc;
			i = blockloc.getWorld().dropItem(temp.add(0, 0.5, 0), item);
			i.setCustomName("customshop{chest-shops(no-pickup)}");
			i.teleport(temp);
			update();
		}

		void cancelUpdate() {
			if (i != null) {
				i.remove();
				i = null;
			}
			Bukkit.getScheduler().cancelTask(task);
		}

		void update() {
			cancelUpdate();
			task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.pl, new Runnable() {
				@Override
				public void run() {
					Location temp = blockloc;
					temp.add(0, 0.5, 0);
					if (i != null) {
						if (!i.getLocation().equals(temp)) {
							i.teleport(temp);
						}
					} else {
						i = blockloc.getWorld().dropItem(temp.add(0, 0.5, 0), item);
						i.setCustomName("customshop{chest-shops(no-pickup)}");
						i.teleport(temp);
					}
				}
			}, 0L, 100L);
		}

		@EventHandler
		public void onPickup(PlayerPickupItemEvent e) {
			Item i = e.getItem();
			if (i.equals(this.i)) {
				e.setCancelled(true);
			} else if (i.getCustomName() != null) {
				if (i.getCustomName().equals("customshop{chest-shops(no-pickup)}")) {
					e.setCancelled(true);
				}
			}
		}

		@EventHandler
		public void onHit(BlockDamageEvent e) {
			if (e.getBlock().getLocation().equals(location.getBlock().getLocation())
					|| e.getBlock().getLocation().equals(blockloc.getBlock().getLocation())) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		public void onBreak(BlockBreakEvent e) {
			if (e.getBlock().getLocation().equals(location.getBlock().getLocation())
					|| e.getBlock().getLocation().equals(blockloc.getBlock().getLocation())) {
				e.setCancelled(true);
			}
		}

		@EventHandler
		public void onInteract(PlayerInteractEvent e) {
			if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				return;
			}
			if (!e.getClickedBlock().getLocation().equals(location.getBlock().getLocation())) {
				return;
			}
			e.setCancelled(true);
			Player p = e.getPlayer();
			if (!isBuy) {
				if (p.getItemInHand() == null) {
					p.sendMessage(ChatColor.RED + "You must have an item in your hand!");
				}
			}
			if (!p.isSneaking()) {
				confirmTransaction(this, p);
			} else {
				completeTransaction(this, p);
			}
		}
	}

	private static boolean containsItem(Player p, ItemStack item) {
		int count = 0;
		for (ItemStack i : p.getInventory()) {
			if (count >= item.getAmount()) {
				return true;
			}
			if (i != null) {
				if (i.getType().equals(item.getType()) && i.getDurability() == item.getDurability()) {
					count += i.getAmount();
				}
			}
		}
		return false;
	}

	private static void takeItem(Player p, ItemStack item) {
		int count = 0;
		for (int i = 0; i < p.getInventory().getSize(); i++) {
			if (count >= item.getAmount()) {
				return;
			}
			ItemStack j = p.getInventory().getItem(i);
			if (j != null) {
				if (j.getType().equals(item.getType()) && j.getDurability() == item.getDurability()) {
					p.getInventory().setItem(i, null);
					count += j.getAmount();
				}
			}
		}
	}

	private static void confirmTransaction(ChestShop shop, Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Confirm ChestShop Action");
		inv.setItem(4, shop.item);
		ItemStack temp = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
		ItemMeta im = temp.getItemMeta();
		im.setDisplayName(ChatColor.GREEN + "Continue " + (shop.isBuy ? " purchase" : "sale"));
		im.setLore(Arrays.asList(ChatColor.GOLD + (shop.isBuy ? "Buy" : "Sell") + " x" + shop.item.getAmount()
				+ " (" + shop.item.getType().toString().replaceAll("_", " ") + " : " + shop.item.getDurability()
				+ ") for " + Main.cur + shop.price
				, ChatColor.RED + "" + ChatColor.BOLD + "This action cannot be undone!"));
		temp.setItemMeta(im);
		NBTItem nbt = new NBTItem(temp);
		nbt.setString("confirm-yes", shop.id.toString());
		temp = nbt.getItem();
		inv.setItem(8, temp);
		temp = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
		im = temp.getItemMeta();
		im.setDisplayName(ChatColor.RED + "Cancel operation");
		temp.setItemMeta(im);
		nbt = new NBTItem(temp);
		nbt.setString("confirm-cancel", "l");
		temp = nbt.getItem();
		inv.setItem(0, temp);
		p.openInventory(inv);
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		Inventory inv = e.getClickedInventory();
		ItemStack item = e.getCurrentItem();
		if (item == null)
			return;
		if (item.getType().equals(Material.AIR))
			return;
		if (ValidItem.invNameIs(inv, ChatColor.GOLD + "Confirm ChestShop Action")) {
            e.setCancelled(true);
			NBTItem nbt = new NBTItem(item);
			Player plr = (Player) e.getWhoClicked();
			if (!nbt.getString("confirm-yes").equals("")) {
				String s = nbt.getString("confirm-yes");
				ChestShop shop = getShop(UUID.fromString(s));
				completeTransaction(shop, plr);
			} else if (inv.getItem(0).equals(item)) {
				plr.closeInventory();
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlaceSign(SignChangeEvent e) {
		String[] lines = e.getLines();
		if (lines == null) {
			return;
		}
		if (lines.length < 4) {
			return;
		}
		if (lines[0].equalsIgnoreCase("[Chest-Shop]")) {
			Player plr = e.getPlayer();
			if (!plr.hasPermission(Main.config.getString("chestshop-perm"))) {
				return;
			}
			int id = 0, data = 0, amount = 0;
			try {
				id = Integer.parseInt(lines[1].split(":")[0]);
				data = Integer.parseInt(lines[1].split(":")[1]);
				amount = Integer.parseInt(lines[1].split(":")[2]);
			} catch (Exception er) {
				plr.sendMessage(ChatColor.RED + "Invalid item string!");
				plr.sendMessage(ChatColor.RED + "Example 5(id):3(data):32(amount)");
				return;
			}
			double price = 0;
			try {
				price = Double.parseDouble(lines[2]);
			} catch (Exception er) {
				plr.sendMessage(ChatColor.RED + "Invalid price!");
				plr.sendMessage(ChatColor.RED + "Example 234.5");
				return;
			}
			boolean isBuy = false;
			if (lines[3].equalsIgnoreCase("buy")) {
				isBuy = true;
			} else if (lines[3].equalsIgnoreCase("sell")) {
				isBuy = false;
			} else {
				plr.sendMessage(ChatColor.RED + "Invalid buy/sell switch!");
				plr.sendMessage(ChatColor.RED + "Example Buy or Sell");
				return;
			}
			ItemStack item = new ItemStack(id, amount, (short) data);
			Location blockloc = e.getBlock().getLocation();
			if (e.getBlock().getLocation().add(1, 0, 0).getBlock().getType() == Material.CHEST) {
				blockloc = e.getBlock().getLocation().add(1, 0, 0);
			} else if (e.getBlock().getLocation().subtract(1, 0, 0).getBlock().getType() == Material.CHEST) {
				blockloc = e.getBlock().getLocation().subtract(1, 0, 0);
			} else if (e.getBlock().getLocation().add(0, 0, 1).getBlock().getType() == Material.CHEST) {
				blockloc = e.getBlock().getLocation().add(0, 0, 1);
			} else if (e.getBlock().getLocation().subtract(0, 0, 1).getBlock().getType() == Material.CHEST) {
				blockloc = e.getBlock().getLocation().subtract(0, 0, 1);
			} else {
				plr.sendMessage(ChatColor.RED + "Invalid location!");
				plr.sendMessage(ChatColor.RED + "The sign must be placed infront of a chest!");
				return;
			}
			createShop(e.getBlock().getLocation(), blockloc, item, price, isBuy, null);
			plr.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&aChestShop created! Your shop " + (isBuy ? "sells " : "buys ") + "&cx" + item.getAmount() + " "
							+ item.getType().toString() + ":" + item.getDurability() + " &a for &6" + Main.cur
							+ price));
			plr.sendMessage(ChatColor.RED + "Remember, you do not get any money from your ChestShops!");
		}
	}

	private static void completeTransaction(ChestShop shop, Player p) {
		if (shop.isBuy) {
			if (Main.economy.getBalance(p) >= shop.price) {
				Main.economy.withdrawPlayer(p, shop.price);
				addItem(p, shop.item);
			} else {
				p.sendMessage(ChatColor.RED + "You do not have enough money to purchase this item!");
			}
		} else {
			if (!containsItem(p, shop.item)) {
				p.sendMessage(ChatColor.RED + "You do not have enough of the item specified!");
			} else {
				takeItem(p, shop.item);
			}
		}
		p.closeInventory();
	}

	public static void addItem(Player plr, ItemStack item) {
		boolean space = false;
		int loc = -1;
		for (int i = 0; i < plr.getInventory().getSize(); i++) {
			ItemStack it = plr.getInventory().getItem(i);
			if (it == null) {
				space = true;
				loc = i;
				break;
			} else if (it.getType().equals(Material.AIR)) {
				space = true;
				loc = i;
				break;
			}
		}
		if (space) {
			plr.getInventory().setItem(loc, item);
		} else {
			plr.sendMessage(ChatColor.RED + "" + ChatColor.BOLD
					+ "You do not have enough room for this item! Dropping it on the ground.");
			plr.getWorld().dropItem(plr.getLocation(), item);
		}
	}

	public static ChestShop createShop(Location location, Location blockloc, ItemStack item, double price,
			boolean isBuy, UUID id) {
		ChestShop shop = new ChestShop(location, blockloc, item, price, isBuy, id);
		Main.pl.getServer().getPluginManager().registerEvents(shop, Main.pl);
		shops.add(shop);
		saveShops();
		return shop;
	}

	public static ChestShop getShop(UUID id) {
		for (ChestShop s : shops) {
			if (s.id.equals(id)) {
				return s;
			}
		}
		return null;
	}

	private static void loadConfig() {
		shopsF = new File(Main.pl.getDataFolder(), "chestshops.yml");
		shopsc = new YamlConfiguration();
		if (shopsF.exists()) {
			try {
				shopsc.load(shopsF);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
		} else {
			try {
				shopsc.save(shopsF);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	static void loadShops() {
		shutdownShops();
		shops.clear();
		loadConfig();
		if (shopsc.getConfigurationSection("shops") == null)
			return;
		for (String shop : shopsc.getConfigurationSection("shops").getKeys(false)) {
			String base = "shops." + shop;
			Location loc = stringToLocation(shopsc.getString(base + ".location"));
			Location blockloc = stringToLocation(shopsc.getString(base + ".blockloc"));
			ItemStack item = new ItemStack(Integer.parseInt(shopsc.getString(base + ".id")),
					Integer.parseInt(shopsc.getString(base + ".amount")),
					Short.parseShort(shopsc.getString(base + ".data")));
			double price = Double.parseDouble(shopsc.getString(base + ".price"));
			boolean isBuy = Boolean.parseBoolean(shopsc.getString(base + ".isbuy"));
			UUID id = UUID.fromString(shopsc.getString(base + ".uuid"));
			createShop(loc, blockloc, item, price, isBuy, id);
		}
	}

	public static void shutdownShops() {
		for (ChestShop s : ChestShops.shops) {
			s.cancelUpdate();
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean saveShops() {
		if (shopsc.getConfigurationSection("shops") != null) {
			for (String stritem : shopsc.getConfigurationSection("shops").getKeys(false)) {
				for (String sub : shopsc.getConfigurationSection("shops." + stritem).getKeys(false)) {
					shopsc.set("shops." + stritem + "." + sub, null);
				}
				shopsc.set("shops." + stritem, null);
			}
			shopsc.set("shops", null);
		}
		for (int i = 0, name = 0; i < shops.size(); i++, name++) {
			ChestShop s = shops.get(i);
			s.cancelUpdate();
			shopsc.set("shops." + name + ".uuid", s.id.toString());
			shopsc.set("shops." + name + ".location", locationToString(s.location));
			shopsc.set("shops." + name + ".blockloc", locationToString(s.blockloc));
			shopsc.set("shops." + name + ".id", s.item.getTypeId());
			shopsc.set("shops." + name + ".amount", s.item.getAmount());
			shopsc.set("shops." + name + ".data", s.item.getDurability());
			shopsc.set("shops." + name + ".price", s.price);
			shopsc.set("shops." + name + ".isbuy", s.isBuy);
			s.update();
		}
		try {
			shopsc.save(shopsF);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static Location stringToLocation(String string) {
		Location l = null;
		try {
			String[] e = string.split(",");
			World w = Bukkit.getServer().getWorld(e[0]);
			l = new Location(w, Double.parseDouble(e[1]), Double.parseDouble(e[2]), Double.parseDouble(e[3]),
					Float.parseFloat(e[4]), Float.parseFloat(e[5]));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return l;
	}

	private static String locationToString(Location loc) {
		String world = loc.getWorld().getName();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		float yaw = loc.getYaw();
		float pitch = loc.getPitch();
		String locString = world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
		return locString;
	}

}
