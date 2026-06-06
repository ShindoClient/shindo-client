package com.shindoclient.shindo.management.security

import com.shindoclient.shindo.management.security.impl.DemoSecurity
import com.shindoclient.shindo.management.security.impl.ExplosionSecurity
import com.shindoclient.shindo.management.security.impl.Log4jSecurity
import com.shindoclient.shindo.management.security.impl.ParticleSecurity
import com.shindoclient.shindo.management.security.impl.ResourcePackSecurity
import com.shindoclient.shindo.management.security.impl.TeleportSecurity

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
