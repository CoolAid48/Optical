package me.coolaid.optical;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class OpticalBindings {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.parse(Optical.MOD_ID));

    public static final KeyMapping FREELOOK = new KeyMapping("key.optical.freelook", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);

    public static final KeyMapping TOGGLE_BRIGHTNESS = new KeyMapping("key.optical.brightness.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping INCREASE_BRIGHTNESS = new KeyMapping("key.optical.brightness.increase", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, CATEGORY);
    public static final KeyMapping DECREASE_BRIGHTNESS = new KeyMapping("key.optical.brightness.decrease", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, CATEGORY);

    private OpticalBindings() {
    }
}