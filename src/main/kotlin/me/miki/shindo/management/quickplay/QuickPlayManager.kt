package me.miki.shindo.management.quickplay

import me.miki.shindo.management.quickplay.impl.*

class QuickPlayManager {

    private val quickPlays = ArrayList<QuickPlay>().apply {
        add(ArcadeQuickPlay())
        add(BedwarsQuickPlay())
        add(DuelsQuickPlay())
        add(MainLobbyQuickPlay())
        add(MurderMysteryQuickPlay())
        add(SkywarsQuickPlay())
        add(TNTQuickPlay())
        add(UHCQuickPlay())
    }

    fun getQuickPlays(): ArrayList<QuickPlay> = quickPlays
}
