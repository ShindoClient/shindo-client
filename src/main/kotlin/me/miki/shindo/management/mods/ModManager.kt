package me.miki.shindo.management.mods

import me.miki.shindo.management.mods.impl.*
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.metadata.SettingRegistry
import me.miki.shindo.management.sound.Sound
import me.miki.shindo.management.sound.Sounds
class ModManager {

    private val mods = ArrayList<Mod>()
    private val settings = ArrayList<Setting>()

    fun init() {
        registerMod(AnimationsMod())
        registerMod(AppleSkinMod())
        registerMod(ArmorStatusMod())
        registerMod(ArrayListMod())
        registerMod(AsyncScreenshotMod())
        registerMod(AutoTextMod())
        registerMod(BlockInfoMod())
        registerMod(BlockOverlayMod())
        registerMod(BloodParticlesMod())
        registerMod(BossHealthMod())
        registerMod(BowZoomMod())
        registerMod(BreadcrumbsMod())
        registerMod(CalendarMod())
        registerMod(ChatMod())
        registerMod(ChatTranslateMod())
        registerMod(ChunkBordersMod())
        registerMod(ClearGlassMod())
        registerMod(ClearWaterMod())
        registerMod(ClientSpooferMod())
        registerMod(ClockMod())
        registerMod(ColorSaturationMod())
        registerMod(ComboCounterMod())
        registerMod(CompassMod())
        registerMod(CoordsMod())
        registerMod(CPSDisplayMod())
        registerMod(CrosshairMod())
        registerMod(CustomHeldItemsMod())
        registerMod(DamageParticlesMod())
        registerMod(DamageTiltMod())
        registerMod(DamageTintMod())
        registerMod(DayCounterMod())
        registerMod(DiscordRPCMod())
        registerMod(EntityCullingMod())
        registerMod(FarCameraMod())
        registerMod(FovModifierMod())
        registerMod(FPSBoostMod())
        registerMod(FPSDisplayMod())
        registerMod(FPSLimiterMod())
        registerMod(FPSSpooferMod())
        registerMod(FreelookMod())
        registerMod(FullbrightMod())
        registerMod(GlintColorMod())
        registerMod(InternalSettingsMod())
        registerMod(GodbridgeAssistMod())
        registerMod(HealthDisplayMod())
        registerMod(HitBoxMod())
        registerMod(HitColorMod())
        registerMod(HitDelayFixMod())
        registerMod(HorseStatsMod())
        registerMod(HypixelMod())
        registerMod(HypixelQuickPlayMod())
        registerMod(ImageDisplayMod())
        registerMod(InventoryDisplayMod())
        registerMod(InventoryMod())
        registerMod(ItemInfoMod())
        registerMod(ItemPhysicsMod())
        registerMod(Items2DMod())
        registerMod(JumpCircleMod())
        registerMod(KeystrokesMod())
        registerMod(KillEffectsMod())
        registerMod(KillSoundsMod())
        registerMod(MechvibesMod())
        registerMod(MemoryUsageMod())
        registerMod(MinemenMod())
        registerMod(MinimalDamageShakeMod())
        registerMod(MinimalViewBobbingMod())
        registerMod(MinimapMod())
        registerMod(MoBendsMod())
        registerMod(ModernHotbarMod())
        registerMod(MotionBlurMod())
        registerMod(MouseStrokesMod())
        registerMod(MusicInfoMod())
        registerMod(NameDisplayMod())
        registerMod(NameProtectMod())
        registerMod(NametagMod())
        registerMod(OverlayEditorMod())
        registerMod(PackDisplayMod())
        registerMod(ParticleCustomizerMod())
        registerMod(PingDisplayMod())
        registerMod(PlayerCounterMod())
        registerMod(PlayerDisplayMod())
        registerMod(PlayerListMod())
        registerMod(PlayerPredicatorMod())
        registerMod(PlayTimeDisplayMod())
        registerMod(PotionCounterMod())
        registerMod(PotionStatusMod())
        registerMod(ProjectileTrailMod())
        registerMod(QuickSwitchMod())
        registerMod(RawInputMod())
        registerMod(ReachCirclesMod())
        registerMod(ReachDisplayMod())
        registerMod(RearviewMod())
        registerMod(ScoreboardMod())
        registerMod(ServerIPDisplayMod())
        registerMod(SessionInfoMod())
        registerMod(ShinyPotsMod())
        registerMod(Skin3DMod())
        registerMod(SkinProtectMod())
        registerMod(SlowSwingMod())
        registerMod(SoundModifierMod())
        registerMod(SoundSubtitlesMod())
        registerMod(SpeedometerMod())
        registerMod(StopwatchMod())
        registerMod(TabEditorMod())
        registerMod(TaplookMod())
        registerMod(TargetIndicatorMod())
        registerMod(TargetInfoMod())
        registerMod(TimeChangerMod())
        registerMod(TNTTimerMod())
        registerMod(ToggleSneakMod())
        registerMod(ToggleSprintMod())
        registerMod(UHCOverlayMod())
        registerMod(ViaVersionMod())
        registerMod(WaypointMod())
        registerMod(WeatherChangerMod())
        registerMod(WeatherDisplayMod())
        registerMod(ZoomMod())
    }

    private fun registerMod(mod: Mod) {
        mods.add(mod)
        SettingRegistry.applyMetadata(mod)
        for (setting in SettingRegistry.getSettings(mod)) {
            if (!settings.contains(setting)) {
                settings.add(setting)
            }
        }
    }

    fun getModByTranslateKey(key: String): Mod? {
        for (mod in mods) {
            if (mod.getNameKey() == key) {
                return mod
            }
        }
        return null
    }

    fun getHudMods(): ArrayList<HUDMod> {
        val result = ArrayList<HUDMod>()
        for (mod in mods) {
            if (mod is HUDMod && mod.isDraggable()) {
                result.add(mod)
            }
        }
        return result
    }

    fun getSettingsByMod(mod: Mod): ArrayList<Setting>? {
        val result = ArrayList<Setting>()
        for (setting in settings) {
            if (setting.parent == mod) {
                result.add(setting)
            }
        }
        return if (result.isEmpty()) null else result
    }

    fun getWords(mod: Mod): String {
        val result = StringBuilder()
        for (entry in mods) {
            if (entry == mod) {
                result.append(entry.getName()).append(" ")
            }
        }
        for (setting in settings) {
            if (setting.parent == mod) {
                result.append(setting.name).append(" ")
            }
        }
        for (entry in mods) {
            if (entry == mod && entry.getAlias() != "\u200B") {
                result.append(entry.getAlias()).append(" ")
            }
        }
        return result.toString()
    }

    fun addSettings(vararg settingsList: Setting) {
        settings.addAll(settingsList.asList())
    }

    fun disableAll() {
        for (mod in mods) {
            mod.setToggled(false)
        }
        InternalSettingsMod.instance.setToggled(true)
    }

    fun playToggleSound(toggled: Boolean) {
        if (toggled) {
            Sound.play(Sounds.SHINDO_AUDIO_POSITIVE, true)
        } else {
            Sound.play(Sounds.SHINDO_AUDIO_NEGATIVE, true)
        }
    }

    fun getMods(): ArrayList<Mod> {
        return mods
    }

    fun getSettings(): ArrayList<Setting> {
        return settings
    }
}



