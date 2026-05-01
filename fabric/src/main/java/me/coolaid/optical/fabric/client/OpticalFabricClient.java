package me.coolaid.optical.fabric.client;

import me.coolaid.optical.Keybindings;
import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.logic.Detached;
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
        KeyMappingHelper.registerKeyMapping(Keybindings.FREELOOK);
        KeyMappingHelper.registerKeyMapping(Keybindings.FREECAM_TOGGLE);
        KeyMappingHelper.registerKeyMapping(Keybindings.DETACH_CAMERA);
        KeyMappingHelper.registerKeyMapping(Keybindings.TOGGLE_BRIGHTNESS);
        KeyMappingHelper.registerKeyMapping(Keybindings.INCREASE_BRIGHTNESS);
        KeyMappingHelper.registerKeyMapping(Keybindings.DECREASE_BRIGHTNESS);
        KeyMappingHelper.registerKeyMapping(Keybindings.ZOOM);
        KeyMappingHelper.registerKeyMapping(Keybindings.SECONDARY_ZOOM);

        ClientTickEvents.START_CLIENT_TICK.register(Freecam::onPreClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(OpticalFabricClient::onClientTick);
    }

    private static void onClientTick(Minecraft minecraft) {
        Freelook.onClientCleanup(minecraft);
        Freecam.onClientCleanup(minecraft);

        while (Keybindings.FREECAM_TOGGLE.consumeClick()) {
            Freecam.toggle(minecraft);
        }

        while (Keybindings.DETACH_CAMERA.consumeClick()) {
            Detached.toggle(minecraft);
        }

        if (!Freecam.isActive()) {
            boolean keyDown = Keybindings.FREELOOK.isDown();
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
        Detached.onClientTick(minecraft);
        Gamma.onClientTick(minecraft);
        Zoom.onClientTick(minecraft);
    }
}
