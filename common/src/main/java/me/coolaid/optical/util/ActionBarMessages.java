package me.coolaid.optical.util;

import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ActionBarMessages {
    private static final long ZOOM_MESSAGE_REFRESH_NANOS = 500_000_000L;
    private static int lastZoomPercent = Integer.MIN_VALUE;
    private static long lastZoomMessageNanos;

    private ActionBarMessages() {
    }

    public static void showFreecam(boolean enabled) {
        OpticalConfig.ensureLoaded();
        if (OpticalConfig.ACTION_BAR_MESSAGES.isShowFreecamMessage()) {
            showStateMessage("optical.actionbar.feature.freecam", enabled);
        }
    }

    public static void showFreecamTripod(int slot) {
        showTripodMessage("optical.actionbar.message.tripod.set", slot);
    }

    public static void showFreecamTripodClosing(int slot) {
        showTripodMessage("optical.actionbar.message.tripod.closing", slot);
    }

    private static void showTripodMessage(String translationKey, int slot) {
        OpticalConfig.ensureLoaded();
        if (!OpticalConfig.ACTION_BAR_MESSAGES.isShowFreecamMessage()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        minecraft.player.sendOverlayMessage(Component.translatable(
                translationKey,
                Component.literal(Integer.toString(slot))));
    }

    public static void showZoom(int zoomPercent) {
        OpticalConfig.ensureLoaded();
        if (!OpticalConfig.ACTION_BAR_MESSAGES.isShowZoomMessage()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        zoomPercent = Math.max(0, Math.min(zoomPercent, 100));
        long now = System.nanoTime();
        if (zoomPercent == lastZoomPercent && now - lastZoomMessageNanos < ZOOM_MESSAGE_REFRESH_NANOS) {
            return;
        }

        lastZoomPercent = zoomPercent;
        lastZoomMessageNanos = now;
        minecraft.player.sendOverlayMessage(Component.translatable(
                "optical.actionbar.message.zoom",
                Component.literal(zoomPercent + "%").withStyle(ChatFormatting.WHITE)));
    }

    public static void showFreelook(boolean enabled) {
        OpticalConfig.ensureLoaded();
        if (OpticalConfig.ACTION_BAR_MESSAGES.isShowFreelookMessage()) {
            showStateMessage("optical.actionbar.feature.freelook", enabled);
        }
    }

    public static void showDetachedCamera(boolean enabled) {
        OpticalConfig.ensureLoaded();
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
