package me.coolaid.optical.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
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
    public static final DetachedCameraConfig DETACHED_CAMERA = new DetachedCameraConfig();
    public static final BrightnessConfig BRIGHTNESS = new BrightnessConfig();
    public static final FreecamConfig FREECAM = new FreecamConfig();
    public static final ZoomConfig ZOOM = new ZoomConfig();
    public static final ActionBarMessagesConfig ACTION_BAR_MESSAGES = new ActionBarMessagesConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static boolean loaded;
    private static boolean suppressSave;

    private OpticalConfig() {
    }

    public static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;

        Path path = getConfigPath();
        if (Files.exists(path)) {
            loadConfig(path);
            return;
        }

        Path legacyPath = getLegacyBrightnessPath();
        if (Files.exists(legacyPath)) {
            loadLegacyBrightness(legacyPath);
            save();
        }
    }

    private static void loadConfig(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            PersistedConfig persisted = GSON.fromJson(reader, PersistedConfig.class);
            if (persisted == null) {
                return;
            }

            suppressSave = true;
            applyPersisted(persisted);
        } catch (IOException | JsonSyntaxException e) {
            Optical.LOGGER.warn("Failed to load config from {}", path, e);
        } finally {
            suppressSave = false;
        }
    }

    private static void loadLegacyBrightness(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            BrightnessPersisted persisted = GSON.fromJson(reader, BrightnessPersisted.class);
            if (persisted == null) {
                return;
            }

            suppressSave = true;
            applyLegacyBrightness(persisted);
        } catch (IOException | JsonSyntaxException e) {
            Optical.LOGGER.warn("Failed to load legacy brightness config from {}", path, e);
        } finally {
            suppressSave = false;
        }
    }

    public static void save() {
        if (!loaded || suppressSave) {
            return;
        }

        Path path = getConfigPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(createPersisted(), writer);
            }
        } catch (IOException e) {
            Optical.LOGGER.warn("Failed to save config to {}", path, e);
        }
    }

    private static void applyPersisted(PersistedConfig persisted) {
        if (persisted.freelook != null) {
            FreelookPersisted freelook = persisted.freelook;
            if (freelook.enabled != null) FREELOOK.enabled = freelook.enabled;
            if (freelook.toggleMode != null) FREELOOK.toggleMode = freelook.toggleMode;
            if (freelook.invertY != null) FREELOOK.invertY = freelook.invertY;
            if (freelook.sensitivityMultiplier != null) FREELOOK.sensitivityMultiplier = Mth.clamp(freelook.sensitivityMultiplier, 0.1D, 2.0D);
            if (freelook.rotationLimit != null) FREELOOK.rotationLimit = freelook.rotationLimit;
        }
        if (persisted.detachedCamera != null && persisted.detachedCamera.enabled != null) {
            DETACHED_CAMERA.enabled = persisted.detachedCamera.enabled;
        }
        if (persisted.brightness != null) {
            applyLegacyBrightness(persisted.brightness);
        }
        if (persisted.freecam != null) {
            FreecamPersisted freecam = persisted.freecam;
            if (freecam.enabled != null) FREECAM.enabled = freecam.enabled;
            if (freecam.invertY != null) FREECAM.invertY = freecam.invertY;
            if (freecam.flightMode != null) FREECAM.flightMode = freecam.flightMode;
            if (freecam.showDetachedPlayerName != null) FREECAM.showDetachedPlayerName = freecam.showDetachedPlayerName;
            if (freecam.showDetachedPlayerHand != null) FREECAM.showDetachedPlayerHand = freecam.showDetachedPlayerHand;
            if (freecam.collisionEnabled != null) FREECAM.collisionEnabled = freecam.collisionEnabled;
            if (freecam.horizontalSpeed != null) FREECAM.horizontalSpeed = Mth.clamp(freecam.horizontalSpeed, 0.05D, 8.0D);
            if (freecam.verticalSpeed != null) FREECAM.verticalSpeed = Mth.clamp(freecam.verticalSpeed, 0.05D, 8.0D);
        }
        if (persisted.zoom != null) {
            ZoomPersisted zoom = persisted.zoom;
            if (zoom.enabled != null) ZOOM.enabled = zoom.enabled;
            if (zoom.scrollAdjustEnabled != null) ZOOM.scrollAdjustEnabled = zoom.scrollAdjustEnabled;
            if (zoom.rememberZoomSteps != null) ZOOM.rememberZoomSteps = zoom.rememberZoomSteps;
            if (zoom.defaultZoomStrength != null) ZOOM.defaultZoomStrength = Mth.clamp(zoom.defaultZoomStrength, 1.0D, 10.0D);
            if (zoom.secondaryZoomStrength != null) ZOOM.secondaryZoomStrength = Mth.clamp(zoom.secondaryZoomStrength, 1.0D, 10.0D);
            if (zoom.scrollStepCount != null) ZOOM.scrollStepCount = Mth.clamp(zoom.scrollStepCount, 1, 50);
            if (zoom.zoomPerStep != null) ZOOM.zoomPerStep = Mth.clamp(zoom.zoomPerStep, 1.05D, 3.0D);
            if (zoom.scrollZoomSmoothness != null) ZOOM.scrollZoomSmoothness = Mth.clamp(zoom.scrollZoomSmoothness, 0, 100);
            if (zoom.zoomInSeconds != null) ZOOM.zoomInSeconds = Mth.clamp(zoom.zoomInSeconds, 0.1D, 5.0D);
            if (zoom.zoomOutSeconds != null) ZOOM.zoomOutSeconds = Mth.clamp(zoom.zoomOutSeconds, 0.1D, 5.0D);
            if (zoom.secondaryZoomInSeconds != null) ZOOM.secondaryZoomInSeconds = Mth.clamp(zoom.secondaryZoomInSeconds, 0.1D, 60.0D);
            if (zoom.secondaryZoomOutSeconds != null) ZOOM.secondaryZoomOutSeconds = Mth.clamp(zoom.secondaryZoomOutSeconds, 0.1D, 60.0D);
            if (zoom.zoomInTransition != null) ZOOM.zoomInTransition = zoom.zoomInTransition;
            if (zoom.zoomOutTransition != null) ZOOM.zoomOutTransition = zoom.zoomOutTransition;
        }
        if (persisted.actionBarMessages != null) {
            ActionBarMessagesPersisted messages = persisted.actionBarMessages;
            if (messages.showGammaMessage != null) ACTION_BAR_MESSAGES.showGammaMessage = messages.showGammaMessage;
            if (messages.showFreecamMessage != null) ACTION_BAR_MESSAGES.showFreecamMessage = messages.showFreecamMessage;
            if (messages.showFreelookMessage != null) ACTION_BAR_MESSAGES.showFreelookMessage = messages.showFreelookMessage;
            if (messages.showDetachedCameraMessage != null) ACTION_BAR_MESSAGES.showDetachedCameraMessage = messages.showDetachedCameraMessage;
        }
    }

    private static void applyLegacyBrightness(BrightnessPersisted persisted) {
        if (persisted.enabled != null) BRIGHTNESS.enabled = persisted.enabled;
        if (persisted.toggled != null) BRIGHTNESS.toggled = persisted.toggled;
        if (persisted.defaultLevel != null) BRIGHTNESS.defaultLevel = BRIGHTNESS.clampToRange(persisted.defaultLevel);
        if (persisted.toggledLevel != null) BRIGHTNESS.toggledLevel = BRIGHTNESS.clampToRange(persisted.toggledLevel);
        if (persisted.updateToggleValue != null) BRIGHTNESS.updateToggleValue = persisted.updateToggleValue;
        if (persisted.gammaStep != null) BRIGHTNESS.gammaStep = Mth.clamp(persisted.gammaStep, 1, 1000);
        if (persisted.showGammaMessage != null) ACTION_BAR_MESSAGES.showGammaMessage = persisted.showGammaMessage;
        if (persisted.actionBarMessages != null) {
            ActionBarMessagesPersisted messages = persisted.actionBarMessages;
            if (messages.showGammaMessage != null) ACTION_BAR_MESSAGES.showGammaMessage = messages.showGammaMessage;
            if (messages.showFreecamMessage != null) ACTION_BAR_MESSAGES.showFreecamMessage = messages.showFreecamMessage;
            if (messages.showFreelookMessage != null) ACTION_BAR_MESSAGES.showFreelookMessage = messages.showFreelookMessage;
            if (messages.showDetachedCameraMessage != null) ACTION_BAR_MESSAGES.showDetachedCameraMessage = messages.showDetachedCameraMessage;
        }
    }

    private static PersistedConfig createPersisted() {
        PersistedConfig persisted = new PersistedConfig();
        persisted.freelook = new FreelookPersisted();
        persisted.freelook.enabled = FREELOOK.enabled;
        persisted.freelook.toggleMode = FREELOOK.toggleMode;
        persisted.freelook.invertY = FREELOOK.invertY;
        persisted.freelook.sensitivityMultiplier = FREELOOK.sensitivityMultiplier;
        persisted.freelook.rotationLimit = FREELOOK.rotationLimit;
        persisted.detachedCamera = new DetachedCameraPersisted();
        persisted.detachedCamera.enabled = DETACHED_CAMERA.enabled;
        persisted.brightness = new BrightnessPersisted();
        persisted.brightness.enabled = BRIGHTNESS.enabled;
        persisted.brightness.toggled = BRIGHTNESS.toggled;
        persisted.brightness.defaultLevel = BRIGHTNESS.defaultLevel;
        persisted.brightness.toggledLevel = BRIGHTNESS.toggledLevel;
        persisted.brightness.updateToggleValue = BRIGHTNESS.updateToggleValue;
        persisted.brightness.gammaStep = BRIGHTNESS.gammaStep;
        persisted.freecam = new FreecamPersisted();
        persisted.freecam.enabled = FREECAM.enabled;
        persisted.freecam.invertY = FREECAM.invertY;
        persisted.freecam.flightMode = FREECAM.flightMode;
        persisted.freecam.showDetachedPlayerName = FREECAM.showDetachedPlayerName;
        persisted.freecam.showDetachedPlayerHand = FREECAM.showDetachedPlayerHand;
        persisted.freecam.collisionEnabled = FREECAM.collisionEnabled;
        persisted.freecam.horizontalSpeed = FREECAM.horizontalSpeed;
        persisted.freecam.verticalSpeed = FREECAM.verticalSpeed;
        persisted.zoom = new ZoomPersisted();
        persisted.zoom.enabled = ZOOM.enabled;
        persisted.zoom.scrollAdjustEnabled = ZOOM.scrollAdjustEnabled;
        persisted.zoom.rememberZoomSteps = ZOOM.rememberZoomSteps;
        persisted.zoom.defaultZoomStrength = ZOOM.defaultZoomStrength;
        persisted.zoom.secondaryZoomStrength = ZOOM.secondaryZoomStrength;
        persisted.zoom.scrollStepCount = ZOOM.scrollStepCount;
        persisted.zoom.zoomPerStep = ZOOM.zoomPerStep;
        persisted.zoom.scrollZoomSmoothness = ZOOM.scrollZoomSmoothness;
        persisted.zoom.zoomInSeconds = ZOOM.zoomInSeconds;
        persisted.zoom.zoomOutSeconds = ZOOM.zoomOutSeconds;
        persisted.zoom.secondaryZoomInSeconds = ZOOM.secondaryZoomInSeconds;
        persisted.zoom.secondaryZoomOutSeconds = ZOOM.secondaryZoomOutSeconds;
        persisted.zoom.zoomInTransition = ZOOM.zoomInTransition;
        persisted.zoom.zoomOutTransition = ZOOM.zoomOutTransition;
        persisted.actionBarMessages = new ActionBarMessagesPersisted();
        persisted.actionBarMessages.showGammaMessage = ACTION_BAR_MESSAGES.showGammaMessage;
        persisted.actionBarMessages.showFreecamMessage = ACTION_BAR_MESSAGES.showFreecamMessage;
        persisted.actionBarMessages.showFreelookMessage = ACTION_BAR_MESSAGES.showFreelookMessage;
        persisted.actionBarMessages.showDetachedCameraMessage = ACTION_BAR_MESSAGES.showDetachedCameraMessage;
        return persisted;
    }

    private static Path getConfigPath() {
        Minecraft minecraft = Minecraft.getInstance();
        Path root = minecraft != null ? minecraft.gameDirectory.toPath() : Path.of(".");
        return root.resolve("config").resolve("optical.json");
    }

    private static Path getLegacyBrightnessPath() {
        Minecraft minecraft = Minecraft.getInstance();
        Path root = minecraft != null ? minecraft.gameDirectory.toPath() : Path.of(".");
        return root.resolve("config").resolve("optical-brightness.json");
    }

    private static final class PersistedConfig {
        private FreelookPersisted freelook;
        private DetachedCameraPersisted detachedCamera;
        private BrightnessPersisted brightness;
        private FreecamPersisted freecam;
        private ZoomPersisted zoom;
        private ActionBarMessagesPersisted actionBarMessages;
    }

    private static final class FreelookPersisted {
        private Boolean enabled;
        private Boolean toggleMode;
        private Boolean invertY;
        private Double sensitivityMultiplier;
        private Integer rotationLimit;
    }

    private static final class DetachedCameraPersisted {
        private Boolean enabled;
    }

    private static final class BrightnessPersisted {
        private Boolean enabled;
        private Boolean toggled;
        private Integer defaultLevel;
        private Integer toggledLevel;
        private Boolean updateToggleValue;
        private Integer gammaStep;
        private Boolean showGammaMessage;
        private ActionBarMessagesPersisted actionBarMessages;
    }

    private static final class FreecamPersisted {
        private Boolean enabled;
        private Boolean invertY;
        private FreecamConfig.FlightMode flightMode;
        private Boolean showDetachedPlayerName;
        private Boolean showDetachedPlayerHand;
        private Boolean collisionEnabled;
        private Double horizontalSpeed;
        private Double verticalSpeed;
    }

    private static final class ZoomPersisted {
        private Boolean enabled;
        private Boolean scrollAdjustEnabled;
        private Boolean rememberZoomSteps;
        private Double defaultZoomStrength;
        private Double secondaryZoomStrength;
        private Integer scrollStepCount;
        private Double zoomPerStep;
        private Integer scrollZoomSmoothness;
        private Double zoomInSeconds;
        private Double zoomOutSeconds;
        private Double secondaryZoomInSeconds;
        private Double secondaryZoomOutSeconds;
        private ZoomConfig.TransitionMode zoomInTransition;
        private ZoomConfig.TransitionMode zoomOutTransition;
    }

    private static final class ActionBarMessagesPersisted {
        private Boolean showGammaMessage;
        private Boolean showFreecamMessage;
        private Boolean showFreelookMessage;
        private Boolean showDetachedCameraMessage;
    }

    public static final class FreelookConfig {
        private boolean enabled = true;
        private boolean toggleMode = false;
        private boolean invertY = false;
        private double sensitivityMultiplier = 1.0D;
        private int rotationLimit = 360;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; OpticalConfig.save(); }
        public boolean isToggleMode() { return this.toggleMode; }
        public void setToggleMode(boolean toggleMode) { this.toggleMode = toggleMode; OpticalConfig.save(); }
        public boolean isInvertY() { return this.invertY; }
        public void setInvertY(boolean invertY) { this.invertY = invertY; OpticalConfig.save(); }
        public double getSensitivityMultiplier() { return this.sensitivityMultiplier; }
        public void setSensitivityMultiplier(double sensitivityMultiplier) { this.sensitivityMultiplier = Mth.clamp(sensitivityMultiplier, 0.1D, 2.0D); OpticalConfig.save(); }
        public int getRotationLimit() { return this.rotationLimit; }
        public void setRotationLimit(int rotationLimit) { this.rotationLimit = rotationLimit; OpticalConfig.save(); }
    }

    public static final class DetachedCameraConfig {
        private boolean enabled = true;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; OpticalConfig.save(); }
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
        private boolean showDetachedPlayerHand = false;
        private boolean collisionEnabled = false;
        private double horizontalSpeed = 1.0D;
        private double verticalSpeed = 1.0D;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; OpticalConfig.save(); }
        public boolean isInvertY() { return this.invertY; }
        public void setInvertY(boolean invertY) { this.invertY = invertY; OpticalConfig.save(); }
        public FlightMode getFlightMode() { return this.flightMode; }
        public void setFlightMode(FlightMode flightMode) { this.flightMode = flightMode == null ? FlightMode.DEFAULT : flightMode; OpticalConfig.save(); }
        public boolean isShowDetachedPlayerName() { return this.showDetachedPlayerName; }
        public void setShowDetachedPlayerName(boolean showDetachedPlayerName) { this.showDetachedPlayerName = showDetachedPlayerName; OpticalConfig.save(); }
        public boolean isShowDetachedPlayerHand() { return this.showDetachedPlayerHand; }
        public void setShowDetachedPlayerHand(boolean showDetachedPlayerHand) { this.showDetachedPlayerHand = showDetachedPlayerHand; OpticalConfig.save(); }
        public boolean isCollisionEnabled() { return this.collisionEnabled; }
        public void setCollisionEnabled(boolean collisionEnabled) { this.collisionEnabled = collisionEnabled; OpticalConfig.save(); }
        public double getHorizontalSpeed() { return this.horizontalSpeed; }
        public void setHorizontalSpeed(double horizontalSpeed) { this.horizontalSpeed = Mth.clamp(horizontalSpeed, 0.05D, 8.0D); OpticalConfig.save(); }
        public double getVerticalSpeed() { return this.verticalSpeed; }
        public void setVerticalSpeed(double verticalSpeed) { this.verticalSpeed = Mth.clamp(verticalSpeed, 0.05D, 8.0D); OpticalConfig.save(); }
    }

    public static final class BrightnessConfig {
        private boolean enabled = true;
        private boolean toggled = false;
        private int defaultLevel = 100;
        private int toggledLevel = 1500;
        private boolean updateToggleValue = true;
        private int gammaStep = 10;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; OpticalConfig.save(); }
        public boolean isToggled() { return this.toggled; }
        public void setToggled(boolean toggled) { this.toggled = toggled; OpticalConfig.save(); }
        public int getDefaultLevel() { return this.defaultLevel; }
        public void setDefaultLevel(int defaultLevel) { this.defaultLevel = clampToRange(defaultLevel); OpticalConfig.save(); }
        public int getToggledLevel() { return this.toggledLevel; }
        public void setToggledLevel(int toggledLevel) { this.toggledLevel = clampToRange(toggledLevel); OpticalConfig.save(); }
        public boolean isUpdateToggleValue() { return this.updateToggleValue; }
        public void setUpdateToggleValue(boolean updateToggleValue) { this.updateToggleValue = updateToggleValue; OpticalConfig.save(); }
        public int getGammaStep() { return this.gammaStep; }
        public void setGammaStep(int gammaStep) { this.gammaStep = Mth.clamp(gammaStep, 1, 1000); OpticalConfig.save(); }
        public int clampToRange(int value) {
            return Mth.clamp(value, -750, 1500);
        }
    }

    public static final class ActionBarMessagesConfig {
        private boolean showGammaMessage = true;
        private boolean showFreecamMessage = true;
        private boolean showFreelookMessage = false;
        private boolean showDetachedCameraMessage = false;

        public boolean isShowGammaMessage() { return this.showGammaMessage; }
        public void setShowGammaMessage(boolean showGammaMessage) { this.showGammaMessage = showGammaMessage; OpticalConfig.save(); }
        public boolean isShowFreecamMessage() { return this.showFreecamMessage; }
        public void setShowFreecamMessage(boolean showFreecamMessage) { this.showFreecamMessage = showFreecamMessage; OpticalConfig.save(); }
        public boolean isShowFreelookMessage() { return this.showFreelookMessage; }
        public void setShowFreelookMessage(boolean showFreelookMessage) { this.showFreelookMessage = showFreelookMessage; OpticalConfig.save(); }
        public boolean isShowDetachedCameraMessage() { return this.showDetachedCameraMessage; }
        public void setShowDetachedCameraMessage(boolean showDetachedCameraMessage) { this.showDetachedCameraMessage = showDetachedCameraMessage; OpticalConfig.save(); }
    }

    public static final class ZoomConfig {
        public enum TransitionMode {
            @SerializedName(value = "SMOOTH", alternate = "EXPONENTIAL")
            SMOOTH,
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
        private TransitionMode zoomInTransition = TransitionMode.SMOOTH;
        private TransitionMode zoomOutTransition = TransitionMode.SMOOTH;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; OpticalConfig.save(); }
        public boolean isScrollAdjustEnabled() { return this.scrollAdjustEnabled; }
        public void setScrollAdjustEnabled(boolean scrollAdjustEnabled) { this.scrollAdjustEnabled = scrollAdjustEnabled; OpticalConfig.save(); }
        public boolean isRememberZoomSteps() { return this.rememberZoomSteps; }
        public void setRememberZoomSteps(boolean rememberZoomSteps) { this.rememberZoomSteps = rememberZoomSteps; OpticalConfig.save(); }
        public double getDefaultZoomStrength() { return this.defaultZoomStrength; }
        public void setDefaultZoomStrength(double defaultZoomStrength) {
            this.defaultZoomStrength = Mth.clamp(defaultZoomStrength, 1.0D, 10.0D);
            OpticalConfig.save();
        }
        public double getSecondaryZoomStrength() { return this.secondaryZoomStrength; }
        public void setSecondaryZoomStrength(double secondaryZoomStrength) {
            this.secondaryZoomStrength = Mth.clamp(secondaryZoomStrength, 1.0D, 10.0D);
            OpticalConfig.save();
        }
        public int getScrollStepCount() { return this.scrollStepCount; }
        public void setScrollStepCount(int scrollStepCount) { this.scrollStepCount = Mth.clamp(scrollStepCount, 1, 50); OpticalConfig.save(); }
        public double getZoomPerStep() { return this.zoomPerStep; }
        public void setZoomPerStep(double zoomPerStep) { this.zoomPerStep = Mth.clamp(zoomPerStep, 1.05D, 3.0D); OpticalConfig.save(); }
        public int getScrollZoomSmoothness() { return this.scrollZoomSmoothness; }
        public void setScrollZoomSmoothness(int scrollZoomSmoothness) { this.scrollZoomSmoothness = Mth.clamp(scrollZoomSmoothness, 0, 100); OpticalConfig.save(); }
        public double getZoomInSeconds() { return this.zoomInSeconds; }
        public void setZoomInSeconds(double zoomInSeconds) { this.zoomInSeconds = Mth.clamp(zoomInSeconds, 0.1D, 5.0D); OpticalConfig.save(); }
        public double getZoomOutSeconds() { return this.zoomOutSeconds; }
        public void setZoomOutSeconds(double zoomOutSeconds) { this.zoomOutSeconds = Mth.clamp(zoomOutSeconds, 0.1D, 5.0D); OpticalConfig.save(); }
        public double getSecondaryZoomInSeconds() { return this.secondaryZoomInSeconds; }
        public void setSecondaryZoomInSeconds(double secondaryZoomInSeconds) {
            this.secondaryZoomInSeconds = Mth.clamp(secondaryZoomInSeconds, 0.1D, 60.0D);
            OpticalConfig.save();
        }
        public double getSecondaryZoomOutSeconds() { return this.secondaryZoomOutSeconds; }
        public void setSecondaryZoomOutSeconds(double secondaryZoomOutSeconds) {
            this.secondaryZoomOutSeconds = Mth.clamp(secondaryZoomOutSeconds, 0.1D, 60.0D);
            OpticalConfig.save();
        }
        public TransitionMode getZoomInTransition() { return this.zoomInTransition; }
        public void setZoomInTransition(TransitionMode zoomInTransition) {
            this.zoomInTransition = zoomInTransition == null ? TransitionMode.SMOOTH : zoomInTransition;
            OpticalConfig.save();
        }
        public TransitionMode getZoomOutTransition() { return this.zoomOutTransition; }
        public void setZoomOutTransition(TransitionMode zoomOutTransition) {
            this.zoomOutTransition = zoomOutTransition == null ? TransitionMode.SMOOTH : zoomOutTransition;
            OpticalConfig.save();
        }
        public double clampStrength(double value) {
            return Mth.clamp(value, 1.0D, 40.0D);
        }
    }
}
