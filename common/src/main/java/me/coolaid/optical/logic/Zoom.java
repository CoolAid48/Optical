package me.coolaid.optical.logic;

import me.coolaid.optical.OpticalBindings;
import me.coolaid.optical.config.OpticalConfig;
import net.minecraft.client.Minecraft;

public final class Zoom {
    private static final double EPSILON = 0.0001D;

    private static double currentZoomStrength = 1.0D;
    private static boolean secondaryZoomActive;
    private static int primaryScrollSteps;
    private static int secondaryScrollSteps;
    private static boolean wasPrimaryZoomActive;
    private static boolean secondaryRecentlyDeactivated;
    private static long secondaryActivatedAtNanos = System.nanoTime();
    private static double transitionStartStrength = 1.0D;
    private static double transitionTargetStrength = 1.0D;
    private static double transitionDurationSeconds = 0.0D;
    private static long transitionStartNanos = System.nanoTime();

    private Zoom() {
    }

    public static void onClientTick(Minecraft minecraft) {
        if (minecraft == null) {
            return;
        }

        if (!OpticalConfig.ZOOM.isEnabled()) {
            resetState();
            return;
        }

        while (OpticalBindings.SECONDARY_ZOOM.consumeClick()) {
            secondaryZoomActive = !secondaryZoomActive;
            if (secondaryZoomActive) {
                secondaryActivatedAtNanos = System.nanoTime();
                secondaryRecentlyDeactivated = false;
            } else {
                secondaryRecentlyDeactivated = true;
            }
        }

        boolean primaryActive = isPrimaryZoomActive();
        if (!primaryActive && wasPrimaryZoomActive && !OpticalConfig.ZOOM.isRememberZoomSteps()) {
            primaryScrollSteps = 0;
        }

        if (!secondaryZoomActive) {
            if (!OpticalConfig.ZOOM.isRememberZoomSteps()) {
                secondaryScrollSteps = 0;
            }
        }

        wasPrimaryZoomActive = primaryActive;
    }

    public static boolean handleScroll(double yOffset) {
        if (!OpticalConfig.ZOOM.isEnabled() || !OpticalConfig.ZOOM.isScrollAdjustEnabled() || !isAnyZoomActive()) {
            return false;
        }

        int stepDelta = yOffset > 0.0D ? 1 : yOffset < 0.0D ? -1 : 0;
        if (stepDelta == 0) {
            return false;
        }

        if (isSecondaryZoomActive()) {
            secondaryScrollSteps = clampStepCount(secondaryScrollSteps + stepDelta);
        } else if (isPrimaryZoomActive()) {
            primaryScrollSteps = clampStepCount(primaryScrollSteps + stepDelta);
        }

        return true;
    }

    public static double applyZoomFov(double originalFov) {
        if (!OpticalConfig.ZOOM.isEnabled()) {
            return originalFov;
        }

        double targetStrength = getTargetStrength();
        updateSmoothedStrength(targetStrength);
        return originalFov / currentZoomStrength;
    }

    public static boolean shouldHideHud() {
        return OpticalConfig.ZOOM.isEnabled() && isSecondaryZoomActive();
    }

    private static boolean isAnyZoomActive() {
        return isPrimaryZoomActive() || isSecondaryZoomActive();
    }

    private static boolean isPrimaryZoomActive() {
        return OpticalBindings.ZOOM.isDown();
    }

    private static boolean isSecondaryZoomActive() {
        return secondaryZoomActive;
    }

