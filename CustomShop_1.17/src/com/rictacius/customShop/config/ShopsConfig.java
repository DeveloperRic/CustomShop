package com.rictacius.customShop.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.rictacius.customShop.config.migration.Migration.SHOPS_CONFIG_FILE_NAME;

public class ShopsConfig extends Config {
    private List<ShopConfig> shopConfigs;

    public ShopsConfig(File parentFolder) throws ConfigFileException {
        super(parentFolder, SHOPS_CONFIG_FILE_NAME);
    }

    private void throwInvalidItemConfig(String shopName, String itemName, String problem) throws ConfigFileException {
        throwInvalidShopsConfig("(shop '" + shopName + "', item '" + itemName + "'): " + problem);
    }

    private void throwInvalidShopsConfig(String problem) throws ConfigFileException {
        throw new ConfigFileException(SHOPS_CONFIG_FILE_NAME + " is invalid: " + problem);
    }

    @Override
    protected void onConfigFileLoaded() throws ConfigFileException {
        loadShops();
    }

    private void loadShops() throws ConfigFileException {
        shopConfigs = new ArrayList<>();
        ConfigurationSection shopsSection = file.getConfigurationSection("shops");
        if (shopsSection == null) {
            throwInvalidShopsConfig("no 'shops' section");
        }
        for (String shopName : shopsSection.getKeys(false)) {
            ConfigurationSection shopConfig = shopsSection.getConfigurationSection(shopName);
            assert shopConfig != null;
            shopConfigs.add(loadShop(shopName, shopConfig));
        }
    }

    private ShopConfig loadShop(String shopName, ConfigurationSection shopConfig) throws ConfigFileException {
        String fancyName = shopConfig.getString("name");
        String requiredPermission = shopConfig.getString("permission");
        ConfigurationSection itemsConfig = shopConfig.getConfigurationSection("items");
        assert itemsConfig != null;
        List<ItemConfig> itemConfigs = loadItems(shopName, itemsConfig);
        String onItemBoughtCommand = shopConfig.getString("on-item-bought-cmd");
        String onItemSoldCommand = shopConfig.getString("on-item-sold-cmd");
        return new ShopConfig() {
            @Override
            public String getName() {
                return shopName;
            }

            @Override
            public String getFancyName() {
                return fancyName;
            }

            @Nullable
            @Override
            public String getRequiredPermission() {
                return requiredPermission;
            }

            @Override
            public List<ItemConfig> getItems() {
                return itemConfigs;
            }

            @Override
            public String getOnItemBoughtCommand() {
                return onItemBoughtCommand;
            }

            @Override
            public String getOnItemSoldCommand() {
                return onItemSoldCommand;
            }
        };
    }

    private List<ItemConfig> loadItems(String shopName, ConfigurationSection itemsConfig) throws ConfigFileException {
        List<ItemConfig> itemConfigs = new ArrayList<>();
        for (String itemName : itemsConfig.getKeys(false)) {
            ConfigurationSection itemSection = itemsConfig.getConfigurationSection(itemName);
            assert itemSection != null;
            itemConfigs.add(loadItem(shopName, itemName, itemSection));
        }
        return itemConfigs;
    }

    private ItemConfig loadItem(String shopName, String itemName, ConfigurationSection itemConfig) throws ConfigFileException {
        String fancyName = itemConfig.getString("name");
        String materialName = itemConfig.getString("material");
        if (materialName == null) {
            throwInvalidItemConfig(shopName, itemName, "item " + itemName + " is missing a material");
        }
        Material material = Material.matchMaterial(materialName);
        boolean allowBuy = itemConfig.getBoolean("allow-buy");
        boolean allowSell = itemConfig.getBoolean("allow-sell");
        if (!allowBuy && !allowSell) {
            throwInvalidItemConfig(shopName, itemName, "you can't disable both buying and selling of an item");
        }
        int buySize = itemConfig.getInt("buy-size");
        int sellSize = itemConfig.getInt("sell-size");
        if (allowBuy && buySize < 1) {
            allowBuy = false;
        }
        if (allowSell && sellSize < 1) {
            allowSell = false;
        }
        double buyPrice = itemConfig.getDouble("buy-price");
        double sellPrice = itemConfig.getDouble("sell-price");
        final boolean finalAllowBuy = allowBuy;
        final boolean finalAllowSell = allowSell;
        return new ItemConfig() {
            @Override
            public String getName() {
                return itemName;
            }

            @Override
            public String getFancyName() {
                return fancyName;
            }

            @Override
            public Material getMaterial() {
                return material;
            }

            @Override
            public boolean shouldAllowBuy() {
                return finalAllowBuy;
            }

            @Override
            public boolean shouldAllowSell() {
                return finalAllowSell;
            }

            @Override
            public int getBuySize() {
                return buySize;
            }

            @Override
            public int getSellSize() {
                return sellSize;
            }

            @Override
            public double getBuyPrice() {
                return buyPrice;
            }

            @Override
            public double getSellPrice() {
                return sellPrice;
            }
        };
    }

    public List<ShopConfig> getShopConfigs() {
        return shopConfigs;
    }
}
