package com.rictacius.customShop.shop;

import com.rictacius.customShop.Main;
import com.rictacius.customShop.PermCheck;
import com.rictacius.customShop.config.ItemConfig;
import com.rictacius.customShop.config.ShopConfig;
import com.rictacius.customShop.config.ShopsConfig;
import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class Shops implements Listener {
    private Shop mainMenu;
    private Map<String, Shop> shops = new HashMap<>();
    private Set<UUID> playersViewingAShop = new HashSet<>();
    private Map<Material, ItemConfig> materialItemConfigMap = new HashMap<>();

    public Shops() {
        loadShops();
    }

    public void loadShops() {
        ShopsConfig shopsConfig = Main.getShopsConfig();
        List<ShopConfig> shopConfigs = shopsConfig.getShopConfigs();
        List<Shop> shops = shopConfigs.stream().map(Shop::new).collect(Collectors.toList());
        mainMenu = new Shop(shops);
        this.shops.clear();
        this.materialItemConfigMap.clear();
        shops.forEach(shop -> this.shops.put(shop.getConfig().getName(), shop));
        this.shops.put(null, mainMenu);
        for (Shop shop : shops) {
            for (ItemConfig itemConfig : shop.getConfig().getItems()) {
                materialItemConfigMap.put(itemConfig.getMaterial(), itemConfig);
            }
        }
    }

    public Shop getMainMenu() {
        return mainMenu;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) {
            return;
        }
        if (event.getClickedInventory() == null || !event.getView().getTitle().equals(Shop.getInventoryName())) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) clicker;
        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }
        NBTItem nbtItem = new NBTItem(item);
        String shopName = nbtItem.getString(Shop.NBT_SHOP_KEY);
        if (shopName == null || shopName.equals("")) {
            return;
        }
        Shop shop = shops.get(shopName);
        String requiredPermissionForShop = shop.getConfig().getRequiredPermission();
        if (requiredPermissionForShop != null && !PermCheck.hasAccessPerm(player, requiredPermissionForShop)) {
            player.sendMessage(ChatColor.RED + "You may not access this shop! Contact the server administrator for more info");
        }
        if (nbtItem.getBoolean(Shop.NBT_IS_SHOP_KEY)) {
            player.openInventory(shop.getInventory());
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                    Main.plugin, () -> playersViewingAShop.add(player.getUniqueId()),
                    2L
            );
            return;
        }
        shop.onClick(event, nbtItem);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        HumanEntity entity = event.getPlayer();
        if (!(entity instanceof Player)) {
            return;
        }
        if (!event.getView().getTitle().equals(Shop.getInventoryName())) {
            return;
        }
        Player player = (Player) entity;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
            if (playersViewingAShop.contains(player.getUniqueId())) {
                player.openInventory(mainMenu.getInventory());
                playersViewingAShop.remove(player.getUniqueId());
            }
        }, 2L);
    }

    @Nullable
    ItemConfig getConfigForMaterial(Material material) {
        return materialItemConfigMap.get(material);
    }
}
