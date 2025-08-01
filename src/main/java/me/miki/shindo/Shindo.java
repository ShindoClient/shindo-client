package me.miki.shindo;

import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.injection.mixin.ShindoTweaker;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.account.AccountManager;
import me.miki.shindo.management.cape.CapeManager;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.command.CommandManager;
import me.miki.shindo.management.event.EventManager;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.LanguageManager;
import me.miki.shindo.management.mods.ModManager;
import me.miki.shindo.management.mods.RestrictedMod;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.music.MusicManager;
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
import me.miki.shindo.management.roles.ClientRoleManager;
import me.miki.shindo.management.screenshot.ScreenshotManager;
import me.miki.shindo.management.security.SecurityFeatureManager;
import me.miki.shindo.management.waypoint.WaypointManager;
import me.miki.shindo.ui.ClickEffects;
import me.miki.shindo.utils.OptifineUtils;
import me.miki.shindo.utils.Sound;
import me.miki.shindo.management.addons.rpo.RPOConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class Shindo {

	private static Shindo instance = new Shindo();
	private Minecraft mc = Minecraft.getMinecraft();
	private boolean updateNeeded;
	private String name, version;
	private int verIdentifier;
	
	private NanoVGManager nanoVGManager;
	private FileManager fileManager;
	private LanguageManager languageManager;
	private AccountManager accountManager;
	private EventManager eventManager;
	private DownloadManager downloadManager;
	private ModManager modManager;
	private CapeManager capeManager;
	private ColorManager colorManager;
	private ProfileManager profileManager;
	private CommandManager commandManager;
	private ScreenshotManager screenshotManager;
	private NotificationManager notificationManager;
	private SecurityFeatureManager securityFeatureManager;
	private MusicManager musicManager;
	private QuickPlayManager quickPlayManager;
	private ChangelogManager changelogManager;
	private NewsManager newsManager;
	private DiscordStats discordStats;
    private WaypointManager waypointManager;
	private GuiShindoMainMenu mainMenu;
	private Update update;
	private ClickEffects clickEffects;
	private BlacklistManager blacklistManager;
	private RestrictedMod restrictedMod;
	private ShindoAPI shindoAPI;
	
	public Shindo() {
		name = "Shindo";
		version = "5.1.08";
		verIdentifier = 5108;
	}
	
	public void start() {
		ShindoLogger.info("Starting Shindo");
		try {
			OptifineUtils.disableFastRender();
			this.removeOptifineZoom();
		} catch(Exception ignored) {}
		blacklistManager = new BlacklistManager();
		restrictedMod = new RestrictedMod();
		try {
			restrictedMod.shouldCheck = !System.getProperty("me.miki.shindo.blacklistchecks", "true").equalsIgnoreCase("false");
		} catch (Exception ignored) {}
		fileManager = new FileManager();
		languageManager = new LanguageManager();
		accountManager = new AccountManager();
		eventManager = new EventManager();
		downloadManager = new DownloadManager();
		modManager = new ModManager();
		
		modManager.init();
		
		capeManager = new CapeManager();
		colorManager = new ColorManager();
		profileManager = new ProfileManager();
		musicManager = new MusicManager();

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
		mc.updateDisplay();
	}
	
	public void stop() {
		ShindoLogger.info("Stopping Shindo");
		profileManager.save();
		accountManager.save();
		shindoAPI.disconnect();
		Sound.play("shindo/audio/close.wav", true);
	}
	
	private void removeOptifineZoom() {
		if(ShindoTweaker.hasOptifine) {
			try {
				this.unregisterKeybind((KeyBinding) GameSettings.class.getField("ofKeyBindZoom").get(mc.gameSettings));
			} catch(Exception e) {
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
    
	public static Shindo getInstance() {
		return instance;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {return version;}

	public int getVersionIdentifier() {return verIdentifier;}

	public FileManager getFileManager() {
		return fileManager;
	}

	public ModManager getModManager() {
		return modManager;
	}

	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public DownloadManager getDownloadManager() {
		return downloadManager;
	}

	public NanoVGManager getNanoVGManager() {
		return nanoVGManager;
	}

	public ColorManager getColorManager() {
		return colorManager;
	}

	public ProfileManager getProfileManager() {
		return profileManager;
	}

	public AccountManager getAccountManager() {
		return accountManager;
	}

	public CapeManager getCapeManager() {
		return capeManager;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public ScreenshotManager getScreenshotManager() {
		return screenshotManager;
	}

	public void setNanoVGManager(NanoVGManager nanoVGManager) {
		this.nanoVGManager = nanoVGManager;
	}

	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

	public SecurityFeatureManager getSecurityFeatureManager() {
		return securityFeatureManager;
	}

	public QuickPlayManager getQuickPlayManager() {
		return quickPlayManager;
	}

	public ChangelogManager getChangelogManager() {
		return changelogManager;
	}
	public NewsManager getNewsManager() { return newsManager; }

	public DiscordStats getDiscordStats() {
		return discordStats;
	}

	public WaypointManager getWaypointManager() {
		return waypointManager;
	}

	public MusicManager getMusicManager() {
		return musicManager;
	}

	public Update getUpdateInstance(){
		return update;
	}

	public ShindoAPI getShindoAPI() {
		return shindoAPI;
	}

	public void setUpdateNeeded(boolean in) {updateNeeded = in;}

	public boolean getUpdateNeeded() {return updateNeeded;}

	public ClickEffects getClickEffects() {return clickEffects;}

	public BlacklistManager getBlacklistManager() { return blacklistManager; }

	public RestrictedMod getRestrictedMod() { return restrictedMod; }
}
