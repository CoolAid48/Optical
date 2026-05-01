package me.coolaid.optical.util;

import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ActionBarMessages {
    private ActionBarMessages() {
    }

    public static void showFreecam(boolean enabled) {
        OpticalConfig.ensureBrightnessLoaded();
        if (OpticalConfig.ACTION_BAR_MESSAGES.isShowFreecamMessage()) {
            showStateMessage("optical.actionbar.feature.freecam", enabled);
        }
    }

    public static void showFreelook(boolean enabled) {
        OpticalConfig.ensureBrightnessLoaded();
        if (OpticalConfig.ACTION_BAR_MESSAGES.isShowFreelookMessage()) {
            showStateMessage("optical.actionbar.feature.freelook", enabled);
        }
    }

    public static void showDetachedCamera(boolean enabled) {
        OpticalConfig.ensureBrightnessLoaded();
        if (OpticalConfig.ACTION_BAR_MESSAGES.isShowDetachedCameraMessage()) {
            showStateMessage("optical.actionbar.feature.detached_camera", enabled);
        }
    }

    private static void showStateMessage(String featureKey, boolean enabled) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        Component state = Component.translatable(enabled
                ? "optical.actionbar.state.enabled"
                : "optical.actionbar.state.disabled").withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
        minecraft.player.sendOverlayMessage(Component.translatable(
                "optical.actionbar.message.state",
                Component.translatable(featureKey),
                state));
    }
}
