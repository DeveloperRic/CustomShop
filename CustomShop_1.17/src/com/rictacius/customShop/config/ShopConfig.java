package com.rictacius.customShop.config;

import javax.annotation.Nullable;
import java.util.List;

public interface ShopConfig {
    String getName();

    String getFancyName();

    @Nullable()
    String getRequiredPermission();

    List<ItemConfig> getItems();

    String getOnItemBoughtCommand();

    String getOnItemSoldCommand();
}
