package com.rictacius.customShop.config;

import org.bukkit.Material;

public interface ItemConfig {
    String getName();

    String getFancyName();

    Material getMaterial(); // item data? 234:data

    boolean shouldAllowBuy();

    boolean shouldAllowSell();

    int getBuySize();

    int getSellSize();

    double getBuyPrice();

    double getSellPrice();
}
