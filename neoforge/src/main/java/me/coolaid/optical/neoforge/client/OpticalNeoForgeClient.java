package me.coolaid.optical.neoforge.client;

import me.coolaid.optical.Optical;
import me.coolaid.optical.Keybindings;
import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.logic.Detached;
import me.coolaid.optical.logic.Freecam;
import me.coolaid.optical.logic.Freelook;
import me.coolaid.optical.logic.Gamma;
import me.coolaid.optical.logic.Zoom;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = Optical.MOD_ID, value = Dist.CLIENT)
public final class OpticalNeoForgeClient {
    private static boolean freelookKeyWasDown;

    private OpticalNeoForgeClient() {
    }

    @SubscribeEvent
    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(Keybindings.category());
        event.register(Keybindings.FREELOOK);
        event.register(Keybindings.FREECAM_TOGGLE);
        event.register(Keybindings.FREECAM_MOMENTUM);
        event.register(Keybindings.FREECAM_TRIPOD_1);
        event.register(Keybindings.FREECAM_TRIPOD_2);
        event.register(Keybindings.FREECAM_TRIPOD_3);
        event.register(Keybindings.DETACH_CAMERA);
        event.register(Keybindings.TOGGLE_BRIGHTNESS);
        event.register(Keybindings.INCREASE_BRIGHTNESS);
        event.register(Keybindings.DECREASE_BRIGHTNESS);
        event.register(Keybindings.ZOOM);
        event.register(Keybindings.SECONDARY_ZOOM);
    }

    @SubscribeEvent
    private static void onPreClientTick(ClientTickEvent.Pre event) {
        Freecam.onPreClientTick(Minecraft.getInstance());
    }

    @SubscribeEvent
    private static void onPostClientTick(ClientTickEvent.Post event) {
        onClientTick(Minecraft.getInstance());
    }

    private static void onClientTick(Minecraft minecraft) {
        Freelook.onClientCleanup(minecraft);
        Freecam.onClientCleanup(minecraft);

        while (Keybindings.FREECAM_TOGGLE.consumeClick()) {
            Freecam.toggle(minecraft);
        }

        while (Keybindings.FREECAM_MOMENTUM.consumeClick()) {
            Freecam.toggleMomentumMode();
        }

        while (Keybindings.FREECAM_TRIPOD_1.consumeClick()) {
            Freecam.handleTripod(0);
        }
        while (Keybindings.FREECAM_TRIPOD_2.consumeClick()) {
            Freecam.handleTripod(1);
        }
        while (Keybindings.FREECAM_TRIPOD_3.consumeClick()) {
            Freecam.handleTripod(2);
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
