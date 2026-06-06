package com.shindoclient.shindo.management.mods

import com.shindoclient.shindo.management.mods.impl.AnimationsMod
import com.shindoclient.shindo.management.mods.impl.AppleSkinMod
import com.shindoclient.shindo.management.mods.impl.ArmorStatusMod
import com.shindoclient.shindo.management.mods.impl.ArrayListMod
import com.shindoclient.shindo.management.mods.impl.AsyncScreenshotMod
import com.shindoclient.shindo.management.mods.impl.AutoTextMod
import com.shindoclient.shindo.management.mods.impl.BlockInfoMod
import com.shindoclient.shindo.management.mods.impl.BlockOverlayMod
import com.shindoclient.shindo.management.mods.impl.BloodParticlesMod
import com.shindoclient.shindo.management.mods.impl.BossHealthMod
import com.shindoclient.shindo.management.mods.impl.BowZoomMod
import com.shindoclient.shindo.management.mods.impl.BreadcrumbsMod
import com.shindoclient.shindo.management.mods.impl.CPSDisplayMod
import com.shindoclient.shindo.management.mods.impl.CalendarMod
import com.shindoclient.shindo.management.mods.impl.ChatMod
import com.shindoclient.shindo.management.mods.impl.ChatTranslateMod
import com.shindoclient.shindo.management.mods.impl.ChunkBordersMod
import com.shindoclient.shindo.management.mods.impl.ClearGlassMod
import com.shindoclient.shindo.management.mods.impl.ClearWaterMod
import com.shindoclient.shindo.management.mods.impl.ClientSpooferMod
import com.shindoclient.shindo.management.mods.impl.ClockMod
import com.shindoclient.shindo.management.mods.impl.ColorSaturationMod
import com.shindoclient.shindo.management.mods.impl.ComboCounterMod
import com.shindoclient.shindo.management.mods.impl.CompassMod
import com.shindoclient.shindo.management.mods.impl.CoordsMod
import com.shindoclient.shindo.management.mods.impl.CrosshairMod
import com.shindoclient.shindo.management.mods.impl.CustomHeldItemsMod
import com.shindoclient.shindo.management.mods.impl.DamageParticlesMod
import com.shindoclient.shindo.management.mods.impl.DamageTiltMod
import com.shindoclient.shindo.management.mods.impl.DamageTintMod
import com.shindoclient.shindo.management.mods.impl.DayCounterMod
import com.shindoclient.shindo.management.mods.impl.DiscordRPCMod
import com.shindoclient.shindo.management.mods.impl.EntityCullingMod
import com.shindoclient.shindo.management.mods.impl.FPSBoostMod
import com.shindoclient.shindo.management.mods.impl.FPSDisplayMod
import com.shindoclient.shindo.management.mods.impl.FPSLimiterMod
import com.shindoclient.shindo.management.mods.impl.FPSSpooferMod
import com.shindoclient.shindo.management.mods.impl.FarCameraMod
import com.shindoclient.shindo.management.mods.impl.FovModifierMod
import com.shindoclient.shindo.management.mods.impl.FreelookMod
import com.shindoclient.shindo.management.mods.impl.FullbrightMod
import com.shindoclient.shindo.management.mods.impl.GlintColorMod
import com.shindoclient.shindo.management.mods.impl.GodbridgeAssistMod
import com.shindoclient.shindo.management.mods.impl.HealthDisplayMod
import com.shindoclient.shindo.management.mods.impl.HitBoxMod
import com.shindoclient.shindo.management.mods.impl.HitColorMod
import com.shindoclient.shindo.management.mods.impl.HitDelayFixMod
import com.shindoclient.shindo.management.mods.impl.HorseStatsMod
import com.shindoclient.shindo.management.mods.impl.HypixelMod
import com.shindoclient.shindo.management.mods.impl.HypixelQuickPlayMod
import com.shindoclient.shindo.management.mods.impl.ImageDisplayMod
import com.shindoclient.shindo.management.mods.impl.InternalSettingsMod
import com.shindoclient.shindo.management.mods.impl.InventoryDisplayMod
import com.shindoclient.shindo.management.mods.impl.InventoryMod
import com.shindoclient.shindo.management.mods.impl.ItemInfoMod
import com.shindoclient.shindo.management.mods.impl.ItemPhysicsMod
import com.shindoclient.shindo.management.mods.impl.Items2DMod
import com.shindoclient.shindo.management.mods.impl.JumpCircleMod
import com.shindoclient.shindo.management.mods.impl.KeystrokesMod
import com.shindoclient.shindo.management.mods.impl.KillEffectsMod
import com.shindoclient.shindo.management.mods.impl.KillSoundsMod
import com.shindoclient.shindo.management.mods.impl.MechvibesMod
import com.shindoclient.shindo.management.mods.impl.MemoryUsageMod
import com.shindoclient.shindo.management.mods.impl.MinemenMod
import com.shindoclient.shindo.management.mods.impl.MinimalDamageShakeMod
import com.shindoclient.shindo.management.mods.impl.MinimalViewBobbingMod
import com.shindoclient.shindo.management.mods.impl.MinimapMod
import com.shindoclient.shindo.management.mods.impl.MoBendsMod
import com.shindoclient.shindo.management.mods.impl.ModernHotbarMod
import com.shindoclient.shindo.management.mods.impl.MotionBlurMod
import com.shindoclient.shindo.management.mods.impl.MouseStrokesMod
import com.shindoclient.shindo.management.mods.impl.MusicInfoMod
import com.shindoclient.shindo.management.mods.impl.NameDisplayMod
import com.shindoclient.shindo.management.mods.impl.NameProtectMod
import com.shindoclient.shindo.management.mods.impl.NametagMod
import com.shindoclient.shindo.management.mods.impl.OverlayEditorMod
import com.shindoclient.shindo.management.mods.impl.PackDisplayMod
import com.shindoclient.shindo.management.mods.impl.ParticleCustomizerMod
import com.shindoclient.shindo.management.mods.impl.PingDisplayMod
import com.shindoclient.shindo.management.mods.impl.PlayTimeDisplayMod
import com.shindoclient.shindo.management.mods.impl.PlayerCounterMod
import com.shindoclient.shindo.management.mods.impl.PlayerDisplayMod
import com.shindoclient.shindo.management.mods.impl.PlayerListMod
import com.shindoclient.shindo.management.mods.impl.PlayerPredicatorMod
import com.shindoclient.shindo.management.mods.impl.PotionCounterMod
import com.shindoclient.shindo.management.mods.impl.PotionStatusMod
import com.shindoclient.shindo.management.mods.impl.ProjectileTrailMod
import com.shindoclient.shindo.management.mods.impl.QuickSwitchMod
import com.shindoclient.shindo.management.mods.impl.RawInputMod
import com.shindoclient.shindo.management.mods.impl.ReachCirclesMod
import com.shindoclient.shindo.management.mods.impl.ReachDisplayMod
import com.shindoclient.shindo.management.mods.impl.RearviewMod
import com.shindoclient.shindo.management.mods.impl.ScoreboardMod
import com.shindoclient.shindo.management.mods.impl.ServerIPDisplayMod
import com.shindoclient.shindo.management.mods.impl.SessionInfoMod
import com.shindoclient.shindo.management.mods.impl.ShinyPotsMod
import com.shindoclient.shindo.management.mods.impl.Skin3DMod
import com.shindoclient.shindo.management.mods.impl.SkinProtectMod
import com.shindoclient.shindo.management.mods.impl.SlowSwingMod
import com.shindoclient.shindo.management.mods.impl.SoundModifierMod
import com.shindoclient.shindo.management.mods.impl.SoundSubtitlesMod
import com.shindoclient.shindo.management.mods.impl.SpeedometerMod
import com.shindoclient.shindo.management.mods.impl.StopwatchMod
import com.shindoclient.shindo.management.mods.impl.SuperHeroFxMod
import com.shindoclient.shindo.management.mods.impl.TNTTimerMod
import com.shindoclient.shindo.management.mods.impl.TabEditorMod
import com.shindoclient.shindo.management.mods.impl.TaplookMod
import com.shindoclient.shindo.management.mods.impl.TargetIndicatorMod
import com.shindoclient.shindo.management.mods.impl.TargetInfoMod
import com.shindoclient.shindo.management.mods.impl.TimeChangerMod
import com.shindoclient.shindo.management.mods.impl.ToggleSneakMod
import com.shindoclient.shindo.management.mods.impl.ToggleSprintMod
import com.shindoclient.shindo.management.mods.impl.UHCOverlayMod
import com.shindoclient.shindo.management.mods.impl.ViaVersionMod
import com.shindoclient.shindo.management.mods.impl.WaypointMod
import com.shindoclient.shindo.management.mods.impl.WeatherChangerMod
import com.shindoclient.shindo.management.mods.impl.WeatherDisplayMod
import com.shindoclient.shindo.management.mods.impl.ZoomMod
import com.shindoclient.shindo.management.settings.Setting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry
import com.shindoclient.shindo.management.sound.Sound
import com.shindoclient.shindo.management.sound.Sounds

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
