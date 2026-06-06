package com.shindoclient.shindo.management.security

import com.shindoclient.shindo.Shindo

open class SecurityFeature {
    init {
        Shindo.getInstance().getEventManager().register(this)
    }
}
