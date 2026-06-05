package me.miki.shindo.management.mods

import me.miki.shindo.management.mods.impl.AnimationsMod
import me.miki.shindo.management.mods.impl.AppleSkinMod
import me.miki.shindo.management.mods.impl.ArmorStatusMod
import me.miki.shindo.management.mods.impl.ArrayListMod
import me.miki.shindo.management.mods.impl.AsyncScreenshotMod
import me.miki.shindo.management.mods.impl.AutoTextMod
import me.miki.shindo.management.mods.impl.BlockInfoMod
import me.miki.shindo.management.mods.impl.BlockOverlayMod
import me.miki.shindo.management.mods.impl.BloodParticlesMod
import me.miki.shindo.management.mods.impl.BossHealthMod
import me.miki.shindo.management.mods.impl.BowZoomMod
import me.miki.shindo.management.mods.impl.BreadcrumbsMod
import me.miki.shindo.management.mods.impl.CPSDisplayMod
import me.miki.shindo.management.mods.impl.CalendarMod
import me.miki.shindo.management.mods.impl.ChatMod
import me.miki.shindo.management.mods.impl.ChatTranslateMod
import me.miki.shindo.management.mods.impl.ChunkBordersMod
import me.miki.shindo.management.mods.impl.ClearGlassMod
import me.miki.shindo.management.mods.impl.ClearWaterMod
import me.miki.shindo.management.mods.impl.ClientSpooferMod
import me.miki.shindo.management.mods.impl.ClockMod
import me.miki.shindo.management.mods.impl.ColorSaturationMod
import me.miki.shindo.management.mods.impl.ComboCounterMod
import me.miki.shindo.management.mods.impl.CompassMod
import me.miki.shindo.management.mods.impl.CoordsMod
import me.miki.shindo.management.mods.impl.CrosshairMod
import me.miki.shindo.management.mods.impl.CustomHeldItemsMod
import me.miki.shindo.management.mods.impl.DamageParticlesMod
import me.miki.shindo.management.mods.impl.DamageTiltMod
import me.miki.shindo.management.mods.impl.DamageTintMod
import me.miki.shindo.management.mods.impl.DayCounterMod
import me.miki.shindo.management.mods.impl.DiscordRPCMod
import me.miki.shindo.management.mods.impl.EntityCullingMod
import me.miki.shindo.management.mods.impl.FPSBoostMod
import me.miki.shindo.management.mods.impl.FPSDisplayMod
import me.miki.shindo.management.mods.impl.FPSLimiterMod
import me.miki.shindo.management.mods.impl.FPSSpooferMod
import me.miki.shindo.management.mods.impl.FarCameraMod
import me.miki.shindo.management.mods.impl.FovModifierMod
import me.miki.shindo.management.mods.impl.FreelookMod
import me.miki.shindo.management.mods.impl.FullbrightMod
import me.miki.shindo.management.mods.impl.GlintColorMod
import me.miki.shindo.management.mods.impl.GodbridgeAssistMod
import me.miki.shindo.management.mods.impl.HealthDisplayMod
import me.miki.shindo.management.mods.impl.HitBoxMod
import me.miki.shindo.management.mods.impl.HitColorMod
import me.miki.shindo.management.mods.impl.HitDelayFixMod
import me.miki.shindo.management.mods.impl.HorseStatsMod
import me.miki.shindo.management.mods.impl.HypixelMod
import me.miki.shindo.management.mods.impl.HypixelQuickPlayMod
import me.miki.shindo.management.mods.impl.ImageDisplayMod
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.mods.impl.InventoryDisplayMod
import me.miki.shindo.management.mods.impl.InventoryMod
import me.miki.shindo.management.mods.impl.ItemInfoMod
import me.miki.shindo.management.mods.impl.ItemPhysicsMod
import me.miki.shindo.management.mods.impl.Items2DMod
import me.miki.shindo.management.mods.impl.JumpCircleMod
import me.miki.shindo.management.mods.impl.KeystrokesMod
import me.miki.shindo.management.mods.impl.KillEffectsMod
import me.miki.shindo.management.mods.impl.KillSoundsMod
import me.miki.shindo.management.mods.impl.MechvibesMod
import me.miki.shindo.management.mods.impl.MemoryUsageMod
import me.miki.shindo.management.mods.impl.MinemenMod
import me.miki.shindo.management.mods.impl.MinimalDamageShakeMod
import me.miki.shindo.management.mods.impl.MinimalViewBobbingMod
import me.miki.shindo.management.mods.impl.MinimapMod
import me.miki.shindo.management.mods.impl.MoBendsMod
import me.miki.shindo.management.mods.impl.ModernHotbarMod
import me.miki.shindo.management.mods.impl.MotionBlurMod
import me.miki.shindo.management.mods.impl.MouseStrokesMod
import me.miki.shindo.management.mods.impl.MusicInfoMod
import me.miki.shindo.management.mods.impl.NameDisplayMod
import me.miki.shindo.management.mods.impl.NameProtectMod
import me.miki.shindo.management.mods.impl.NametagMod
import me.miki.shindo.management.mods.impl.OverlayEditorMod
import me.miki.shindo.management.mods.impl.PackDisplayMod
import me.miki.shindo.management.mods.impl.ParticleCustomizerMod
import me.miki.shindo.management.mods.impl.PingDisplayMod
import me.miki.shindo.management.mods.impl.PlayTimeDisplayMod
import me.miki.shindo.management.mods.impl.PlayerCounterMod
import me.miki.shindo.management.mods.impl.PlayerDisplayMod
import me.miki.shindo.management.mods.impl.PlayerListMod
import me.miki.shindo.management.mods.impl.PlayerPredicatorMod
import me.miki.shindo.management.mods.impl.PotionCounterMod
import me.miki.shindo.management.mods.impl.PotionStatusMod
import me.miki.shindo.management.mods.impl.ProjectileTrailMod
import me.miki.shindo.management.mods.impl.QuickSwitchMod
import me.miki.shindo.management.mods.impl.RawInputMod
import me.miki.shindo.management.mods.impl.ReachCirclesMod
import me.miki.shindo.management.mods.impl.ReachDisplayMod
import me.miki.shindo.management.mods.impl.RearviewMod
import me.miki.shindo.management.mods.impl.ScoreboardMod
import me.miki.shindo.management.mods.impl.ServerIPDisplayMod
import me.miki.shindo.management.mods.impl.SessionInfoMod
import me.miki.shindo.management.mods.impl.ShinyPotsMod
import me.miki.shindo.management.mods.impl.Skin3DMod
import me.miki.shindo.management.mods.impl.SkinProtectMod
import me.miki.shindo.management.mods.impl.SlowSwingMod
import me.miki.shindo.management.mods.impl.SoundModifierMod
import me.miki.shindo.management.mods.impl.SoundSubtitlesMod
import me.miki.shindo.management.mods.impl.SpeedometerMod
import me.miki.shindo.management.mods.impl.StopwatchMod
import me.miki.shindo.management.mods.impl.SuperHeroFxMod
import me.miki.shindo.management.mods.impl.TNTTimerMod
import me.miki.shindo.management.mods.impl.TabEditorMod
import me.miki.shindo.management.mods.impl.TaplookMod
import me.miki.shindo.management.mods.impl.TargetIndicatorMod
import me.miki.shindo.management.mods.impl.TargetInfoMod
import me.miki.shindo.management.mods.impl.TimeChangerMod
import me.miki.shindo.management.mods.impl.ToggleSneakMod
import me.miki.shindo.management.mods.impl.ToggleSprintMod
import me.miki.shindo.management.mods.impl.UHCOverlayMod
import me.miki.shindo.management.mods.impl.ViaVersionMod
import me.miki.shindo.management.mods.impl.WaypointMod
import me.miki.shindo.management.mods.impl.WeatherChangerMod
import me.miki.shindo.management.mods.impl.WeatherDisplayMod
import me.miki.shindo.management.mods.impl.ZoomMod
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.metadata.SettingRegistry
import me.miki.shindo.management.sound.Sound
import me.miki.shindo.management.sound.Sounds

@Suppress("UNUSED")
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
        registerMod(SuperHeroFxMod())
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

    /**
     * Register an externally-created HUDMod (e.g. an [AddonHUDMod])
     * so it appears in the HUD editor.
     *
     * Unlike [registerMod], this does NOT process SettingRegistry metadata
     * because addon HUDs have no settings to register.
     */
    fun registerHudMod(hudMod: HUDMod) {
        mods.add(hudMod)
    }

    /**
     * Remove a previously-registered external HUDMod.
     */
    fun unregisterHudMod(hudMod: HUDMod) {
        mods.remove(hudMod)
    }

    fun playToggleSound(toggled: Boolean) {
        if (toggled) {
            Sound.play(Sounds.SHINDO_AUDIO_POSITIVE, true)
        } else {
            Sound.play(Sounds.SHINDO_AUDIO_NEGATIVE, true)
        }
    }

    fun getMods(): ArrayList<Mod> = mods

    fun getSettings(): ArrayList<Setting> = settings
}
