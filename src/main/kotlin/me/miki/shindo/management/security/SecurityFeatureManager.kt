package me.miki.shindo.management.security

import me.miki.shindo.management.security.impl.DemoSecurity
import me.miki.shindo.management.security.impl.ExplosionSecurity
import me.miki.shindo.management.security.impl.Log4jSecurity
import me.miki.shindo.management.security.impl.ParticleSecurity
import me.miki.shindo.management.security.impl.ResourcePackSecurity
import me.miki.shindo.management.security.impl.TeleportSecurity

class SecurityFeatureManager {
    private val features = ArrayList<SecurityFeature>()

    init {
        features.add(DemoSecurity())
        features.add(ExplosionSecurity())
        features.add(Log4jSecurity())
        features.add(ParticleSecurity())
        features.add(ResourcePackSecurity())
        features.add(TeleportSecurity())
    }

    fun getFeatures(): ArrayList<SecurityFeature> = features
}
