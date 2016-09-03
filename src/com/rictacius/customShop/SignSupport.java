package com.rictacius.customShop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class SignSupport implements Listener {
	private File signsf;
	private FileConfiguration signsc;
	private ConsoleCommandSender console = Bukkit.getConsoleSender();
	private String prefix = ChatColor.translateAlternateColorCodes('&', "&7[&cCustomShop&7] &r");

	public SignSupport(Main pl) {
		signsf = new File(pl.getDataFolder(), "signs.yml");
		if (!signsf.exists()) {
			signsf.getParentFile().mkdirs();
			pl.saveResource("signs.yml", false);
		}
		signsc = new YamlConfiguration();
		try {
			signsc.load(signsf);
		} catch (FileNotFoundException e) {
			console.sendMessage(prefix + "Signs.yml file could not be found!");
			e.printStackTrace();
		} catch (IOException e) {
			console.sendMessage(prefix + "Signs.yml file could not be loaded!");
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			console.sendMessage(
					prefix + "Signs.yml file could not be loaded make sure you're not editing the file manually!!");
			e.printStackTrace();
		}
		signsc.options().copyDefaults(false);
		saveConfig();
	}

	void saveConfig() {
		try {
			signsc.save(signsf);
		} catch (IOException e) {
			console.sendMessage(prefix + "Signs.yml file could not be saved!");
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onPlace(final SignChangeEvent event) {
		Bukkit.getScheduler().runTaskLater(Main.pl, new Runnable() {
			public void run() {
				String[] lines = event.getLines();
				if (lines == null)
					return;
				if (lines.length < 2)
					return;
				if (!event.isCancelled()) {
					if (ChatColor.stripColor(lines[0]).equalsIgnoreCase("[CustomShop]")) {
						if (ChatColor.stripColor(lines[1]).equalsIgnoreCase("Buy")) {
							if (lines.length >= 3) {
								if (Shops.getShop(lines[2]) == null) {
									if (!lines[2].equals("")) {
										event.setLine(2, ChatColor.DARK_RED + "Invalid shop");
										return;
									}
								}
							}
							String loc = locationToString(event.getBlock().getLocation());
							String send = loc;
							List<String> signs = signsc.getStringList("signs");
							for (String sign : signs) {
								String[] e = sign.split("(%£%)");
								if (e[0].equals(loc)) {
									signs.remove(sign);
								}
							}
							signs.add(send);
							signsc.set("signs", signs);
							saveConfig();
						} else if (ChatColor.stripColor(lines[1]).equalsIgnoreCase("Sell")) {
							String loc = locationToString(event.getBlock().getLocation());
							String send = loc;
							List<String> signs = signsc.getStringList("signs");
							for (String sign : signs) {
								String[] e = sign.split("(%£%)");
								if (e[0].equals(loc)) {
									signs.remove(sign);
								}
							}
							signs.add(send);
							signsc.set("signs", signs);
							saveConfig();
							event.getPlayer().sendMessage(ChatColor.GREEN + "Added Custom shop sign!");
						} else {
							event.setLine(1, ChatColor.DARK_RED + "Error: Buy | Sell");
						}
					}
				}
			}
		}, 10L);
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (!event.isCancelled()) {
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				Block block = event.getClickedBlock();
				if (block.getType() == Material.SIGN || block.getType() == Material.SIGN_POST
						|| block.getType() == Material.WALL_SIGN) {
					Sign signb = (Sign) block.getState();
					String[] lines = signb.getLines();
					if (ChatColor.stripColor(lines[0]).equalsIgnoreCase("[CustomShop]")) {
						Player p = event.getPlayer();
						if (ChatColor.stripColor(lines[1]).equalsIgnoreCase("Buy")) {
							String shop = "customshop{main-inventory($$$)}";
							if (lines.length >= 3) {
								shop = lines[2];
							}
							if (shop.equals("")) {
								shop = "customshop{main-inventory($$$)}";
							}
							event.setCancelled(true);
							p.openInventory(Shops.getShop(shop));
						} else if (ChatColor.stripColor(lines[1]).equalsIgnoreCase("Sell")) {
							event.setCancelled(true);
							Inventory inv = Bukkit.createInventory(null, 36, ChatColor.RED + "CSell");
							p.openInventory(inv);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		String loc = locationToString(event.getBlock().getLocation());
		List<String> signs = signsc.getStringList("signs");
		List<String> newsigns = signs;
		boolean change = false;
		for (int i = 0; i < signs.size(); i++) {
			String sign = signs.get(i);
			String[] e = sign.split("(%£%)");
			if (e[0].equals(loc)) {
				newsigns.remove(sign);
				change = true;
			}
		}
		if (change) {
			signsc.set("signs", newsigns);
			saveConfig();
		}
	}

	Location stringToLocation(String string) {
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

	String locationToString(Location loc) {
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
