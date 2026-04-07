package me.coolaid.optical.neoforge;

import me.coolaid.optical.Optical;
import net.neoforged.fml.common.Mod;

@Mod(Optical.MOD_ID)
public final class OpticalNeoForge {
    public OpticalNeoForge() {
        // Run our common setup.
        Optical.init();
    }
}
