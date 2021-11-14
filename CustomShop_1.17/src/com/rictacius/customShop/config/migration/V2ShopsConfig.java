package com.rictacius.customShop.config.migration;

import com.rictacius.customShop.config.Config;
import com.rictacius.customShop.config.ConfigFileException;
import com.rictacius.customShop.config.ItemConfig;
import com.rictacius.customShop.config.ShopConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class V2ShopsConfig extends Config {
    private List<ShopConfig> v3ShopConfigs;

    V2ShopsConfig(File parentFolder, String fileName) throws ConfigFileException {
        super(parentFolder, fileName);
    }

    private void throwConfigFileException(String issue) throws ConfigFileException {
        throw new ConfigFileException("v2 shops.yml is invalid: " + issue);
    }

    @SuppressWarnings("SameParameterValue")
    private void throwConfigFileException(String issue, Throwable cause) throws ConfigFileException {
        throw new ConfigFileException("v2 shops.yml is invalid: " + issue, cause);
    }

    @Override
    protected void onConfigFileLoaded() throws ConfigFileException {
        v3ShopConfigs = new ArrayList<>();
        ConfigurationSection shopsSection = file.getConfigurationSection("shops");
        if (shopsSection == null) {
            throwConfigFileException("no 'shops' section");
        }
        for (String shopName : shopsSection.getKeys(false)) {
            ConfigurationSection shopSection = shopsSection.getConfigurationSection(shopName);
            assert shopSection != null;
            ShopConfig shopConfig = parseShopSection(shopName, shopSection);
            v3ShopConfigs.add(shopConfig);
        }
    }

    private ShopConfig parseShopSection(String shopName, ConfigurationSection shopSection) throws ConfigFileException {
        String fancyName = shopSection.getString("name");
        String requiredPermission = shopSection.getString("permission");
        ConfigurationSection itemsSection = shopSection.getConfigurationSection("items");
        if (itemsSection == null) {
            throwConfigFileException("no 'shops.[shop].items' section");
        }
        List<ItemConfig> itemConfigs = new ArrayList<>();
        for (String itemName : itemsSection.getKeys(false)) {
            String itemConfigStr = itemsSection.getString(itemName);
            if (itemConfigStr == null) {
                throwConfigFileException("invalid 'shops.[shop].items.[item]' type, expected String");
            }
            try {
                ItemConfig itemConfig = parseItemSection(itemName, itemConfigStr);
                itemConfigs.add(itemConfig);
            } catch (Exception e) {
                throwConfigFileException("invalid item config(s)", e);
            }
        }
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
                return null;
            }

            @Override
            public String getOnItemSoldCommand() {
                return null;
            }
        };
    }

    private ItemConfig parseItemSection(String itemName, String itemConfigStr) {
        // ID:DATA, AMOUNT_BUY, PRICE_BUY, AMOUNT_SELL, PRICE_SELL, buy:[COMMAND</>COMMAND], sell:[COMMAND</>COMMAND]
        // '313:0,1,1120,0,-'
        String[] parts = itemConfigStr.split(",");
        String[] typeParts = parts[0].split(":");
        int typeId = Integer.parseInt(typeParts[0]);
        int typeData = Integer.parseInt(typeParts[1]);
        Material material = convertLegacyTypeToMaterial(typeId, typeData);
        assert material != null;
        String amountBuy = parts[1];
        String priceBuy = parts[2];
        String amountSell = parts[3];
        String priceSell = parts[4];
        boolean allowBuy = true;
        boolean allowSell = true;
        int buySize = 0;
        int sellSize = 0;
        double buyPrice = 0;
        double sellPrice = 0;
        try {
            buySize = Integer.parseInt(amountBuy);
            buyPrice = Double.parseDouble(priceBuy);
        } catch (Exception e) {
            allowBuy = false;
        }
        try {
            sellSize = Integer.parseInt(amountSell);
            sellPrice = Double.parseDouble(priceSell);
        } catch (Exception e) {
            allowSell = false;
        }
        final boolean finalAllowBuy = allowBuy;
        final boolean finalAllowSell = allowSell;
        final int finalBuySize = buySize;
        final int finalSellSize = sellSize;
        final double finalBuyPrice = buyPrice;
        final double finalSellPrice = sellPrice;
        return new ItemConfig() {
            @Override
            public String getName() {
                return itemName;
            }

            @Override
            public String getFancyName() {
                return itemName;
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
                return finalBuySize;
            }

            @Override
            public int getSellSize() {
                return finalSellSize;
            }

            @Override
            public double getBuyPrice() {
                return finalBuyPrice;
            }

            @Override
            public double getSellPrice() {
                return finalSellPrice;
            }
        };
    }

    private Material convertLegacyTypeToMaterial(int typeId, int typeData) {
        for(Material material : Material.values()) {
            //noinspection deprecation
            if(material.getId() == typeId) {
                //noinspection deprecation
                return Bukkit.getUnsafe().fromLegacy(new MaterialData(material, (byte) typeData));
            }
        }
        return null;
    }

    YamlConfiguration getV3ShopsConfig() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection shopsSection = config.createSection("shops");
        for (ShopConfig shopConfig : v3ShopConfigs) {
            ConfigurationSection shopSection = shopsSection.createSection(shopConfig.getName());
            shopSection.set("name", shopConfig.getFancyName());
            shopSection.set("permission", shopConfig.getRequiredPermission());
            shopSection.set("on-item-bought-cmd", shopConfig.getOnItemBoughtCommand());
            shopSection.set("on-item-sold-cmd", shopConfig.getOnItemSoldCommand());
            ConfigurationSection itemsSection = shopSection.createSection("items");
            for (ItemConfig itemConfig : shopConfig.getItems()) {
                ConfigurationSection itemSection = itemsSection.createSection(itemConfig.getName());
                addV3ItemConfigValues(itemSection, itemConfig);
            }
        }
        return config;
    }

    private void addV3ItemConfigValues(ConfigurationSection itemSection, ItemConfig itemConfig) {
        itemSection.set("name", itemConfig.getFancyName());
        itemSection.set("material", itemConfig.getMaterial().toString());
        itemSection.set("allow-buy", itemConfig.shouldAllowBuy());
        itemSection.set("allow-sell", itemConfig.shouldAllowSell());
        itemSection.set("buy-size", itemConfig.getBuySize());
        itemSection.set("sell-size", itemConfig.getSellSize());
        itemSection.set("buy-price", itemConfig.getBuyPrice());
        itemSection.set("sell-price", itemConfig.getSellPrice());
    }
}
