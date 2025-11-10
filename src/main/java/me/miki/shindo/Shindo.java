package me.miki.shindo;

import eu.shoroa.contrib.cosmetic.CosmeticManager;
import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.injection.mixin.ShindoTweaker;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.account.AccountManager;
import me.miki.shindo.management.addons.AddonManager;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.command.CommandManager;
import me.miki.shindo.management.cosmetic.bandanna.BandannaManager;
import me.miki.shindo.management.cosmetic.cape.CapeManager;
import me.miki.shindo.management.cosmetic.wing.WingManager;
import me.miki.shindo.management.event.EventManager;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.LanguageManager;
import me.miki.shindo.management.mods.ModManager;
import me.miki.shindo.management.mods.RestrictedMod;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.music.MusicManager;
import me.miki.shindo.management.music.RomanizationManager;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.notification.NotificationManager;
import me.miki.shindo.management.profile.ProfileManager;
import me.miki.shindo.management.quickplay.QuickPlayManager;
import me.miki.shindo.management.remote.blacklists.BlacklistManager;
import me.miki.shindo.management.remote.changelog.ChangelogManager;
import me.miki.shindo.management.remote.discord.DiscordStats;
import me.miki.shindo.management.remote.download.DownloadManager;
import me.miki.shindo.management.remote.news.NewsManager;
import me.miki.shindo.management.remote.update.Update;
import me.miki.shindo.management.screenshot.ScreenshotManager;
import me.miki.shindo.management.security.SecurityFeatureManager;
import me.miki.shindo.management.shader.ShaderManager;
import me.miki.shindo.management.skin.SkinManager;
import me.miki.shindo.management.tweaker.ConnectionTweakerManager;
import me.miki.shindo.management.tweaker.proxy.WarpProxyManager;
import me.miki.shindo.management.waypoint.WaypointManager;
import me.miki.shindo.ui.ClickEffects;
import me.miki.shindo.utils.OptifineUtils;
import me.miki.shindo.utils.Sound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class Shindo {

    @Getter
    private static final Shindo instance = new Shindo();
    private final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private final String name;

    @Getter
    private final String version;

    @Getter
    private final String author;

    @Getter
    private final int verIdentifier;

    @Setter
    private boolean updateNeeded;

    @Setter
    @Getter
    private NanoVGManager nanoVGManager;

    @Getter
    private FileManager fileManager;

    @Getter
    private LanguageManager languageManager;

    @Getter
    private AccountManager accountManager;

    @Getter
    private EventManager eventManager;

    @Getter
    private DownloadManager downloadManager;

    @Getter
    private ModManager modManager;

    @Getter
    private AddonManager addonManager;

    //@Getter
    //private CosmeticManager cosmeticManager;

    @Getter
    private CapeManager capeManager;

    @Getter
    private WingManager wingManager;

    @Getter
    private BandannaManager bandannaManager;

    @Getter
    private ColorManager colorManager;

    @Getter
    private ProfileManager profileManager;

    @Getter
    private CommandManager commandManager;

    @Getter
    private ScreenshotManager screenshotManager;

    @Getter
    private NotificationManager notificationManager;

    @Getter
    private SecurityFeatureManager securityFeatureManager;

    @Getter
    private MusicManager musicManager;

    @Getter
    private QuickPlayManager quickPlayManager;

    @Getter
    private ChangelogManager changelogManager;

    @Getter
    private NewsManager newsManager;

    @Getter
    private DiscordStats discordStats;

    @Getter
    private WaypointManager waypointManager;

    @Getter
    private WarpProxyManager warpProxyManager;

    @Getter
    private ConnectionTweakerManager connectionTweakerManager;

    @Getter
    private Update update;

    @Getter
    private ClickEffects clickEffects;

    @Getter
    private BlacklistManager blacklistManager;

    @Getter
    private RestrictedMod restrictedMod;

    @Getter
    private ShaderManager shaderManager;

    @Getter
    private RomanizationManager romanizationManager;

    @Getter
    private SkinManager skinManager;

    // API instance
    @Getter
    private ShindoAPI shindoAPI;

    public Shindo() {
        name = "Shindo";
        version = "5.1.09";
        author = "MikiDevAHM";
        verIdentifier = 5109;
    }

    public void start() {
        ShindoLogger.info("Starting Shindo");
        try {
            OptifineUtils.disableFastRender();
            this.removeOptifineZoom();
        } catch (Exception ignored) {
        }
        blacklistManager = new BlacklistManager();
        restrictedMod = new RestrictedMod();
        try {
            restrictedMod.shouldCheck = !System.getProperty("me.miki.shindo.blacklistchecks", "true").equalsIgnoreCase("false");
        } catch (Exception ignored) {
        }
        fileManager = new FileManager();
        languageManager = new LanguageManager();
        accountManager = new AccountManager();
        eventManager = new EventManager();
        downloadManager = new DownloadManager();
        modManager = new ModManager();
        addonManager = new AddonManager();

        CosmeticManager.getInstance().init();
        modManager.init();
        addonManager.init();

        warpProxyManager = new WarpProxyManager();
        connectionTweakerManager = new ConnectionTweakerManager();

        capeManager = new CapeManager();
        wingManager = new WingManager();
        bandannaManager = new BandannaManager();
        colorManager = new ColorManager();
        profileManager = new ProfileManager();
        musicManager = new MusicManager(fileManager);
        romanizationManager = new RomanizationManager();
        skinManager = new SkinManager();

        shindoAPI = new ShindoAPI();
        shindoAPI.init();

        commandManager = new CommandManager();
        screenshotManager = new ScreenshotManager();
        notificationManager = new NotificationManager();
        securityFeatureManager = new SecurityFeatureManager();
        quickPlayManager = new QuickPlayManager();
        changelogManager = new ChangelogManager();
        waypointManager = new WaypointManager();
        newsManager = new NewsManager();
        discordStats = new DiscordStats();
        discordStats.check();
        update = new Update();
        update.check();

        eventManager.register(new ShindoHandler());

        InternalSettingsMod.getInstance().setToggled(true);
        clickEffects = new ClickEffects();
        shaderManager = new ShaderManager();
        shaderManager.init();
        mc.updateDisplay();
    }

    public void stop() {
        ShindoLogger.info("Stopping Shindo");
        profileManager.save();
        accountManager.save();
        shindoAPI.stop();

        if (shaderManager != null) {
            shaderManager.cleanup();
        }

        Sound.play("shindo/audio/close.wav", true);

        if (romanizationManager != null) {
            romanizationManager.shutdown();
        }
    }

    private void removeOptifineZoom() {
        if (ShindoTweaker.hasOptifine) {
            try {
                this.unregisterKeybind((KeyBinding) GameSettings.class.getField("ofKeyBindZoom").get(mc.gameSettings));
            } catch (Exception e) {
                ShindoLogger.error("Failed to unregister zoom key", e);
            }
        }
    }

    private void unregisterKeybind(KeyBinding key) {
        if (Arrays.asList(mc.gameSettings.keyBindings).contains(key)) {
            mc.gameSettings.keyBindings = ArrayUtils.remove(mc.gameSettings.keyBindings, Arrays.asList(mc.gameSettings.keyBindings).indexOf(key));
            key.setKeyCode(0);
        }
    }

    public boolean getUpdateNeeded() {
        return updateNeeded;
    }

}
