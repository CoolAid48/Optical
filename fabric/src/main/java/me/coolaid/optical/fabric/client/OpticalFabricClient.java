package me.coolaid.optical.fabric.client;

import me.coolaid.optical.OpticalBindings;
import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.logic.Gamma;
import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.logic.Freelook;
import me.coolaid.optical.logic.Zoom;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;

public final class OpticalFabricClient implements ClientModInitializer {
    private static boolean freelookKeyWasDown;

    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(OpticalBindings.FREELOOK);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.FREECAM_TOGGLE);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.FREECAM_MOMENTUM);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.FREECAM_TRIPOD_1);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.FREECAM_TRIPOD_2);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.FREECAM_TRIPOD_3);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.TOGGLE_BRIGHTNESS);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.INCREASE_BRIGHTNESS);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.DECREASE_BRIGHTNESS);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.ZOOM);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.SECONDARY_ZOOM);

        ClientTickEvents.END_CLIENT_TICK.register(OpticalFabricClient::onClientTick);
    }

    private static void onClientTick(Minecraft minecraft) {
        Freelook.onClientCleanup(minecraft);
        Freecam.onClientCleanup(minecraft);

        while (OpticalBindings.FREECAM_TOGGLE.consumeClick()) {
            Freecam.toggle(minecraft);
        }

        while (OpticalBindings.FREECAM_MOMENTUM.consumeClick()) {
            Freecam.toggleMomentumMode();
        }

        while (OpticalBindings.FREECAM_TRIPOD_1.consumeClick()) {
            Freecam.handleTripod(0);
        }
        while (OpticalBindings.FREECAM_TRIPOD_2.consumeClick()) {
            Freecam.handleTripod(1);
        }
        while (OpticalBindings.FREECAM_TRIPOD_3.consumeClick()) {
            Freecam.handleTripod(2);
        }

        if (!Freecam.isActive()) {
            boolean keyDown = OpticalBindings.FREELOOK.isDown();
            if (OpticalConfig.FREELOOK.isToggleMode()) {
                if (keyDown && !freelookKeyWasDown) {
                    Freelook.toggle(minecraft);
                }
            } else {
                Freelook.updateFromKeyState(minecraft, keyDown);
            }
            freelookKeyWasDown = keyDown;
        } else {
            freelookKeyWasDown = false;
        }

        Freecam.onClientTick(minecraft);
        Gamma.onClientTick(minecraft);
        Zoom.onClientTick(minecraft);
    }
}