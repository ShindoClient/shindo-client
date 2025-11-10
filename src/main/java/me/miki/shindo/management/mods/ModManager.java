package me.miki.shindo.management.mods;

import lombok.Getter;
import me.miki.shindo.management.mods.impl.*;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.utils.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@Getter
public class ModManager {

    private final ArrayList<Mod> mods = new ArrayList<Mod>();
    private final ArrayList<Setting> settings = new ArrayList<Setting>();

    public void init() {
        registerMod(new AnimationsMod());
        registerMod(new AppleSkinMod());
        registerMod(new ArmorStatusMod());
        registerMod(new ArrayListMod());
        registerMod(new AsyncScreenshotMod());
        registerMod(new AutoTextMod());
        registerMod(new BlockInfoMod());
        registerMod(new BlockOverlayMod());
        registerMod(new BloodParticlesMod());
        registerMod(new BorderlessFullscreenMod());
        registerMod(new BossHealthMod());
        registerMod(new BowZoomMod());
        registerMod(new BreadcrumbsMod());
        registerMod(new CalendarMod());
        registerMod(new ChatMod());
        registerMod(new ChatTranslateMod());
        //registerMod(new ChunkAnimatorMod());
        registerMod(new ChunkBordersMod());
        registerMod(new ClearGlassMod());
        registerMod(new ClearWaterMod());
        registerMod(new ClientSpooferMod());
        registerMod(new ClockMod());
        registerMod(new ColorSaturationMod());
        registerMod(new ComboCounterMod());
        registerMod(new CompassMod());
        registerMod(new CoordsMod());
        registerMod(new CPSDisplayMod());
        registerMod(new CrosshairMod());
        registerMod(new CustomHeldItemsMod());
        registerMod(new DamageParticlesMod());
        registerMod(new DamageTiltMod());
        registerMod(new DamageTintMod());
        registerMod(new DayCounterMod());
        registerMod(new DiscordRPCMod());
        registerMod(new EarsMod());
        registerMod(new EntityCullingMod());
        registerMod(new FarCameraMod());
        registerMod(new FemaleGenderMod());
        registerMod(new FovModifierMod());
        registerMod(new FPSBoostMod());
        registerMod(new FPSDisplayMod());
        registerMod(new FPSLimiterMod());
        registerMod(new FPSSpooferMod());
        registerMod(new FreelookMod());
        registerMod(new FullbrightMod());
        registerMod(new GlintColorMod());
        registerMod(new InternalSettingsMod());
        registerMod(new GodbridgeAssistMod());
        registerMod(new HealthDisplayMod());
        registerMod(new HitBoxMod());
        registerMod(new HitColorMod());
        registerMod(new HitDelayFixMod());
        registerMod(new HorseStatsMod());
        registerMod(new HypixelMod());
        registerMod(new HypixelQuickPlayMod());
        registerMod(new ImageDisplayMod());
        registerMod(new InventoryDisplayMod());
        registerMod(new InventoryMod());
        registerMod(new ItemInfoMod());
        registerMod(new ItemPhysicsMod());
        registerMod(new Items2DMod());
        registerMod(new JumpCircleMod());
        registerMod(new KeystrokesMod());
        registerMod(new KillEffectsMod());
        registerMod(new KillSoundsMod());
        registerMod(new MechvibesMod());
        registerMod(new MemoryUsageMod());
        registerMod(new MinemenMod());
        registerMod(new MinimalDamageShakeMod());
        registerMod(new MinimalViewBobbingMod());
        registerMod(new MinimapMod());
        registerMod(new MoBendsMod());
        registerMod(new ModernHotbarMod());
        registerMod(new MotionBlurMod());
        registerMod(new MouseStrokesMod());
        registerMod(new MusicInfoMod());
        registerMod(new NameDisplayMod());
        registerMod(new NameProtectMod());
        registerMod(new NametagMod());
        registerMod(new OverlayEditorMod());
        registerMod(new PackDisplayMod());
        registerMod(new ParticleCustomizerMod());
        registerMod(new PingDisplayMod());
        registerMod(new PlayerCounterMod());
        registerMod(new PlayerDisplayMod());
        registerMod(new PlayerListMod());
        registerMod(new PlayerPredicatorMod());
        registerMod(new PlayTimeDisplayMod());
        registerMod(new PotionCounterMod());
        registerMod(new PotionStatusMod());
        registerMod(new ProjectileTrailMod());
        registerMod(new QuickSwitchMod());
        registerMod(new RawInputMod());
        registerMod(new ReachCirclesMod());
        registerMod(new ReachDisplayMod());
        registerMod(new RearviewMod());
        registerMod(new ScoreboardMod());
        registerMod(new ServerIPDisplayMod());
        registerMod(new SessionInfoMod());
        registerMod(new ShinyPotsMod());
        registerMod(new Skin3DMod());
        registerMod(new SkinProtectMod());
        registerMod(new SlowSwingMod());
        registerMod(new SoundModifierMod());
        registerMod(new SoundSubtitlesMod());
        registerMod(new SpeedometerMod());
        registerMod(new StopwatchMod());
        registerMod(new TabEditorMod());
        registerMod(new TaplookMod());
        registerMod(new TargetIndicatorMod());
        registerMod(new TargetInfoMod());
        registerMod(new TimeChangerMod());
        registerMod(new TNTTimerMod());
        registerMod(new ToggleSneakMod());
        registerMod(new ToggleSprintMod());
        registerMod(new UHCOverlayMod());
        registerMod(new ViaVersionMod());
        registerMod(new WaveyCapesMod());
        registerMod(new WaypointMod());
        registerMod(new WeatherChangerMod());
        registerMod(new WeatherDisplayMod());
        registerMod(new ZoomMod());
    }

