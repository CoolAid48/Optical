package me.coolaid.optical.util;

import me.coolaid.optical.Keybindings;
import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.KeyMapping;

public final class KeybindingVisibility {
    private KeybindingVisibility() {
    }

    public static boolean isVisible(KeyMapping mapping) {
        OpticalConfig.ensureBrightnessLoaded();
        if (mapping == Keybindings.FREELOOK) {
            return OpticalConfig.FREELOOK.isEnabled();
        }
        if (mapping == Keybindings.FREECAM_TOGGLE) {
            return OpticalConfig.FREECAM.isEnabled();
        }
        if (mapping == Keybindings.DETACH_CAMERA) {
            return OpticalConfig.DETACHED_CAMERA.isEnabled();
        }
        if (mapping == Keybindings.TOGGLE_BRIGHTNESS
                || mapping == Keybindings.INCREASE_BRIGHTNESS
                || mapping == Keybindings.DECREASE_BRIGHTNESS) {
            return OpticalConfig.BRIGHTNESS.isEnabled();
        }
        if (mapping == Keybindings.ZOOM || mapping == Keybindings.SECONDARY_ZOOM) {
            return OpticalConfig.ZOOM.isEnabled();
        }
        return true;
    }
}
