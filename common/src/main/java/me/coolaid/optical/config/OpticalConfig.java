package me.coolaid.optical.config;

import net.minecraft.util.Mth;

public final class OpticalConfig {
    public static final FreelookConfig FREELOOK = new FreelookConfig();
    public static final BrightnessConfig BRIGHTNESS = new BrightnessConfig();
    public static final FreecamConfig FREECAM = new FreecamConfig();

    private OpticalConfig() {
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
        private boolean enabled = true;
        private boolean invertY = false;
        private double sensitivityMultiplier = 1.0D;
        private boolean momentumByDefault = false;
        private double horizontalSpeed = 0.8D;
        private double verticalSpeed = 0.6D;
        private double momentumSmoothing = 0.25D;
        private double momentumDamping = 0.9D;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isInvertY() { return this.invertY; }
        public void setInvertY(boolean invertY) { this.invertY = invertY; }
        public double getSensitivityMultiplier() { return this.sensitivityMultiplier; }
        public void setSensitivityMultiplier(double sensitivityMultiplier) { this.sensitivityMultiplier = Mth.clamp(sensitivityMultiplier, 0.1D, 4.0D); }
        public boolean isMomentumByDefault() { return this.momentumByDefault; }
        public void setMomentumByDefault(boolean momentumByDefault) { this.momentumByDefault = momentumByDefault; }
        public double getHorizontalSpeed() { return this.horizontalSpeed; }
        public void setHorizontalSpeed(double horizontalSpeed) { this.horizontalSpeed = Mth.clamp(horizontalSpeed, 0.05D, 8.0D); }
        public double getVerticalSpeed() { return this.verticalSpeed; }
        public void setVerticalSpeed(double verticalSpeed) { this.verticalSpeed = Mth.clamp(verticalSpeed, 0.05D, 8.0D); }
        public double getMomentumSmoothing() { return this.momentumSmoothing; }
        public void setMomentumSmoothing(double momentumSmoothing) { this.momentumSmoothing = Mth.clamp(momentumSmoothing, 0.05D, 1.0D); }
        public double getMomentumDamping() { return this.momentumDamping; }
        public void setMomentumDamping(double momentumDamping) { this.momentumDamping = Mth.clamp(momentumDamping, 0.1D, 0.99D); }
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
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isToggled() { return this.toggled; }
        public void setToggled(boolean toggled) { this.toggled = toggled; }
        public int getDefaultLevel() { return this.defaultLevel; }
        public void setDefaultLevel(int defaultLevel) { this.defaultLevel = clampToRange(defaultLevel); }
        public int getToggledLevel() { return this.toggledLevel; }
        public void setToggledLevel(int toggledLevel) { this.toggledLevel = clampToRange(toggledLevel); }
        public boolean isUpdateToggleValue() { return this.updateToggleValue; }
        public void setUpdateToggleValue(boolean updateToggleValue) { this.updateToggleValue = updateToggleValue; }
        public int getGammaStep() { return this.gammaStep; }
        public void setGammaStep(int gammaStep) { this.gammaStep = Mth.clamp(gammaStep, 1, 1000); }
        public boolean isShowGammaMessage() { return this.showGammaMessage; }
        public void setShowGammaMessage(boolean showGammaMessage) { this.showGammaMessage = showGammaMessage; }
        public int clampToRange(int value) {
            return Mth.clamp(value, -750, 1500);
        }
    }
}