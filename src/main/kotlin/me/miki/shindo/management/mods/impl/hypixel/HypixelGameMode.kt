package me.miki.shindo.management.mods.impl.hypixel

enum class HypixelGameMode(command: String) {
    SKYWARS_SOLO_NORMAL("/play solo_normal"), SKYWARS_SOLO_INSANE("/play solo_insane"),
    SKYWARS_DOUBLES_NORMAL("/play teams_normal"), SKYWARS_DOUBLES_INSANE("/play teams_insane"),
    UHC_DUEL_1V1("/play duels_uhc_duel"), UHC_DUEL_2V2("/play duels_uhc_doubles"),
    UHC_DUEL_4V4("/play duels_uhc_four"), UHC_DUEL_MEETUP("/play duels_uhc_meetup"),
    BEDWARS_4V4("/play bedwars_four_four"), BEDWARS_3V3("/play bedwars_four_three"),
    BEDWARS_DOUBLES("/play bedwars_eight_two"), BEDWARS_SOLO("/play bedwars_eight_one"),
    TNT_RUN("/play tnt_tntrun"), PVP_RUN("/play tnt_pvprun"),
    BOW_SPLEEF("/play tnt_bowspleef"), TNT_TAG("/play tnt_tntag"),
    TNT_WIZARDS("/play tnt_capture");

    val command: String?

    init {
        this.command = command
    }

    companion object {
        fun getModeByCommand(command: String?): HypixelGameMode? {
            for (g in HypixelGameMode.values()) {
                if (g.command == command) {
                    return g
                }
            }

            return null
        }

        fun isBedwars(mode: HypixelGameMode): Boolean {
            return mode == BEDWARS_4V4 || mode == BEDWARS_3V3 || mode == BEDWARS_DOUBLES || mode == BEDWARS_SOLO
        }

        fun isTntGames(mode: HypixelGameMode): Boolean {
            return mode == TNT_RUN || mode == PVP_RUN || mode == BOW_SPLEEF
                    || mode == TNT_TAG || mode == TNT_WIZARDS
        }
    }
}

