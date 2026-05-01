package me.coolaid.optical.mixin;

import me.coolaid.optical.util.KeybindingVisibility;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;

@Mixin(KeyBindsList.class)
public class KeyBindsListMixin {
    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;"))
    private KeyMapping[] optical$hideDisabledFeatureKeybinds(Options options) {
        return Arrays.stream(options.keyMappings)
                .filter(KeybindingVisibility::isVisible)
                .toArray(KeyMapping[]::new);
    }
}
