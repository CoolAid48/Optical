package me.coolaid.optical.neoforge;

import me.coolaid.optical.Optical;
import me.coolaid.optical.config.ConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Optical.MOD_ID, dist = Dist.CLIENT)
public final class OpticalNeoForge {
    public OpticalNeoForge(ModContainer container) {

        // Run our common setup.
        Optical.init();
        container.registerExtensionPoint(
                IConfigScreenFactory.class, (IConfigScreenFactory) (modContainer, parent) -> ConfigScreen.create(parent)
        );
    }
}
