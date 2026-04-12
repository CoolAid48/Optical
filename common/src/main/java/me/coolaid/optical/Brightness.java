package me.coolaid.optical;

import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class Brightness {

    private Brightness() {
    }

    public static void onClientTick(Minecraft minecraft) {
        if (!OpticalConfig.BRIGHTNESS.isEnabled()) {
            return;
        }

        while (OpticalBindings.TOGGLE_BRIGHTNESS.consumeClick()) {
            OpticalConfig.BRIGHTNESS.setToggled(!OpticalConfig.BRIGHTNESS.isToggled());
        }

        while (OpticalBindings.INCREASE_BRIGHTNESS.consumeClick()) {
            adjustGamma(10);
        }

        while (OpticalBindings.DECREASE_BRIGHTNESS.consumeClick()) {
            adjustGamma(-10);
        }
    }

    private static void adjustGamma(int amount) {
        if (OpticalConfig.BRIGHTNESS.isToggled()) {
            int current = OpticalConfig.BRIGHTNESS.getToggledLevel();
            OpticalConfig.BRIGHTNESS.setToggledLevel(Mth.clamp(current + amount, -750, 1500));
        } else {
            int current = OpticalConfig.BRIGHTNESS.getDefaultLevel();
            OpticalConfig.BRIGHTNESS.setDefaultLevel(Mth.clamp(current + amount, -750, 1500));
        }
    }

    public static double getCurrentGamma() {
        int level = OpticalConfig.BRIGHTNESS.isToggled() ? OpticalConfig.BRIGHTNESS.getToggledLevel() : OpticalConfig.BRIGHTNESS.getDefaultLevel();
        return level / 100.0D;
    }
}