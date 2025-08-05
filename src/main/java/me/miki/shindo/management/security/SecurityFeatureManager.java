package me.miki.shindo.management.security;

import me.miki.shindo.management.security.impl.*;

import java.util.ArrayList;

public class SecurityFeatureManager {

    private final ArrayList<SecurityFeature> features = new ArrayList<SecurityFeature>();

    public SecurityFeatureManager() {
        features.add(new DemoSecurity());
        features.add(new ExplosionSecurity());
        features.add(new Log4jSecurity());
        features.add(new ParticleSecurity());
        features.add(new ResourcePackSecurity());
        features.add(new TeleportSecurity());
    }

    public ArrayList<SecurityFeature> getFeatures() {
        return features;
    }
}
