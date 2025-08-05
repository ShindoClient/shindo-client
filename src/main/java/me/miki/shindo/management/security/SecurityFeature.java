package me.miki.shindo.management.security;

import me.miki.shindo.Shindo;

public class SecurityFeature {

    public SecurityFeature() {
        Shindo.getInstance().getEventManager().register(this);
    }
}