    private static void updateSmoothedStrength(double targetStrength) {
        long now = System.nanoTime();
        currentZoomStrength = getTransitionValue(now);

        boolean zoomingIn = targetStrength > currentZoomStrength;
        if ((zoomingIn && OpticalConfig.ZOOM.getZoomInTransition() == OpticalConfig.ZoomConfig.TransitionMode.INSTANT)
                || (!zoomingIn && OpticalConfig.ZOOM.getZoomOutTransition() == OpticalConfig.ZoomConfig.TransitionMode.INSTANT)) {
            currentZoomStrength = targetStrength;
            transitionStartStrength = targetStrength;
            transitionTargetStrength = targetStrength;
            transitionDurationSeconds = 0.0D;
            transitionStartNanos = now;
            if (targetStrength <= 1.0001D) {
                secondaryRecentlyDeactivated = false;
            }
            return;
        }

        double baseDuration;
        if (zoomingIn) {
            baseDuration = isSecondaryZoomActive()
                    ? OpticalConfig.ZOOM.getSecondaryZoomInSeconds()
                    : OpticalConfig.ZOOM.getZoomInSeconds();
        } else {
            baseDuration = (!isSecondaryZoomActive() && secondaryRecentlyDeactivated)
                    ? OpticalConfig.ZOOM.getSecondaryZoomOutSeconds()
                    : OpticalConfig.ZOOM.getZoomOutSeconds();
        }
        if (Math.abs(targetStrength - transitionTargetStrength) > EPSILON) {
            transitionStartStrength = currentZoomStrength;
            transitionTargetStrength = targetStrength;
            transitionDurationSeconds = Math.max(0.001D, baseDuration);
            transitionStartNanos = now;
        }

        currentZoomStrength = OpticalConfig.ZOOM.clampStrength(getTransitionValue(now));
        if (targetStrength <= 1.0001D && currentZoomStrength <= 1.0001D) {
            secondaryRecentlyDeactivated = false;
        }
    }

    private static double getTargetStrength() {
        if (isSecondaryZoomActive()) {
            double easedProgress = easeInOutCubic(getSecondaryInProgress());
            double finalStrength = getSteppedStrength(OpticalConfig.ZOOM.getSecondaryZoomStrength(), secondaryScrollSteps);
            return lerp(1.0D, finalStrength, easedProgress);
        }

        if (isPrimaryZoomActive()) {
            return getSteppedStrength(OpticalConfig.ZOOM.getDefaultZoomStrength(), primaryScrollSteps);
        }

        return 1.0D;
    }

    private static double getSteppedStrength(double baseStrength, int steps) {
        return OpticalConfig.ZOOM.clampStrength(baseStrength * Math.pow(OpticalConfig.ZOOM.getZoomPerStep(), steps));
    }

    private static int clampStepCount(int value) {
        int maxSteps = OpticalConfig.ZOOM.getScrollStepCount();
        return Math.max(-maxSteps, Math.min(value, maxSteps));
    }

    private static double easeInOutCubic(double value) {
        if (value < 0.5D) {
            return 4.0D * value * value * value;
        }
        double adjusted = -2.0D * value + 2.0D;
        return 1.0D - (adjusted * adjusted * adjusted) / 2.0D;
    }

    private static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    private static double getTransitionValue(long now) {
        if (transitionDurationSeconds <= EPSILON || Math.abs(transitionTargetStrength - transitionStartStrength) <= EPSILON) {
            return transitionTargetStrength;
        }

        double elapsed = Math.max(0.0D, (now - transitionStartNanos) / 1_000_000_000.0D);
        double progress = Math.min(1.0D, elapsed / transitionDurationSeconds);
        double smoothness = OpticalConfig.ZOOM.getScrollZoomSmoothness() / 100.0D;
        double easedProgress = lerp(progress, easeInOutCubic(progress), smoothness);
        return lerp(transitionStartStrength, transitionTargetStrength, easedProgress);
    }

    private static double getSecondaryInProgress() {
        double duration = OpticalConfig.ZOOM.getSecondaryZoomInSeconds();
        if (duration <= 0.0D) {
            return 1.0D;
        }

        long now = System.nanoTime();
        double elapsed = Math.max(0.0D, (now - secondaryActivatedAtNanos) / 1_000_000_000.0D);
        return Math.min(1.0D, elapsed / duration);
    }

    private static void resetState() {
        currentZoomStrength = 1.0D;
        secondaryZoomActive = false;
        primaryScrollSteps = 0;
        secondaryScrollSteps = 0;
        wasPrimaryZoomActive = false;
        secondaryRecentlyDeactivated = false;
        secondaryActivatedAtNanos = System.nanoTime();
        transitionStartStrength = 1.0D;
        transitionTargetStrength = 1.0D;
        transitionDurationSeconds = 0.0D;
        transitionStartNanos = System.nanoTime();
    }
}