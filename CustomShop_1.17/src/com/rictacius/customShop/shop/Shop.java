package com.rictacius.customShop.shop;

import com.rictacius.customShop.Main;
import com.rictacius.customShop.config.ItemConfig;
import com.rictacius.customShop.config.PluginConfig;
import com.rictacius.customShop.config.ShopConfig;
import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Shop {
    private static final String NBT_ITEM_INDEX_KEY = "item-index";
    private static final String NBT_BUY_KEY = "buy";
    private static final String NBT_SELL_KEY = "sell";
    static final String NBT_SHOP_KEY = "shop";
    static final String NBT_IS_SHOP_KEY = "is-shop";

    private ShopConfig config;
    private final Inventory inventory;
    private ItemStack logo;

    Shop(ShopConfig config) {
        PluginConfig pluginConfig = Main.getPluginConfig();
        this.config = config;
        List<ItemConfig> itemConfigs = config.getItems();
        inventory = createInventory(itemConfigs.size());
        for (int itemSlot = 0; itemSlot < itemConfigs.size(); ++itemSlot) {
            ItemConfig itemConfig = itemConfigs.get(itemSlot);
            ItemStack item = createItemForInventory(pluginConfig, config.getName(), itemSlot, itemConfig);
            inventory.setItem(itemSlot, item);
        }
        logo = createLogoFromInventory(inventory, config);
    }

    Shop(List<Shop> childShops) {
        inventory = createInventory(childShops.size());
        for (int itemSlot = 0; itemSlot < childShops.size(); ++itemSlot) {
            Shop childShop = childShops.get(itemSlot);
            ItemStack logo = childShop.getLogo();
            inventory.setItem(itemSlot, logo);
        }
    }

    static String getInventoryName() {
        PluginConfig pluginConfig = Main.getPluginConfig();
        return ChatColor.translateAlternateColorCodes('&', pluginConfig.getShopInventoryTitle());
    }

    static String replaceAmountCurrencyPricePlaceholders(String original, Integer amount, Double price) {
        original = org.bukkit.ChatColor.translateAlternateColorCodes('&', original);
        original = original.replaceAll("<amount>", "" + amount).replaceAll("<price>", "" + price)
                .replaceAll("<currency>", Matcher.quoteReplacement(Main.getPluginConfig().getCurrency()));
        return original;
    }

    private Inventory createInventory(int numSlots) {
        int rowCount = numSlots / 9 + (numSlots % 9 > 0 ? 1 : 0);
        return Bukkit.createInventory(null, rowCount * 9, getInventoryName());
    }

    private ItemStack createItemForInventory(PluginConfig pluginConfig, String shopName, int itemIndex, ItemConfig itemConfig) {
        ItemStack item = new ItemStack(itemConfig.getMaterial(), 1);
        ItemMeta itemMeta = Main.plugin.getServer().getItemFactory().getItemMeta(item.getType());
        List<String> lore = new ArrayList<>();
        if (itemConfig.shouldAllowBuy()) {
            int buySize = itemConfig.getBuySize();
            double buyPrice = itemConfig.getBuyPrice();
            lore.add(replaceAmountCurrencyPricePlaceholders(pluginConfig.getItemBuyLore(), buySize, buyPrice));
            if (buySize < 64 && pluginConfig.shouldEnableStackBuying()) {
                double stackPrice = (buyPrice / buySize) * 64D;
                lore.add(replaceAmountCurrencyPricePlaceholders(pluginConfig.getStackBuyLore(), 64, stackPrice));
            }
        } else{
            lore.add(ChatColor.RED + "Item Cannot be bought");
        }
        if (itemConfig.shouldAllowSell()) {
            int sellSize = itemConfig.getSellSize();
            double sellPrice = itemConfig.getSellPrice();
            lore.add(replaceAmountCurrencyPricePlaceholders(pluginConfig.getItemSellLore(), sellSize, sellPrice));
            if (sellSize < 64 && pluginConfig.shouldEnableStackBuying()) {
                double stackPrice = (sellPrice / sellSize) * 64D;
                lore.add(replaceAmountCurrencyPricePlaceholders(pluginConfig.getStackSellLore(), 64, stackPrice));
            }
        } else {
            lore.add(ChatColor.RED + "Item Cannot be sold");
        }
        if (itemMeta != null) {
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
        }
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString(NBT_SHOP_KEY, shopName);
        nbtItem.setInteger(NBT_ITEM_INDEX_KEY, itemIndex);
        nbtItem.setBoolean(NBT_BUY_KEY, itemConfig.shouldAllowBuy());
        nbtItem.setBoolean(NBT_SELL_KEY, itemConfig.shouldAllowSell());
        return nbtItem.getItem();
    }

    private ItemStack createLogoFromInventory(Inventory inventory, ShopConfig shopConfig) {
        ItemStack logo = inventory.getItem(0);
        if (logo == null) {
            return null;
        }
        logo = logo.clone();
        ItemMeta itemMeta = logo.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', shopConfig.getFancyName()));
        itemMeta.setLore(new ArrayList<>());
        logo.setItemMeta(itemMeta);
        NBTItem nbtItem = new NBTItem(logo);
        nbtItem.setString(NBT_SHOP_KEY, shopConfig.getName());
        nbtItem.setBoolean(NBT_IS_SHOP_KEY, true);
        return nbtItem.getItem();
    }

    ShopConfig getConfig() {
        return config;
    }

    public Inventory getInventory() {
        return inventory;
    }

    private ItemStack getLogo() {
        return logo;
    }

    void onClick(InventoryClickEvent event, NBTItem nbtItem) {
        PluginConfig pluginConfig = Main.getPluginConfig();
        Player player = (Player) event.getWhoClicked();
        boolean isStackTransaction = event.isShiftClick() && pluginConfig.shouldEnableStackBuying();
        int itemIndex = nbtItem.getInteger(Shop.NBT_ITEM_INDEX_KEY);
        ItemConfig itemConfig = config.getItems().get(itemIndex);
        if (event.isLeftClick()) {
            buyItem(nbtItem, isStackTransaction, itemConfig, player);
        } else {
            sellItem(nbtItem, isStackTransaction, itemConfig, player);
        }
    }

    private void buyItem(NBTItem nbtItem, boolean isStackTransaction, ItemConfig itemConfig, Player player) {
        Economy economy = Main.getEconomy();
        boolean allowBuy = nbtItem.getBoolean(Shop.NBT_BUY_KEY);
        if (!allowBuy) {
            return;
        }
        int amountBought = isStackTransaction ? 64 : itemConfig.getBuySize();
        double price;
        if (isStackTransaction) {
            price = (itemConfig.getBuyPrice() / itemConfig.getBuySize()) * 64d;
        } else {
            price = itemConfig.getBuyPrice();
        }
        if (economy.getBalance(player) < price) {
            if (isStackTransaction) {
                player.sendMessage(ChatColor.RED + "You do not have enough money to buy a stack of this!");
            } else {
                player.sendMessage(ChatColor.RED + "You do not have enough money to buy this!");
            }
            return;
        }
        ItemStack shopItem = new ItemStack(itemConfig.getMaterial(), amountBought);
        player.getInventory().addItem(shopItem);
        economy.withdrawPlayer(player, price);
        String onItemBoughtCmd = getConfig().getOnItemBoughtCommand();
        if (onItemBoughtCmd != null) {
            player.performCommand(onItemBoughtCmd);
        }
    }

    private void sellItem(NBTItem nbtItem, boolean isStackTransaction, ItemConfig itemConfig, Player player) {
        Economy economy = Main.getEconomy();
        boolean allowSell = nbtItem.getBoolean(Shop.NBT_SELL_KEY);
        if (!allowSell) {
            return;
        }
        int amountToSell = isStackTransaction ? 64 : itemConfig.getSellSize();
        if (!player.getInventory().contains(itemConfig.getMaterial(), amountToSell)) {
            player.sendMessage(ChatColor.RED + "You do not have enough of this item!");
            return;
        }
        double playerEarnings = (itemConfig.getSellPrice() / itemConfig.getSellSize()) * amountToSell;
        removeMatchingItemsFromPlayersInventory(player, itemConfig.getMaterial(), amountToSell);
        economy.depositPlayer(player, playerEarnings);
        player.sendMessage(
                replaceAmountCurrencyPricePlaceholders(Main.getPluginConfig().getItemsSoldMessage(), amountToSell, playerEarnings)
        );
        String onItemSoldCmd = getConfig().getOnItemSoldCommand();
        if (onItemSoldCmd != null) {
            player.performCommand(onItemSoldCmd);
        }
    }

    private static void removeMatchingItemsFromPlayersInventory(Player player, Material material, int amountToRemove) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize() && amountToRemove > 0; ++slot) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack == null) {
                continue;
            }
            if (!itemStack.getType().equals(material)) {
                continue;
            }
            if (amountToRemove >= itemStack.getAmount()) {
                inventory.clear(slot);
            } else {
                itemStack.setAmount(itemStack.getAmount() - amountToRemove);
                inventory.setItem(slot, itemStack);
            }
            amountToRemove = amountToRemove - itemStack.getAmount();
        }
    }
}
