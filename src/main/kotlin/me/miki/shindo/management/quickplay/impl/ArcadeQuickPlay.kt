package me.miki.shindo.management.quickplay.impl

import me.miki.shindo.management.quickplay.QuickPlay
import me.miki.shindo.management.quickplay.QuickPlayCommand
import net.minecraft.util.ResourceLocation

class ArcadeQuickPlay : QuickPlay("Arcade", ResourceLocation("shindo/icons/hypixel/Arcade.png")) {
    override fun addCommands() {
        setCommands(
            arrayListOf(
                QuickPlayCommand("Lobby", "/l a"),
                QuickPlayCommand("Hole In The Wall", "/play arcade_hole_in_the_wall"),
                QuickPlayCommand("Football", "/play arcade_soccer"),
                QuickPlayCommand("Bounty Hunters", "/play arcade_bounty_hunters"),
                QuickPlayCommand("Pixel Painters", "/play arcade_pixel_painters"),
                QuickPlayCommand("Dragon Walls", "/play arcade_dragon_wars"),
                QuickPlayCommand("Ender Spleef", "/play arcade_ender_spleef"),
                QuickPlayCommand("Galaxy Wars", "/play arcade_starwars"),
                QuickPlayCommand("Throw Out", "/play arcade_throw_out"),
                QuickPlayCommand("Capture The Wool", "/play arcade_pvp_ctw"),
                QuickPlayCommand("Party Games", "/play arcade_party_games_1"),
                QuickPlayCommand("Farm Hunt", "/play arcade_farm_hunt"),
                QuickPlayCommand("Zombies Dead End", "/play arcade_zombies_dead_end"),
                QuickPlayCommand("Zombies Bad Blood", "/play arcade_zombies_bad_blood"),
                QuickPlayCommand("Zombies Alien Arcadium", "/play arcade_zombies_alien_arcadium"),
                QuickPlayCommand("Hide & Seek Prop Hunt", "/play arcade_hide_and_seek_prop_hunt"),
                QuickPlayCommand("Hide & Seek Party Pooper", "/play arcade_hide_and_seek_party_pooper"),
                QuickPlayCommand("Hypixel Says", "/play arcade_simon_says"),
                QuickPlayCommand("Mini Walls", "/play arcade_mini_walls"),
                QuickPlayCommand("Blocking Dead", "/play arcade_day_one"),
            ),
        )
    }
}
