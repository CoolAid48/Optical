package me.coolaid.optical.fabric.client;

import me.coolaid.optical.Optical;
import net.fabricmc.api.ClientModInitializer;

public final class OpticalFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.

        // Run our common setup.
        Optical.init();
    }
}
