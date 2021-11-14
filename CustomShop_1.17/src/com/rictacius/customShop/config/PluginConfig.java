package com.rictacius.customShop.config;

import java.io.File;

import static com.rictacius.customShop.config.migration.Migration.*;

public class PluginConfig extends Config {

    public PluginConfig(File parentFolder) throws ConfigFileException {
        super(parentFolder, CONFIG_FILE_NAME);
    }

    @Override
    protected void onConfigFileLoaded() throws ConfigFileException {
        int configVersion = getConfigVersion();
        if (configVersion < JAR_CONFIG_VERSION) {
            throw new ConfigFileException("config-version in " + CONFIG_FILE_NAME + " is out of date");
        } else if (configVersion > JAR_CONFIG_VERSION) {
            throw new ConfigFileException("This version of CustomShop cannot support your " + CONFIG_FILE_NAME + " config-version");
        }
    }

    private int getConfigVersion() {
        return file.getInt(CONFIG_VERSION_KEY);
    }

    public String getCurrency() {
        return file.getString("currency");
    }

    public String getShopPermission() {
        return file.getString("shop-perm");
    }

    public String getAdminPermission() {
        return file.getString("admin-perm");
    }

    public String getChestShopPermission() {
        return file.getString("chestshop-perm");
    }

    public String getItemsSoldMessage() {
        return file.getString("items-sold-message");
    }

    public String getNoPermissionMessage() {
        return file.getString("no-permission-message");
    }

    public String getUpdateFoundMessage() {
        return file.getString("update-found");
    }

    public String getSpanwerPlacedMessage() {
        return file.getString("spawner-placed-message");
    }

    public String getItemBuyLore() {
        return file.getString("item-buy-lore");
    }

    public String getStackBuyLore() {
        return file.getString("stack-buy-lore");
    }

    public String getItemSellLore() {
        return file.getString("item-sell-lore");
    }

    public String getStackSellLore() {
        return file.getString("stack-sell-lore");
    }

    public String getSpawnerName() {
        return file.getString("spawner-name");
    }

    public String getShopInventoryTitle() {
        return file.getString("shop-inventory-title");
    }

    public String getSellInventoryTitle() {
        return file.getString("sell-inventory-title");
    }

    public boolean shouldEnableStackBuying() {
        return file.getBoolean("enable-stack-buying");
    }
}
