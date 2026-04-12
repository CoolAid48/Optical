package me.coolaid.optical.fabric.client;

import me.coolaid.optical.Brightness;
import me.coolaid.optical.Freelook;
import me.coolaid.optical.OpticalBindings;
import me.coolaid.optical.config.OpticalConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;

public final class OpticalFabricClient implements ClientModInitializer {
    private static boolean freelookKeyWasDown;

    @Override
    public void onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(OpticalBindings.FREELOOK);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.TOGGLE_BRIGHTNESS);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.INCREASE_BRIGHTNESS);
        KeyMappingHelper.registerKeyMapping(OpticalBindings.DECREASE_BRIGHTNESS);

        ClientTickEvents.END_CLIENT_TICK.register(OpticalFabricClient::onClientTick);
    }

    private static void onClientTick(Minecraft minecraft) {
        Freelook.onClientCleanup(minecraft);

        boolean keyDown = OpticalBindings.FREELOOK.isDown();
        if (OpticalConfig.FREELOOK.isToggleMode()) {
            if (keyDown && !freelookKeyWasDown) {
                Freelook.toggle(minecraft);
            }
        } else {
            Freelook.updateFromKeyState(minecraft, keyDown);
        }
        freelookKeyWasDown = keyDown;

        Brightness.onClientTick(minecraft);
    }
}