    private void registerMod(Mod mod) {
        mods.add(mod);
        SettingRegistry.applyMetadata(mod);
    }

    public Mod getModByTranslateKey(String key) {

        for (Mod m : mods) {
            if (m.getNameKey().equals(key)) {
                return m;
            }
        }

        return null;
    }

    public ArrayList<HUDMod> getHudMods() {

        ArrayList<HUDMod> result = new ArrayList<HUDMod>();

        for (Mod m : mods) {
            if (m instanceof HUDMod && ((HUDMod) m).isDraggable()) {
                result.add((HUDMod) m);
            }
        }

        return result;
    }

    public ArrayList<Setting> getSettingsByMod(Mod m) {

        ArrayList<Setting> result = new ArrayList<Setting>();

        for (Setting s : settings) {
            if (s.getParent().equals(m)) {
                result.add(s);
            }
        }

        if (result.isEmpty()) {
            return null;
        }

        return result;
    }

    public String getWords(Mod mod) {

        StringBuilder result = new StringBuilder();

        for (Mod m : mods) {
            if (m.equals(mod)) {
                result.append(m.getName()).append(" ");
            }
        }

        for (Setting s : settings) {
            if (s.getParent().equals(mod)) {
                result.append(s.getName()).append(" ");
            }
        }

        for (Mod m : mods) {
            if (m.equals(mod) && !Objects.equals(m.getAlias(), "\u200B")) {
                result.append(m.getAlias()).append(" ");
            }
        }

        return result.toString();
    }

    public void addSettings(Setting... settingsList) {
        settings.addAll(Arrays.asList(settingsList));
    }

    public void disableAll() {
        for (Mod m : mods) {
            m.setToggled(false);
        }
        InternalSettingsMod.getInstance().setToggled(true);
    }

    public void playToggleSound(boolean toggled) {
        if (toggled) {
            Sound.play("shindo/audio/positive.wav", true);
        } else {
            Sound.play("shindo/audio/negative.wav", true);
        }

    }

}

