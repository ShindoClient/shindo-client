package com.shindoclient.shindo.management.quickplay

import com.shindoclient.shindo.management.quickplay.impl.ArcadeQuickPlay
import com.shindoclient.shindo.management.quickplay.impl.BedwarsQuickPlay
import com.shindoclient.shindo.management.quickplay.impl.DuelsQuickPlay
import com.shindoclient.shindo.management.quickplay.impl.MainLobbyQuickPlay
import com.shindoclient.shindo.management.quickplay.impl.MurderMysteryQuickPlay
import com.shindoclient.shindo.management.quickplay.impl.SkywarsQuickPlay
import com.shindoclient.shindo.management.quickplay.impl.TNTQuickPlay
import com.shindoclient.shindo.management.quickplay.impl.UHCQuickPlay

class QuickPlayManager {
    private val quickPlays =
        ArrayList<QuickPlay>().apply {
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
