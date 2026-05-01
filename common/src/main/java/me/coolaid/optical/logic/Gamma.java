package me.coolaid.optical.logic;

import me.coolaid.optical.Keybindings;
import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public final class Gamma {
    private static Integer currentGamma;

    private Gamma() {
    }

    public static void onClientTick(Minecraft minecraft) {
        if (minecraft == null) {
            return;
        }

        OpticalConfig.ensureBrightnessLoaded();
        ensureCurrentValueInitialized();

        if (!OpticalConfig.BRIGHTNESS.isEnabled()) {
            currentGamma = OpticalConfig.BRIGHTNESS.getDefaultLevel();
            return;
        }

        while (Keybindings.TOGGLE_BRIGHTNESS.consumeClick()) {
            OpticalConfig.BRIGHTNESS.setToggled(!OpticalConfig.BRIGHTNESS.isToggled());
            currentGamma = OpticalConfig.BRIGHTNESS.isToggled()
                    ? OpticalConfig.BRIGHTNESS.getToggledLevel()
                    : OpticalConfig.BRIGHTNESS.getDefaultLevel();
            showGammaMessage(minecraft);
        }

        while (Keybindings.INCREASE_BRIGHTNESS.consumeClick()) {
            adjustGamma(OpticalConfig.BRIGHTNESS.getGammaStep());
            showGammaMessage(minecraft);
        }

        while (Keybindings.DECREASE_BRIGHTNESS.consumeClick()) {
            adjustGamma(-OpticalConfig.BRIGHTNESS.getGammaStep());
            showGammaMessage(minecraft);
        }

        if (!OpticalConfig.BRIGHTNESS.isUpdateToggleValue()) {
            currentGamma = OpticalConfig.BRIGHTNESS.clampToRange(currentGamma);
        } else {
            currentGamma = getTargetGamma();
        }
    }

    private static void adjustGamma(int amount) {
        ensureCurrentValueInitialized();
        if (OpticalConfig.BRIGHTNESS.isToggled()) {
            int current = OpticalConfig.BRIGHTNESS.isUpdateToggleValue() ? OpticalConfig.BRIGHTNESS.getToggledLevel() : currentGamma;
            int updated = OpticalConfig.BRIGHTNESS.clampToRange(current + amount);
            currentGamma = updated;
            if (OpticalConfig.BRIGHTNESS.isUpdateToggleValue()) {
                OpticalConfig.BRIGHTNESS.setToggledLevel(updated);
            }
        } else {
            int current = OpticalConfig.BRIGHTNESS.isUpdateToggleValue() ? OpticalConfig.BRIGHTNESS.getDefaultLevel() : currentGamma;
            int updated = OpticalConfig.BRIGHTNESS.clampToRange(current + amount);
            currentGamma = updated;
            if (OpticalConfig.BRIGHTNESS.isUpdateToggleValue()) {
                OpticalConfig.BRIGHTNESS.setDefaultLevel(updated);
            }
        }
    }

    public static double getCurrentGamma() {
        ensureCurrentValueInitialized();
        return currentGamma / 100.0D;
    }

    private static int getTargetGamma() {
        return OpticalConfig.BRIGHTNESS.isToggled() ? OpticalConfig.BRIGHTNESS.getToggledLevel() : OpticalConfig.BRIGHTNESS.getDefaultLevel();
    }

    private static void ensureCurrentValueInitialized() {
        if (currentGamma == null) {
            currentGamma = OpticalConfig.BRIGHTNESS.getDefaultLevel();
        }
    }

    private static void showGammaMessage(Minecraft minecraft) {
        if (!OpticalConfig.ACTION_BAR_MESSAGES.isShowGammaMessage() || minecraft.player == null) {
            return;
        }
        int level;
        if (!OpticalConfig.BRIGHTNESS.isUpdateToggleValue()) {
            level = currentGamma != null ? currentGamma : getTargetGamma();
        } else {
            level = OpticalConfig.BRIGHTNESS.isToggled() ? OpticalConfig.BRIGHTNESS.getToggledLevel() : OpticalConfig.BRIGHTNESS.getDefaultLevel();
        }
        Component value = Component.literal(level + "%").withStyle(getGammaColor(level));
        minecraft.player.sendOverlayMessage(Component.translatable("optical.gamma.message", value));
    }

    private static ChatFormatting getGammaColor(int level) {
        if (level < 0) {
            return ChatFormatting.RED;
        }
        if (level <= 100) {
            return ChatFormatting.GREEN;
        }
        return ChatFormatting.GOLD;
    }
}
