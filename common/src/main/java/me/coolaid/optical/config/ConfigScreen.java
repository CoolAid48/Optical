package me.coolaid.optical.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen {
    private static final Component TITLE = Component.translatable("optical.config.title");

    private ConfigScreen() {
    }

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(TITLE)
                .category(createCategory("optical.configCategory.general"))
                .category(createCategory("optical.configCategory.freelook"))
                .category(createCategory("optical.configCategory.freecam"))
                .category(createCategory("optical.configCategory.zoom"))
                .category(createCategory("optical.configCategory.gamma"))
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory createCategory(String name) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable(name))
                .build();
    }
}