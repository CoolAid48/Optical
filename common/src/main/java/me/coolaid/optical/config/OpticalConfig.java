package me.coolaid.optical.config;

import net.minecraft.util.Mth;

public final class OpticalConfig {
    public static final FreelookConfig FREELOOK = new FreelookConfig();
    public static final BrightnessConfig BRIGHTNESS = new BrightnessConfig();

    private OpticalConfig() {
    }

    public static final class FreelookConfig {
        private boolean enabled = true;
        private boolean toggleMode = false;
        private boolean invertY = false;
        private double sensitivityMultiplier = 1.0D;
        private Style style = Style.CLASSIC;
        private int rotationLimit = 360;

        public enum Style {
            CLASSIC,
            BETTER
        }

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isToggleMode() { return this.toggleMode; }
        public void setToggleMode(boolean toggleMode) { this.toggleMode = toggleMode; }
        public boolean isInvertY() { return this.invertY; }
        public void setInvertY(boolean invertY) { this.invertY = invertY; }
        public double getSensitivityMultiplier() { return this.sensitivityMultiplier; }
        public void setSensitivityMultiplier(double sensitivityMultiplier) { this.sensitivityMultiplier = sensitivityMultiplier; }
        public Style getStyle() { return this.style; }
        public void setStyle(Style style) { this.style = style; }
        public boolean isBetterStyle() { return this.style == Style.BETTER; }
        public int getRotationLimit() { return this.rotationLimit; }
        public void setRotationLimit(int rotationLimit) { this.rotationLimit = rotationLimit; }
    }

    public static final class BrightnessConfig {
        private boolean enabled = true;
        private boolean toggled = false;
        private int defaultLevel = 100;
        private int toggledLevel = 1500;

        public boolean isEnabled() { return this.enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isToggled() { return this.toggled; }
        public void setToggled(boolean toggled) { this.toggled = toggled; }
        public int getDefaultLevel() { return this.defaultLevel; }
        public void setDefaultLevel(int defaultLevel) { this.defaultLevel = Mth.clamp(defaultLevel, -750, 1500); }
        public int getToggledLevel() { return this.toggledLevel; }
        public void setToggledLevel(int toggledLevel) { this.toggledLevel = Mth.clamp(toggledLevel, -750, 1500); }
    }
}