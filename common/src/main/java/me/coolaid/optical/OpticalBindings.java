package me.coolaid.optical;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class OpticalBindings {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.parse(Optical.MOD_ID));

    public static final KeyMapping FREELOOK = new KeyMapping("key.optical.freelook", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    public static final KeyMapping FREECAM_TOGGLE = new KeyMapping("key.optical.freecam.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F6, CATEGORY);
    public static final KeyMapping FREECAM_MOMENTUM = new KeyMapping("key.optical.freecam.momentum", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F7, CATEGORY);
    public static final KeyMapping FREECAM_TRIPOD_1 = new KeyMapping("key.optical.freecam.tripod_1", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_1, CATEGORY);
    public static final KeyMapping FREECAM_TRIPOD_2 = new KeyMapping("key.optical.freecam.tripod_2", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_2, CATEGORY);
    public static final KeyMapping FREECAM_TRIPOD_3 = new KeyMapping("key.optical.freecam.tripod_3", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_3, CATEGORY);
    public static final KeyMapping TOGGLE_BRIGHTNESS = new KeyMapping("key.optical.brightness.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping INCREASE_BRIGHTNESS = new KeyMapping("key.optical.brightness.increase", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, CATEGORY);
    public static final KeyMapping DECREASE_BRIGHTNESS = new KeyMapping("key.optical.brightness.decrease", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, CATEGORY);
    public static final KeyMapping ZOOM = new KeyMapping("key.optical.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);
    public static final KeyMapping SECONDARY_ZOOM = new KeyMapping("key.optical.zoom.secondary", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

    private OpticalBindings() {
    }
}