package me.coolaid.optical;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Optical {
    public static final String MOD_ID = "optical";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        // Write common init code here.

        LOGGER.info("Initializing Optical's Camera Tweaks...");
    }
}
