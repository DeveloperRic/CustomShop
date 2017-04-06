package customShop;

import itemnbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class Shops implements Listener {
    private static HashMap<String, Inventory> shops = new HashMap<String, Inventory>();

    /**
     * @return A shop
     */
    static Inventory getShop(String name) {
        if (!shops.containsKey(name)) {
            loadShops();
        }
        return shops.get(name);
    }

    /**
     * @param shops the shops to set
     */
    public static void setShops(HashMap<String, Inventory> shops) {
        Shops.shops = shops;
    }

    @SuppressWarnings("deprecation")
    static void loadShops() {
        int totalShops = Main.getShopsConfig().getConfigurationSection("shops").getKeys(false).size();
        int size = 9;
        while (size <= 54 && size <= totalShops) {
            size += 9;
        }
        Inventory mainInv = Bukkit.createInventory(null, size,
                ChatColor.translateAlternateColorCodes('&', Main.config.getString("shop-inventory-title")));
        int shopSlot = 0;
        for (String shop : Main.getShopsConfig().getConfigurationSection("shops").getKeys(false)) {
            try {
                ItemStack logo;
                int totalItems = Main.getShopsConfig().getConfigurationSection("shops." + shop + ".items").getKeys(false).size();
                int shopSize = 9;
                while (shopSize <= 54 && shopSize <= totalItems) {
                    shopSize += 9;
                }
                String shopName = Main.getShopsConfig().getString("shops." + shop + ".name");
                Inventory shopInv = Bukkit.createInventory(null, shopSize,
                        ChatColor.translateAlternateColorCodes('&', Main.config.getString("shop-inventory-title")));
                int itemSlot = 0;
                for (String item : Main.getShopsConfig().getConfigurationSection("shops." + shop + ".items")
                        .getKeys(false)) {
                    try {
                        String[] data = Main.getShopsConfig().getString("shops." + shop + ".items." + item).split(",");
                        int id = Integer.parseInt(data[0].split(":")[0]);
                        if (id > 0) {
                            int typeData = Integer.parseInt(data[0].split(":")[1]);
                            ItemStack invItem;
                            ItemMeta im;
                            String spawnerName = null;
                            if (id != 383 && id != 52) {
                                invItem = new ItemStack(id, 1, (short) typeData);
                                im = invItem.getItemMeta();
                            } else {
                                invItem = craftSpawnerItemStack(convertDataToEntityType(typeData));
                                im = invItem.getItemMeta();
                                im.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                                        Main.config.getString("spawner-name")
                                        .replaceAll("<spawner>", convertDataToEntityType(typeData).getName())));
                                spawnerName = convertDataToEntityType(typeData).getName();
                            }
                            if (id != 0) {
                                List<String> lore = new ArrayList<>();
                                int buyAmount = Integer.parseInt(data[1]);
                                boolean canBuy = true, canSell = true;
                                double buyPrice = 0;
                                try {
                                    buyPrice = Double.parseDouble(data[2]);
                                    lore.add(Main.replaceRegex(Main.config.getString("item-buy-lore"), buyAmount, buyPrice));
                                    if (buyAmount < 64 && Boolean.parseBoolean(Main.config.getString("enable-stack-buying"))) {
                                        double stackPrice = (buyPrice / buyAmount) * 64D;
                                        lore.add(Main.replaceRegex(Main.config.getString("stack-buy-lore"), 64, stackPrice));
                                    }
                                } catch (Exception e) {
                                    lore.add(ChatColor.RED + "Item Cannot be bought");
                                    canBuy = false;
                                }
                                int sellAmount = Integer.parseInt(data[3]);
                                double sellPrice = 0;
                                try {
                                    sellPrice = Double.parseDouble(data[4]);
                                    lore.add(Main.replaceRegex(Main.config.getString("item-sell-lore"), sellAmount, sellPrice));
                                    if (sellAmount < 64 && Boolean.parseBoolean(Main.config.getString("enable-stack-buying"))) {
                                        double stackPrice = (sellPrice / sellAmount) * 64D;
                                        lore.add(Main.replaceRegex(Main.config.getString("stack-sell-lore"), 64, stackPrice));
                                    }
                                } catch (Exception e) {
                                    lore.add(ChatColor.RED + "Item Cannot be sold");
                                    canSell = false;
                                }
                                im.setLore(lore);
                                invItem.setItemMeta(im);
                                NBTItem nbt = new NBTItem(invItem);
                                if (spawnerName != null) {
                                    nbt.setString("shopSpawner", spawnerName);
                                }
                                if (canBuy) {
                                    nbt.setString("buy", buyAmount + "," + buyPrice);
                                }
                                if (canSell) {
                                    nbt.setString("sell", sellAmount + "," + sellPrice);
                                }
                                for (int i = 5; i <= 6; i++) {
                                    if (data.length >= i + 1) {
                                        if (!data[i].equals("-") && data[i].contains(":") && data[i].contains("[") && data[i].contains("]")) {
                                            Bukkit.broadcastMessage(data[i].substring(data[i].indexOf('[') + 1, data[i].indexOf("]")));
                                            switch (data[i].substring(0, data[i].indexOf(':')).toLowerCase()) {
                                                case "buy":
                                                    nbt.setString("buyCommands", data[i].substring(data[i].indexOf('[') + 1, data[i].indexOf("]")));
                                                case "sell":
                                                    nbt.setString("sellCommands", data[i].substring(data[i].indexOf('[') + 1, data[i].indexOf("]")));
                                            }
                                        }
                                    }
                                }
                                invItem = nbt.getItem();
                            }
                            shopInv.setItem(itemSlot, invItem);
                        }
                        itemSlot++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                logo = shopInv.getItem(0);
                if (logo != null) {
                    logo = logo.clone();
                    ItemMeta im = logo.getItemMeta();
                    im.setDisplayName(ChatColor.translateAlternateColorCodes('&', shopName));
                    im.setLore(new ArrayList<>());
                    logo.setItemMeta(im);
                    NBTItem nbt = new NBTItem(logo);
                    nbt.setString("shop", shop);
                    logo = nbt.getItem();
                    shops.put(shop, shopInv);
                }
                mainInv.setItem(shopSlot, logo);
                shopSlot++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        shops.put("customshop{main-inventory($$$)}", mainInv);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        if (inv == null) {
            return;
        } else if (!inv.getName().equals(ChatColor.translateAlternateColorCodes('&', Main.config.getString("shop-inventory-title")))) {
            return;
        }
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }
        NBTItem nbt = new NBTItem(item);
        if (nbt.getString("shop") != null) {
            if (!(nbt.getString("shop").equals(""))) {
                String shop = nbt.getString("shop");
                if (PermCheck.hasAccessPerm(p, Main.getShopsConfig().getString("shops." + shop + ".permission"))) {
                    p.openInventory(getShop(shop));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.pl, () -> {
                        accessingPlayers.add(p.getUniqueId());
                    }, 5L);
                    return;
                } else {
                    p.sendMessage(ChatColor.RED + "You may not access this shop! Contact the server administrator for more info");
                }
            }
        }
        boolean shift = event.isShiftClick() && Boolean.parseBoolean(Main.config.getString("enable-stack-buying"));
        if (event.isLeftClick()) {
            if (nbt.getString("buy") != null) {
                if (!(nbt.getString("buy").equals(""))) {
                    String[] data = nbt.getString("buy").split(",");
                    int id = item.getTypeId();
                    int typeData = item.getDurability();
                    int amount = Integer.parseInt(data[0]);
                    double price = Double.parseDouble(data[1]);
                    ItemStack shopItem;
                    if (id != 383 && id != 52) {
                        shopItem = new ItemStack(id, amount, (short) typeData);
                    } else {
                        String name = nbt.getString("shopSpawner");
                        typeData = EntityType.fromName(ChatColor.stripColor(name)).getTypeId();
                        shopItem = craftSpawnerItemStack(convertDataToEntityType(typeData));
                        ItemMeta im = shopItem.getItemMeta();
                        im.setDisplayName(convertDataToEntityType(typeData).getName() + " Spawner");
                        shopItem.setItemMeta(im);
                    }
                    int commandIterations = 1;
                    int amountBought = shopItem.getAmount();
                    double moneyReceived = price;
                    if (!shift) {
                        if (Main.economy.getBalance(p) < price) {
                            p.sendMessage(ChatColor.RED + "You do not have enough money to buy this!");
                            return;
                        }
                        p.getInventory().addItem(shopItem);
                        Main.economy.withdrawPlayer(p, price);
                    } else {
                        price = (price / amount) * 64D;
                        if (Main.economy.getBalance(p) < price) {
                            p.sendMessage(ChatColor.RED + "You do not have enough money to buy a stack of this!");
                            return;
                        }
                        shopItem.setAmount(64);
                        p.getInventory().addItem(shopItem);
                        Main.economy.withdrawPlayer(p, price);
                        double commandIterationsDouble = 64 / amount;
                        if (commandIterations - ((int) commandIterationsDouble) > 0) {
                            commandIterations = ((int) commandIterationsDouble) + 1;
                        }
                        amountBought = 64;
                        moneyReceived = price;
                    }
                    if (!nbt.getString("buyCommands").equals("")) {
                        String commands = nbt.getString("buyCommands");
                        p.sendMessage(commands);
                        String[] commandArray;
                        if (commands.contains(Pattern.quote("</>"))) {
                            commandArray = commands.split(Pattern.quote("</>"));
                        } else {
                            commandArray = new String[1];
                            commandArray[1] = commands;
                        }
                        for (int i = 1; i <= commandIterations; i++) {
                            for (String command : commandArray) {
                                String dispatch = applyPlayerRegex(Main.replaceRegex(command, amountBought, moneyReceived), p);
                                p.sendMessage(dispatch);
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), dispatch);
                            }
                        }
                    }
                }
            }
        } else {
            if (nbt.getString("sell") != null) {
                if (!(nbt.getString("sell").equals(""))) {
                    String[] data = nbt.getString("sell").split(",");
                    int id = item.getTypeId();
                    int typeData;
                    int amount = Integer.parseInt(data[0]);
                    double price = Double.parseDouble(data[1]);
                    ItemStack shopItem;
                    if (!(id != 383 && id != 52)) {
                        String name = item.getItemMeta().getDisplayName().replaceAll("Spawner", "").replaceAll("CS", "")
                                .replaceAll(" ", "");
                        typeData = EntityType.fromName(ChatColor.stripColor(name)).getTypeId();
                        shopItem = craftSpawnerItemStack(convertDataToEntityType(typeData));
                        ItemMeta im = shopItem.getItemMeta();
                        im.setDisplayName(convertDataToEntityType(typeData).getName() + " Spawner");
                        shopItem.setItemMeta(im);
                    }
                    int commandIterations = 1;
                    int amountSold;
                    double moneySent;
                    if (shift) {
                        int filtered = filterInvItems(p, item, 64);
                        price = (price / amount) * filtered;
                        Main.economy.depositPlayer(p, price);
                        p.sendMessage(Main.replaceRegex(Main.config.getString("items-sold-message"), filtered, price));
                        amountSold = filtered;
                        moneySent = price;
                        double commandIterationsDouble = filtered / amount;
                        if (commandIterations - ((int) commandIterationsDouble) > 0) {
                            commandIterations = ((int) commandIterationsDouble) + 1;
                        }
                    } else {
                        int filtered = filterInvItems(p, item, amount);
                        if (filtered < amount) {
                            p.sendMessage(ChatColor.RED + "You do not have enough of this item!");
                        }
                        double moneyToSend = (amount / filtered) * price;
                        Main.economy.depositPlayer(p, moneyToSend);
                        p.sendMessage(Main.replaceRegex(Main.config.getString("items-sold-message"), filtered, moneyToSend));
                        amountSold = filtered;
                        moneySent = moneyToSend;
                        double commandIterationsDouble = filtered / amount;
                        if (commandIterations - ((int) commandIterationsDouble) > 0) {
                            commandIterations = ((int) commandIterationsDouble) + 1;
                        }
                    }
                    if (!nbt.getString("sellCommands").equals("")) {
                        String commands = nbt.getString("sellCommands");
                        String[] commandArray;
                        if (commands.contains(Pattern.quote("</>"))) {
                            commandArray = commands.split(Pattern.quote("</>"));
                        } else {
                            commandArray = new String[1];
                            commandArray[1] = commands;
                        }
                        for (int i = 1; i <= commandIterations; i++) {
                            for (String command : commandArray) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                        applyPlayerRegex(Main.replaceRegex(command, amountSold, moneySent), p));
                            }
                        }
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
            NBTItem nbt = new NBTItem(item);
            if (nbt.getString("spawner") != null) {
                if (!nbt.getString("spawner").equals("")) {
                    Block block = event.getBlockPlaced();
                    CreatureSpawner spawner = (CreatureSpawner) block.getState();
                    EntityType entityType = EntityType.valueOf(nbt.getString("spawner"));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.pl, () -> {
                        spawner.setSpawnedType(entityType);
                        event.getPlayer().sendMessage(Main.config.getString("spawner-placed-message")
                                .replaceAll("<spawner>", nbt.getString("spawner")));
                    }, 10L);
                }
            }
        }
    }

    private static List<UUID> accessingPlayers = new ArrayList<>();

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        final Player plr = (Player) event.getPlayer();
        if (inv.getName() != null) {
            if (inv.getName().equals(ChatColor.translateAlternateColorCodes('&', Main.config.getString("shop-inventory-title")))) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.pl, () -> {
                    if (accessingPlayers.contains(plr.getUniqueId())) {
                        plr.openInventory(getShop("customshop{main-inventory($$$)}"));
                        accessingPlayers.remove(plr.getUniqueId());
                    }
                }, 2L);
            }
        }
    }

    private static ItemStack craftSpawnerItemStack(EntityType type) {
        ItemStack item = new ItemStack(Material.MOB_SPAWNER);
        ItemMeta meta = item.getItemMeta();
        String spawnerType = type.toString();
        spawnerType = spawnerType.substring(0, 1).toUpperCase() + spawnerType.substring(1).toLowerCase();
        meta.setDisplayName(Main.config.getString("spawner-name").replaceAll("<spawner>", spawnerType));
        item.setItemMeta(meta);
        NBTItem nbt = new NBTItem(item);
        nbt.setString("spawner", spawnerType);
        item = nbt.getItem();
        return item;
    }

    @SuppressWarnings("deprecation")
    private static EntityType convertDataToEntityType(int data) {
        EntityType e = EntityType.fromId(data);
        return e;
    }

    private String applyPlayerRegex(String original, Player player) {
        return original.replaceAll("<player>", player.getName()).replaceAll("<uuid>", player.getUniqueId().toString());
    }
}
