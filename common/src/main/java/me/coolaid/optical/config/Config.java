package me.coolaid.optical.config;

public final class Config {
    public static final FreelookConfig FREELOOK = new FreelookConfig();

    private Config() {
    }

    public static final class FreelookConfig {
        private boolean enabled = true;
        private boolean toggleMode = false;
        private boolean invertY = false;
        private double sensitivityMultiplier = 1.0D;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isToggleMode() {
            return toggleMode;
        }

        public void setToggleMode(boolean toggleMode) {
            this.toggleMode = toggleMode;
        }

        public boolean isInvertY() {
            return invertY;
        }

        public void setInvertY(boolean invertY) {
            this.invertY = invertY;
        }

        public double getSensitivityMultiplier() {
            return sensitivityMultiplier;
        }

        public void setSensitivityMultiplier(double sensitivityMultiplier) {
            this.sensitivityMultiplier = sensitivityMultiplier;
        }
    }
}
