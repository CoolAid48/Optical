package me.coolaid.optical.logic;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.optical.Keybindings;
import me.coolaid.optical.config.OpticalConfig;
import me.coolaid.optical.util.ActionBarMessages;
import me.coolaid.optical.util.FreecamCameraEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public final class Freecam {
    private static final int TRIPOD_SLOT_COUNT = 3;

    private static boolean active = false;
    private static boolean changingCameraType = false;
    private static boolean freecamTogglePending = false;
    private static int nextSyntheticEntityId = -420;
    private static int activeTripodSlot = 0;
    private static int lastTripodHotkeySlot = 0;
    private static int selectedHotbarSlotBeforeTick = -1;

    private static CameraType lastPerspective;
    private static CameraType activePerspective = CameraType.FIRST_PERSON;
    private static boolean lastSmartCull = true;
    private static FreecamCameraEntity cameraEntity;
    private static final Map<ResourceKey<Level>, TripodPosition[]> tripodPositions = new HashMap<>();

    private Freecam() {
    }

    public static boolean isActive() { return active; }
    public static boolean shouldRenderPlayerName() { return OpticalConfig.FREECAM.isShowDetachedPlayerName(); }
    public static boolean shouldRenderPlayerHand() { return OpticalConfig.FREECAM.isShowDetachedPlayerHand(); }
    public static boolean shouldControlCamera() { return active && cameraEntity != null; }
    public static CameraType getActiveCameraType() { return activePerspective; }
    public static Vec3 getPosition(float partialTick) {
        if (cameraEntity == null) return Vec3.ZERO;
        return new Vec3(
                Mth.lerp(partialTick, cameraEntity.xo, cameraEntity.getX()),
                Mth.lerp(partialTick, cameraEntity.yo, cameraEntity.getY()),
                Mth.lerp(partialTick, cameraEntity.zo, cameraEntity.getZ())
        );
    }
    public static float getYaw(float partialTick) {
        if (cameraEntity == null) return 0.0F;
        return cameraEntity.yRotO + partialTick * Mth.wrapDegrees(cameraEntity.getYRot() - cameraEntity.yRotO);
    }
    public static float getPitch(float partialTick) {
        return cameraEntity != null ? Mth.lerp(partialTick, cameraEntity.xRotO, cameraEntity.getXRot()) : 0.0F;
    }
    public static FreecamCameraEntity getCameraEntity() { return cameraEntity; }
    public static boolean isChangingCameraType() { return changingCameraType; }

    public static void addLookDelta(double xDelta, double yDelta) {
        if (!shouldControlCamera()) return;
        float pitchDelta = (float) (yDelta * 0.15F);
        float yawDelta = (float) (xDelta * 0.15F);
        float pitch = cameraEntity.getXRot();

        pitch = OpticalConfig.FREECAM.isInvertY()
                ? Mth.clamp(pitch - pitchDelta, -90.0f, 90.0f)
                : Mth.clamp(pitch + pitchDelta, -90.0f, 90.0f);

        cameraEntity.setXRot(pitch);
        cameraEntity.setYRot(cameraEntity.getYRot() + yawDelta);
        cameraEntity.xRotO = cameraEntity.getXRot();
        cameraEntity.yRotO = cameraEntity.getYRot();
        cameraEntity.yHeadRot = cameraEntity.getYRot();
        cameraEntity.yHeadRotO = cameraEntity.getYRot();
        cameraEntity.yBodyRot = cameraEntity.getYRot();
        cameraEntity.yBodyRotO = cameraEntity.getYRot();
        cameraEntity.xBob = cameraEntity.xBobO = cameraEntity.getXRot();
        cameraEntity.yBob = cameraEntity.yBobO = cameraEntity.getYRot();
    }

    public static void toggle(Minecraft minecraft) {
        if (!OpticalConfig.FREECAM.isEnabled()) return;
        if (active) deactivate(minecraft); else activate(minecraft);
    }

    public static void handleToggleKey(Minecraft minecraft) {
        if (!OpticalConfig.FREECAM.isEnabled()) {
            freecamTogglePending = false;
            lastTripodHotkeySlot = 0;
            drainFreecamToggleClicks();
            return;
        }

        if (handleTripodHotkey(minecraft)) {
            freecamTogglePending = false;
            drainFreecamToggleClicks();
            return;
        }

        while (Keybindings.FREECAM_TOGGLE.consumeClick()) {
            freecamTogglePending = true;
        }

        if (!Keybindings.FREECAM_TOGGLE.isDown() && freecamTogglePending) {
            freecamTogglePending = false;
            toggle(minecraft);
        }
    }

    public static void onClientTick(Minecraft minecraft) {
        if (!OpticalConfig.FREECAM.isEnabled() || minecraft.player == null || minecraft.level == null) {
            deactivate(minecraft);
            return;
        }

        if (!active || cameraEntity == null) return;
        if (cameraEntity.isRemoved() || cameraEntity.level() != minecraft.level) {
            deactivate(minecraft);
            return;
        }

        CameraType desiredPerspective = getDesiredCameraType(minecraft);
        if (activePerspective != desiredPerspective) activePerspective = desiredPerspective;
        if (minecraft.options.getCameraType() != desiredPerspective) setCameraType(minecraft, desiredPerspective);

        cameraEntity.setCollisionEnabled(OpticalConfig.FREECAM.isCollisionEnabled());
    }

    public static void onPreClientTick(Minecraft minecraft) {
        selectedHotbarSlotBeforeTick = getSelectedHotbarSlot(minecraft);

        if (!shouldControlCamera() || minecraft.player == null) return;
        if (!(minecraft.player.input instanceof KeyboardInput)) return;

        ClientInput input = new ClientInput();
        Input keyPresses = minecraft.player.input.keyPresses;
        input.keyPresses = new Input(false, false, false, false, false, keyPresses.shift(), false);
        minecraft.player.input = input;
    }

    public static void onClientCleanup(Minecraft minecraft) {
        if (active && (minecraft.player == null || minecraft.level == null)) {
            deactivate(minecraft);
        }
    }

    public static void onDisconnect(Minecraft minecraft) {
        deactivate(minecraft);
        tripodPositions.clear();
        activeTripodSlot = 0;
        lastTripodHotkeySlot = 0;
        freecamTogglePending = false;
        selectedHotbarSlotBeforeTick = -1;
    }

    private static void activate(Minecraft minecraft) {
        activate(minecraft, null, 0);
    }

    private static void activate(Minecraft minecraft, TripodPosition position, int tripodSlot) {
        if (active || minecraft.player == null || !(minecraft.level instanceof ClientLevel level)) return;
        Freelook.forceDeactivate(minecraft);
        Detached.deactivate();
        lastSmartCull = minecraft.smartCull;
        minecraft.smartCull = false;
        lastPerspective = minecraft.options.getCameraType();
        OpticalConfig.FreecamConfig.ActivationPerspective activationPerspective = OpticalConfig.FREECAM.getActivationPerspective();
        activePerspective = position != null ? position.cameraType() : activationPerspective.getCameraType();
        if (lastPerspective != activePerspective) setCameraType(minecraft, activePerspective);
        active = true;
        activeTripodSlot = tripodSlot;

        cameraEntity = new FreecamCameraEntity(level);
        cameraEntity.setId(nextFreecamEntityId(level));
        Vec3 detachPos = position != null
                ? position.position()
                : getActivationPosition(minecraft, activationPerspective);

        cameraEntity.setPos(detachPos.x, detachPos.y, detachPos.z);
        cameraEntity.setYRot(position != null ? position.yaw() : getActivationYaw(minecraft, activationPerspective));
        cameraEntity.setXRot(position != null ? position.pitch() : getActivationPitch(minecraft, activationPerspective));
        snapCameraHistory(cameraEntity);
        cameraEntity.setCollisionEnabled(OpticalConfig.FREECAM.isCollisionEnabled());

        level.addEntity(cameraEntity);
        minecraft.setCameraEntity(cameraEntity);
        ActionBarMessages.showFreecam(true);
    }

    public static void deactivate(Minecraft minecraft) {
        if (!active) return;
        int closingTripodSlot = activeTripodSlot;
        saveActiveTripodPosition(minecraft);
        active = false;
        activeTripodSlot = 0;
        minecraft.smartCull = lastSmartCull;
        if (minecraft.player != null) minecraft.setCameraEntity(minecraft.player);
        if (cameraEntity != null) { cameraEntity.remove(Entity.RemovalReason.DISCARDED); cameraEntity = null; }
        if (minecraft.player != null) minecraft.player.input = new KeyboardInput(minecraft.options);
        if (lastPerspective != null && minecraft.options.getCameraType() != lastPerspective) setCameraType(minecraft, lastPerspective);
        if (closingTripodSlot != 0) {
            ActionBarMessages.showFreecamTripodClosing(closingTripodSlot);
        } else {
            ActionBarMessages.showFreecam(false);
        }
    }

    private static boolean handleTripodHotkey(Minecraft minecraft) {
        int slot = pressedTripodSlot(minecraft);
        if (slot == 0) {
            lastTripodHotkeySlot = 0;
            return false;
        }

        if (slot == lastTripodHotkeySlot) {
            return true;
        }

        lastTripodHotkeySlot = slot;
        activateTripodSlot(minecraft, slot);
        restoreSelectedHotbarSlot(minecraft);
        return true;
    }

    private static int pressedTripodSlot(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || !Keybindings.FREECAM_TOGGLE.isDown()) {
            return 0;
        }

        if (InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_1)) return 1;
        if (InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_2)) return 2;
        if (InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_3)) return 3;
        return 0;
    }

    private static void activateTripodSlot(Minecraft minecraft, int slot) {
        if (minecraft.level == null) return;

        int index = slot - 1;
        TripodPosition[] slots = tripodSlots(minecraft.level);

        if (active && cameraEntity != null) {
            saveActiveTripodPosition(minecraft);

            TripodPosition position = slots[index];
            if (position == null || activeTripodSlot == slot) {
                position = captureCurrentCameraPosition(minecraft);
                slots[index] = position;
            }

            activeTripodSlot = slot;
            applyTripodPosition(minecraft, position);
            ActionBarMessages.showFreecamTripod(slot);
            return;
        }

        TripodPosition position = slots[index];
        if (position == null) {
            position = captureCurrentCameraPosition(minecraft);
            slots[index] = position;
        }

        activate(minecraft, position, slot);
        ActionBarMessages.showFreecamTripod(slot);
    }

    private static void saveActiveTripodPosition(Minecraft minecraft) {
        if (activeTripodSlot == 0 || cameraEntity == null) return;

        TripodPosition[] slots = tripodSlots(cameraEntity.level());
        slots[activeTripodSlot - 1] = captureCurrentCameraPosition(minecraft);
    }

    private static TripodPosition captureCurrentCameraPosition(Minecraft minecraft) {
        if (active && cameraEntity != null) {
            return new TripodPosition(
                    cameraEntity.position(),
                    cameraEntity.getYRot(),
                    cameraEntity.getXRot(),
                    activePerspective
            );
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        return new TripodPosition(
                camera.position(),
                camera.yRot(),
                camera.xRot(),
                OpticalConfig.FREECAM.getActivationCameraType()
        );
    }

    private static void applyTripodPosition(Minecraft minecraft, TripodPosition position) {
        if (cameraEntity == null) return;

        activePerspective = position.cameraType();
        if (minecraft.options.getCameraType() != activePerspective) setCameraType(minecraft, activePerspective);
        cameraEntity.setPos(position.position().x, position.position().y, position.position().z);
        cameraEntity.setYRot(position.yaw());
        cameraEntity.setXRot(position.pitch());
        snapCameraHistory(cameraEntity);
    }

    private static CameraType getDesiredCameraType(Minecraft minecraft) {
        if (activeTripodSlot != 0 && minecraft.level != null) {
            TripodPosition position = tripodSlots(minecraft.level)[activeTripodSlot - 1];
            if (position != null) {
                return position.cameraType();
            }
        }

        return OpticalConfig.FREECAM.getActivationCameraType();
    }

    private static Vec3 getActivationPosition(Minecraft minecraft, OpticalConfig.FreecamConfig.ActivationPerspective perspective) {
        Vec3 eyePosition = minecraft.player.getEyePosition();
        Vec3 bodyDirection = Vec3.directionFromRotation(0.0F, minecraft.player.yBodyRot);

        return switch (perspective) {
            case FIRST_PERSON -> eyePosition;
            case SECOND_PERSON -> eyePosition.add(bodyDirection.scale(4.0D));
            case THIRD_PERSON -> eyePosition.add(bodyDirection.scale(-4.0D));
        };
    }

    private static float getActivationYaw(Minecraft minecraft, OpticalConfig.FreecamConfig.ActivationPerspective perspective) {
        return perspective == OpticalConfig.FreecamConfig.ActivationPerspective.SECOND_PERSON
                ? Mth.wrapDegrees(minecraft.player.yBodyRot + 180.0F)
                : minecraft.player.yBodyRot;
    }

    private static float getActivationPitch(Minecraft minecraft, OpticalConfig.FreecamConfig.ActivationPerspective perspective) {
        return 0.0F;
    }

    private static int getSelectedHotbarSlot(Minecraft minecraft) {
        return minecraft.player == null ? -1 : minecraft.player.getInventory().getSelectedSlot();
    }

    private static void restoreSelectedHotbarSlot(Minecraft minecraft) {
        if (minecraft.player == null || selectedHotbarSlotBeforeTick < 0) {
            return;
        }

        if (minecraft.player.getInventory().getSelectedSlot() != selectedHotbarSlotBeforeTick) {
            minecraft.player.getInventory().setSelectedSlot(selectedHotbarSlotBeforeTick);
        }
    }

    private static TripodPosition[] tripodSlots(Level level) {
        return tripodPositions.computeIfAbsent(level.dimension(), ignored -> new TripodPosition[TRIPOD_SLOT_COUNT]);
    }

    private static void drainFreecamToggleClicks() {
        while (Keybindings.FREECAM_TOGGLE.consumeClick()) {
        }
    }

    private static void setCameraType(Minecraft minecraft, CameraType cameraType) {
        changingCameraType = true;
        try {
            minecraft.options.setCameraType(cameraType);
        } finally {
            changingCameraType = false;
        }
    }

    private static int nextFreecamEntityId(ClientLevel level) {
        for (int attempts = 0; attempts < 1024; attempts++) {
            int id = nextSyntheticEntityId--;
            if (nextSyntheticEntityId >= 0) {
                nextSyntheticEntityId = -1;
            }
            if (level.getEntity(id) == null) {
                return id;
            }
        }

        return nextSyntheticEntityId--;
    }

    private static void snapCameraHistory(FreecamCameraEntity camera) {
        camera.xo = camera.getX();
        camera.yo = camera.getY();
        camera.zo = camera.getZ();
        camera.xRotO = camera.getXRot();
        camera.yRotO = camera.getYRot();
        camera.yHeadRot = camera.getYRot();
        camera.yHeadRotO = camera.getYRot();
        camera.yBodyRot = camera.getYRot();
        camera.yBodyRotO = camera.getYRot();
        camera.xBob = camera.xBobO = camera.getXRot();
        camera.yBob = camera.yBobO = camera.getYRot();
    }

    private record TripodPosition(Vec3 position, float yaw, float pitch, CameraType cameraType) {
    }
}
