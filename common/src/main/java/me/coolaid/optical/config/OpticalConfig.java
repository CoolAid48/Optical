package me.coolaid.optical.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import me.coolaid.optical.Optical;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OpticalConfig {
    public static final FreelookConfig FREELOOK = new FreelookConfig();
    public static final BrightnessConfig BRIGHTNESS = new BrightnessConfig();
    public static final FreecamConfig FREECAM = new FreecamConfig();
    public static final ZoomConfig ZOOM = new ZoomConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean brightnessLoaded;
    private static boolean suppressBrightnessSave;

    private OpticalConfig() {
    }

    public static void ensureBrightnessLoaded() {
        if (brightnessLoaded) {
            return;
        }
        brightnessLoaded = true;

        Path path = getBrightnessPath();
        if (!Files.exists(path)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            BrightnessPersisted persisted = GSON.fromJson(reader, BrightnessPersisted.class);
            if (persisted == null) {
                return;
            }

            suppressBrightnessSave = true;
            if (persisted.enabled != null) BRIGHTNESS.enabled = persisted.enabled;
            if (persisted.toggled != null) BRIGHTNESS.toggled = persisted.toggled;
            if (persisted.defaultLevel != null) BRIGHTNESS.defaultLevel = BRIGHTNESS.clampToRange(persisted.defaultLevel);
            if (persisted.toggledLevel != null) BRIGHTNESS.toggledLevel = BRIGHTNESS.clampToRange(persisted.toggledLevel);
            if (persisted.updateToggleValue != null) BRIGHTNESS.updateToggleValue = persisted.updateToggleValue;
            if (persisted.gammaStep != null) BRIGHTNESS.gammaStep = Mth.clamp(persisted.gammaStep, 1, 1000);
            if (persisted.showGammaMessage != null) BRIGHTNESS.showGammaMessage = persisted.showGammaMessage;
        } catch (IOException | JsonSyntaxException e) {
            Optical.LOGGER.warn("Failed to load brightness config from {}", path, e);
        } finally {
            suppressBrightnessSave = false;
        }
    }

    public static void saveBrightness() {
        if (!brightnessLoaded || suppressBrightnessSave) {
            return;
        }

        Path path = getBrightnessPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                BrightnessPersisted persisted = new BrightnessPersisted();
                persisted.enabled = BRIGHTNESS.enabled;
                persisted.toggled = BRIGHTNESS.toggled;
                persisted.defaultLevel = BRIGHTNESS.defaultLevel;
                persisted.toggledLevel = BRIGHTNESS.toggledLevel;
                persisted.updateToggleValue = BRIGHTNESS.updateToggleValue;
                persisted.gammaStep = BRIGHTNESS.gammaStep;
                persisted.showGammaMessage = BRIGHTNESS.showGammaMessage;
                GSON.toJson(persisted, writer);
            }
        } catch (IOException e) {
            Optical.LOGGER.warn("Failed to save brightness config to {}", path, e);
        }
    }

    private static Path getBrightnessPath() {
        Minecraft minecraft = Minecraft.getInstance();
        Path root = minecraft != null ? minecraft.gameDirectory.toPath() : Path.of(".");
        return root.resolve("config").resolve("optical-brightness.json");
    }

    private static final class BrightnessPersisted {
        private Boolean enabled;
        private Boolean toggled;
        private Integer defaultLevel;
        private Integer toggledLevel;
        private Boolean updateToggleValue;
        private Integer gammaStep;
        private Boolean showGammaMessage;
    }

    public static final class FreelookConfig {
        private boolean enabled = true;
        private boolean toggleMode = false;
        private boolean invertY = false;
        private double sensitivityMultiplier = 1.0D;
        private int rotationLimit = 360;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isToggleMode() { return this.toggleMode; }
        public void setToggleMode(boolean toggleMode) { this.toggleMode = toggleMode; }
        public boolean isInvertY() { return this.invertY; }
        public void setInvertY(boolean invertY) { this.invertY = invertY; }
        public double getSensitivityMultiplier() { return this.sensitivityMultiplier; }
        public void setSensitivityMultiplier(double sensitivityMultiplier) { this.sensitivityMultiplier = sensitivityMultiplier; }
        public int getRotationLimit() { return this.rotationLimit; }
        public void setRotationLimit(int rotationLimit) { this.rotationLimit = rotationLimit; }
    }

    public static final class FreecamConfig {
        public enum FlightMode {
            DEFAULT,
            CREATIVE
        }

        private boolean enabled = true;
        private boolean invertY = false;
        private FlightMode flightMode = FlightMode.DEFAULT;
        private boolean showDetachedPlayerName = true;
        private boolean showDetachedPlayerHand = true;
        private boolean collisionEnabled = false;
        private double horizontalSpeed = 1.0D;
        private double verticalSpeed = 1.0D;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isInvertY() { return this.invertY; }
        public void setInvertY(boolean invertY) { this.invertY = invertY; }
        public FlightMode getFlightMode() { return this.flightMode; }
        public void setFlightMode(FlightMode flightMode) { this.flightMode = flightMode == null ? FlightMode.DEFAULT : flightMode; }
        public boolean isShowDetachedPlayerName() { return this.showDetachedPlayerName; }
        public void setShowDetachedPlayerName(boolean showDetachedPlayerName) { this.showDetachedPlayerName = showDetachedPlayerName; }
        public boolean isShowDetachedPlayerHand() { return this.showDetachedPlayerHand; }
        public void setShowDetachedPlayerHand(boolean showDetachedPlayerHand) { this.showDetachedPlayerHand = showDetachedPlayerHand; }
        public boolean isCollisionEnabled() { return this.collisionEnabled; }
        public void setCollisionEnabled(boolean collisionEnabled) { this.collisionEnabled = collisionEnabled; }
        public double getHorizontalSpeed() { return this.horizontalSpeed; }
        public void setHorizontalSpeed(double horizontalSpeed) { this.horizontalSpeed = Mth.clamp(horizontalSpeed, 0.05D, 8.0D); }
        public double getVerticalSpeed() { return this.verticalSpeed; }
        public void setVerticalSpeed(double verticalSpeed) { this.verticalSpeed = Mth.clamp(verticalSpeed, 0.05D, 8.0D); }
    }

    public static final class BrightnessConfig {
        private boolean enabled = true;
        private boolean toggled = false;
        private int defaultLevel = 100;
        private int toggledLevel = 1500;
        private boolean updateToggleValue = true;
        private int gammaStep = 10;
        private boolean showGammaMessage = true;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; OpticalConfig.saveBrightness(); }
        public boolean isToggled() { return this.toggled; }
        public void setToggled(boolean toggled) { this.toggled = toggled; OpticalConfig.saveBrightness(); }
        public int getDefaultLevel() { return this.defaultLevel; }
        public void setDefaultLevel(int defaultLevel) { this.defaultLevel = clampToRange(defaultLevel); OpticalConfig.saveBrightness(); }
        public int getToggledLevel() { return this.toggledLevel; }
        public void setToggledLevel(int toggledLevel) { this.toggledLevel = clampToRange(toggledLevel); OpticalConfig.saveBrightness(); }
        public boolean isUpdateToggleValue() { return this.updateToggleValue; }
        public void setUpdateToggleValue(boolean updateToggleValue) { this.updateToggleValue = updateToggleValue; OpticalConfig.saveBrightness(); }
        public int getGammaStep() { return this.gammaStep; }
        public void setGammaStep(int gammaStep) { this.gammaStep = Mth.clamp(gammaStep, 1, 1000); OpticalConfig.saveBrightness(); }
        public boolean isShowGammaMessage() { return this.showGammaMessage; }
        public void setShowGammaMessage(boolean showGammaMessage) { this.showGammaMessage = showGammaMessage; OpticalConfig.saveBrightness(); }
        public int clampToRange(int value) {
            return Mth.clamp(value, -750, 1500);
        }
    }

    public static final class ZoomConfig {
        public enum TransitionMode {
            EXPONENTIAL,
            INSTANT
        }

        private boolean enabled = true;
        private boolean scrollAdjustEnabled = true;
        private boolean rememberZoomSteps = false;
        private double defaultZoomStrength = 3.0D;
        private double secondaryZoomStrength = 1.8D;
        private int scrollStepCount = 10;
        private double zoomPerStep = 1.5D;
        private int scrollZoomSmoothness = 70;
        private double zoomInSeconds = 1.0D;
        private double zoomOutSeconds = 0.5D;
        private double secondaryZoomInSeconds = 10.0D;
        private double secondaryZoomOutSeconds = 1.0D;
        private TransitionMode zoomInTransition = TransitionMode.EXPONENTIAL;
        private TransitionMode zoomOutTransition = TransitionMode.EXPONENTIAL;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isScrollAdjustEnabled() { return this.scrollAdjustEnabled; }
        public void setScrollAdjustEnabled(boolean scrollAdjustEnabled) { this.scrollAdjustEnabled = scrollAdjustEnabled; }
        public boolean isRememberZoomSteps() { return this.rememberZoomSteps; }
        public void setRememberZoomSteps(boolean rememberZoomSteps) { this.rememberZoomSteps = rememberZoomSteps; }
        public double getDefaultZoomStrength() { return this.defaultZoomStrength; }
        public void setDefaultZoomStrength(double defaultZoomStrength) {
            this.defaultZoomStrength = Mth.clamp(defaultZoomStrength, 1.0D, 10.0D);
        }
        public double getSecondaryZoomStrength() { return this.secondaryZoomStrength; }
        public void setSecondaryZoomStrength(double secondaryZoomStrength) {
            this.secondaryZoomStrength = Mth.clamp(secondaryZoomStrength, 1.0D, 10.0D);
        }
        public int getScrollStepCount() { return this.scrollStepCount; }
        public void setScrollStepCount(int scrollStepCount) { this.scrollStepCount = Mth.clamp(scrollStepCount, 1, 50); }
        public double getZoomPerStep() { return this.zoomPerStep; }
        public void setZoomPerStep(double zoomPerStep) { this.zoomPerStep = Mth.clamp(zoomPerStep, 1.05D, 3.0D); }
        public int getScrollZoomSmoothness() { return this.scrollZoomSmoothness; }
        public void setScrollZoomSmoothness(int scrollZoomSmoothness) { this.scrollZoomSmoothness = Mth.clamp(scrollZoomSmoothness, 0, 100); }
        public double getZoomInSeconds() { return this.zoomInSeconds; }
        public void setZoomInSeconds(double zoomInSeconds) { this.zoomInSeconds = Mth.clamp(zoomInSeconds, 0.1D, 5.0D); }
        public double getZoomOutSeconds() { return this.zoomOutSeconds; }
        public void setZoomOutSeconds(double zoomOutSeconds) { this.zoomOutSeconds = Mth.clamp(zoomOutSeconds, 0.1D, 5.0D); }
        public double getSecondaryZoomInSeconds() { return this.secondaryZoomInSeconds; }
        public void setSecondaryZoomInSeconds(double secondaryZoomInSeconds) {
            this.secondaryZoomInSeconds = Mth.clamp(secondaryZoomInSeconds, 0.1D, 60.0D);
        }
        public double getSecondaryZoomOutSeconds() { return this.secondaryZoomOutSeconds; }
        public void setSecondaryZoomOutSeconds(double secondaryZoomOutSeconds) {
            this.secondaryZoomOutSeconds = Mth.clamp(secondaryZoomOutSeconds, 0.1D, 60.0D);
        }
        public TransitionMode getZoomInTransition() { return this.zoomInTransition; }
        public void setZoomInTransition(TransitionMode zoomInTransition) {
            this.zoomInTransition = zoomInTransition == null ? TransitionMode.EXPONENTIAL : zoomInTransition;
        }
        public TransitionMode getZoomOutTransition() { return this.zoomOutTransition; }
        public void setZoomOutTransition(TransitionMode zoomOutTransition) {
            this.zoomOutTransition = zoomOutTransition == null ? TransitionMode.EXPONENTIAL : zoomOutTransition;
        }
        public double clampStrength(double value) {
            return Mth.clamp(value, 1.0D, 40.0D);
        }
    }
}