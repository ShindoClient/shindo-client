package me.miki.shindo.management.security

import me.miki.shindo.Shindo

open class SecurityFeature {

    init {
        Shindo.getInstance().getEventManager().register(this)
    }
}
