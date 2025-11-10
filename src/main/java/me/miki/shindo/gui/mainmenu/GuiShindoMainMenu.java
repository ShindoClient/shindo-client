package me.miki.shindo.gui.mainmenu;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.impl.*;
import me.miki.shindo.gui.mainmenu.impl.login.AccountScene;
import me.miki.shindo.gui.mainmenu.impl.login.MicrosoftLoginScene;
import me.miki.shindo.gui.mainmenu.impl.welcome.*;
import me.miki.shindo.injection.interfaces.IMixinMinecraft;
import me.miki.shindo.libs.openauth.microsoft.MicrosoftAuthenticationException;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.account.Account;
import me.miki.shindo.management.account.AccountManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.event.impl.EventRenderNotification;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.profile.mainmenu.impl.Background;
import me.miki.shindo.management.profile.mainmenu.impl.CustomBackground;
import me.miki.shindo.management.profile.mainmenu.impl.DefaultBackground;
import me.miki.shindo.management.profile.mainmenu.impl.ShaderBackground;
import me.miki.shindo.management.shader.ShaderBackgroundRenderer;
import me.miki.shindo.management.notification.NotificationType;
import me.miki.shindo.utils.SessionUtils;
import me.miki.shindo.utils.Sound;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GuiShindoMainMenu extends GuiScreen {

    private final ArrayList<MainMenuScene> scenes = new ArrayList<MainMenuScene>();

    private final SimpleAnimation accountAnimation = new SimpleAnimation();
    private final SimpleAnimation accountSelectFocusAnimation = new SimpleAnimation();
    private final SimpleAnimation skinFocusAnimation = new SimpleAnimation();
    private final SimpleAnimation shopFocusAnimation = new SimpleAnimation();
    private final SimpleAnimation backgroundSelectFocusAnimation = new SimpleAnimation();
    private final SimpleAnimation closeFocusAnimation = new SimpleAnimation();

    private final SimpleAnimation[] backgroundAnimations = new SimpleAnimation[2];

    private MainMenuScene currentScene;


    private Account removeAccount;

    private boolean soundPlayed = false;
    private boolean openAccount;

    private String errorMessage;


    private Animation fadeIconAnimation, fadeBackgroundAnimation;

    public GuiShindoMainMenu() {

        Shindo instance = Shindo.getInstance();

        for (int i = 0; i < backgroundAnimations.length; i++) {
            backgroundAnimations[i] = new SimpleAnimation();
        }

        scenes.add(new MainScene(this));
        scenes.add(new AccountScene(this));
        scenes.add(new MicrosoftLoginScene(this));
        scenes.add(new BackgroundScene(this));
        scenes.add(new ShopScene(this));
        scenes.add(new SkinScene(this));

        scenes.add(new UpdateScene(this));

        scenes.add(new WelcomeMessageScene(this));
        scenes.add(new LanguageSelectScene(this));
        scenes.add(new ThemeSelectScene(this));
        scenes.add(new AccentColorSelectScene(this));
        scenes.add(new LoginMessageScene(this));
        scenes.add(new FirstLoginScene(this));
        scenes.add(new CheckingDataScene(this));
        scenes.add(new LastMessageScene(this));


        if (instance.getShindoAPI().isFirstLogin()) {
            currentScene = getSceneByClass(WelcomeMessageScene.class);
        } else {
            if (instance.getAccountManager().getCurrentAccount() == null) {
                currentScene = getSceneByClass(AccountScene.class);
            } else if (instance.getUpdateNeeded()) {
                currentScene = getSceneByClass(UpdateScene.class);
            } else {
                currentScene = getSceneByClass(MainScene.class);
            }
        }
    }

    @Override
    public void initGui() {
        currentScene.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(mc);

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        boolean isFirstLogin = instance.getShindoAPI().isFirstLogin();

        if (removeAccount != null) {
            instance.getAccountManager().getAccounts().remove(removeAccount);
            instance.getAccountManager().save();
            removeAccount = null;
        }

        backgroundAnimations[0].setAnimation(Mouse.getX(), 16);
        backgroundAnimations[1].setAnimation(Mouse.getY(), 16);

        nvg.setupAndDraw(() -> {

            drawNanoVG(sr, instance, nvg);

            if (!isFirstLogin) {
                drawButtons(mouseX, mouseY, sr, nvg);
            }
        });

        if (!isFirstLogin) {
            drawAccount(mouseX, mouseY, instance, nvg);
        }

        if (currentScene != null) {
            currentScene.drawScreen(mouseX, mouseY, partialTicks);
        }

        if (fadeBackgroundAnimation == null || (fadeBackgroundAnimation != null && !fadeBackgroundAnimation.isDone(Direction.FORWARDS))) {
            nvg.setupAndDraw(() -> drawSplashScreen(sr, nvg));
            if (!soundPlayed) {
                Sound.play("shindo/audio/start.wav", true);
                soundPlayed = true;
            }
        }

        nvg.setupAndDraw(() -> {
            new EventRenderNotification().call();
        });

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawNanoVG(ScaledResolution sr, Shindo instance, NanoVGManager nvg) {

        String copyright = "Copyright Mojang AB. Do not distribute!";
        Background currentBackground = instance.getProfileManager().getBackgroundManager().getCurrentBackground();

        if (currentBackground instanceof DefaultBackground) {

            DefaultBackground bg = (DefaultBackground) currentBackground;

            nvg.drawImage(bg.getImage(), -21 + backgroundAnimations[0].getValue() / 90, backgroundAnimations[1].getValue() * -1 / 90, sr.getScaledWidth() + 21, sr.getScaledHeight() + 20);
        } else if (currentBackground instanceof CustomBackground) {

            CustomBackground bg = (CustomBackground) currentBackground;

            nvg.drawImage(bg.getImage(), -21 + backgroundAnimations[0].getValue() / 90, backgroundAnimations[1].getValue() * -1 / 90, sr.getScaledWidth() + 21, sr.getScaledHeight() + 20);
        } else if (currentBackground instanceof ShaderBackground) {

            ShaderBackground bg = (ShaderBackground) currentBackground;

            // Render animated shader background using our renderer
            ShaderBackgroundRenderer.renderShaderBackground(nvg, bg.getShaderFile(),
                    -21 + backgroundAnimations[0].getValue() / 90, backgroundAnimations[1].getValue() * -1 / 90,
                    sr.getScaledWidth() + 21, sr.getScaledHeight() + 20);
        }

        nvg.drawText(copyright, sr.getScaledWidth() - (nvg.getTextWidth(copyright, 9, Fonts.REGULAR)) - 4, sr.getScaledHeight() - 12, new Color(255, 255, 255), 9, Fonts.REGULAR);
        nvg.drawText("Shindo Client v" + instance.getVersion(), 4, sr.getScaledHeight() - 12, new Color(255, 255, 255), 9, Fonts.REGULAR);
    }

    private void drawButtons(int mouseX, int mouseY, ScaledResolution sr, NanoVGManager nvg) {

        closeFocusAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - 28, 6, 22, 22) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(sr.getScaledWidth() - 28, 6, 22, 22, 4, this.getBackgroundColor());
        nvg.drawCenteredText(LegacyIcon.X, sr.getScaledWidth() - 19F, 8F, new Color(255, 255 - (int) (closeFocusAnimation.getValue() * 200), 255 - (int) (closeFocusAnimation.getValue() * 200)), 18, Fonts.LEGACYICON);

        backgroundSelectFocusAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - 28 - 28, 6, 22, 22) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(sr.getScaledWidth() - (28 * 2), 6, 22, 22, 4, this.getBackgroundColor());
        nvg.drawCenteredText(LegacyIcon.IMAGE, sr.getScaledWidth() - (26 * 2) + 6.5F - 1.5F, 9.5F - 1.5F, new Color(255 - (int) (backgroundSelectFocusAnimation.getValue() * 200), 255, 255 - (int) (backgroundSelectFocusAnimation.getValue() * 200)), 18, Fonts.LEGACYICON);

        shopFocusAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - (28 * 3), 6, 22, 22) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(sr.getScaledWidth() - (28 * 3), 6, 22, 22, 4, this.getBackgroundColor());
        nvg.drawCenteredText(LegacyIcon.SHOPPING, sr.getScaledWidth() - (26 * 3) + 4.5F, 9.5F, new Color(255 - (int) (shopFocusAnimation.getValue() * 200), 255, 255), 15, Fonts.LEGACYICON);

        skinFocusAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - (28 * 4), 6, 22, 22) ? 1.0F : 0.0F, 16);

        // TODO: Adicionar o ícone das skins no arquivo .ttf
        nvg.drawRoundedRect(sr.getScaledWidth() - (28 * 4), 6, 22, 22, 4, this.getBackgroundColor());
        nvg.drawCenteredText(LegacyIcon.EDIT, sr.getScaledWidth() - (26 * 4) + 3.5F, 9.5F, new Color(255 - (int) (skinFocusAnimation.getValue() * 200), 255, 255), 15, Fonts.LEGACYICON);

    }

    private void drawAccount(int mouseX, int mouseY, Shindo instance, NanoVGManager nvg) {

        Account currentAccount = instance.getAccountManager().getCurrentAccount();

        nvg.setupAndDraw(() -> drawAccountNanoVG(mouseX, mouseY, instance, nvg, currentAccount));
    }

    private void drawAccountNanoVG(int mouseX, int mouseY, Shindo instance, NanoVGManager nvg, Account currentAccount) {

        AccountManager accountManager = instance.getAccountManager();

        if (accountManager.getCurrentAccount() != null) {
            File headFile = new File(instance.getFileManager().getCacheDir(), "head/" + accountManager.getCurrentAccount().getName() + ".png");
            String name = currentAccount.getName();
            ColorPalette palette = instance.getColorManager().getPalette();

            float maxUserWidth = nvg.getTextWidth(name, 9.5F, Fonts.REGULAR);
            float progress = accountAnimation.getValue();
            int size = accountManager.getAccounts().size() - 1;
            int offsetY = 20;

            for (Account acc : accountManager.getAccounts()) {

                float tWidth = nvg.getTextWidth(acc.getName(), 9.5F, Fonts.REGULAR);

                if (tWidth > maxUserWidth) {
                    maxUserWidth = tWidth;
                }
            }

            boolean isInsideAccount = MouseUtils.isInside(mouseX, mouseY, 6, 6, 20, 20);

            if (openAccount) {
                isInsideAccount = true;
            }

            if (MouseUtils.isInside(mouseX, mouseY, 6, 6, 20 + maxUserWidth + 18, 20)) {
                openAccount = isInsideAccount;
            } else {
                openAccount = isInsideAccount && MouseUtils.isInside(mouseX, mouseY, 6, 6, 20 + maxUserWidth + 18, 20 + (size * 20));
            }

            accountAnimation.setAnimation(openAccount ? 1.0F : 0F, 16);

            nvg.drawRoundedRect(6, 6, 20 + (progress * (maxUserWidth + 18)), 20 + (progress * 20 * size), 4, this.getBackgroundColor());

            if (!headFile.exists()) {
                nvg.drawPlayerHead(new ResourceLocation("textures/entity/steve.png"), 9, 9, 14, 14, 2);
            } else {
                nvg.drawRoundedImage(headFile, 9, 9, 14, 14, 2);
            }

            nvg.save();
            nvg.translate(-18 + (progress * 18), 0);
            nvg.drawText(name, 26, 13, new Color(255, 255, 255, (int) (progress * 255)), 9.5F, Fonts.REGULAR);
            nvg.drawText(LegacyIcon.PLUS, maxUserWidth + 29, 10F, new Color(255, 255, 255, (int) (progress * 255)), 13F, Fonts.LEGACYICON);
            nvg.restore();

            for (Account acc : accountManager.getAccounts()) {

                if (!acc.equals(currentAccount)) {

                    headFile = new File(instance.getFileManager().getCacheDir(), "head/" + acc.getName() + ".png");

                    nvg.save();
                    nvg.translate(0, -18 + (progress * 18));

                    if (!headFile.exists()) {
                        nvg.drawPlayerHead(new ResourceLocation("textures/entity/steve.png"), 9, 9 + offsetY, 14, 14, 2, accountAnimation.getValue());
                    } else {
                        nvg.drawRoundedImage(headFile, 9, 9 + offsetY, 14, 14, 2, accountAnimation.getValue());
                    }

                    nvg.restore();

                    nvg.save();
                    nvg.translate(-18 + (progress * 18), -18 + (progress * 18));

                    nvg.drawText(acc.getName(), 26, 13 + offsetY, new Color(255, 255, 255, (int) (progress * 255)), 9.5F, Fonts.REGULAR);
                    nvg.drawText(LegacyIcon.TRASH, maxUserWidth + 30, 11F + offsetY, palette.getMaterialRed((int) (progress * 255)), 10F, Fonts.LEGACYICON);

                    nvg.restore();

                    offsetY += 20;
                }
            }
        }
    }

    private void drawSplashScreen(ScaledResolution sr, NanoVGManager nvg) {

        if (fadeIconAnimation == null) {
            fadeIconAnimation = new DecelerateAnimation(100, 1);
            fadeIconAnimation.setDirection(Direction.FORWARDS);
            fadeIconAnimation.reset();
        }

        if (fadeIconAnimation != null) {

            if (fadeIconAnimation.isDone(Direction.FORWARDS) && fadeBackgroundAnimation == null) {
                fadeBackgroundAnimation = new DecelerateAnimation(500, 1);
                fadeBackgroundAnimation.setDirection(Direction.FORWARDS);
                fadeBackgroundAnimation.reset();
            }

            nvg.drawRect(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), new Color(0, 0, 0, fadeBackgroundAnimation != null ? (int) (255 - (fadeBackgroundAnimation.getValue() * 255)) : 255));
            nvg.drawCenteredText(LegacyIcon.SHINDO, sr.getScaledWidth() / 2F, (sr.getScaledHeight() / 2F) - (nvg.getTextHeight(LegacyIcon.SHINDO, 130, Fonts.LEGACYICON) / 2) - 1, new Color(255, 255, 255, (int) (255 - (fadeIconAnimation.getValue() * 255))), 130, Fonts.LEGACYICON);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        ScaledResolution sr = new ScaledResolution(mc);

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        AccountManager accountManager = instance.getAccountManager();
        boolean isFirstLogin = instance.getShindoAPI().isFirstLogin();

        if (mouseButton == 0 && !isFirstLogin) {

            if (MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - 28, 6, 22, 22)) {
                mc.shutdown();
            }

            if (MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - (28 * 2), 6, 22, 22) && !this.getCurrentScene().equals(getSceneByClass(BackgroundScene.class))) {
                this.setCurrentScene(this.getSceneByClass(BackgroundScene.class));
            }

            if (MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - (28 * 3), 6, 22, 22)) {
                this.setCurrentScene(this.getSceneByClass(ShopScene.class));
            }

            if (MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - (28 * 4), 6, 22, 22)) {
                this.setCurrentScene(this.getSceneByClass(SkinScene.class));
            }

            if (openAccount) {

                Account currentAccount = accountManager.getCurrentAccount();
                float maxUserWidth = nvg.getTextWidth(currentAccount.getName(), 9.5F, Fonts.REGULAR);
                int offsetY = 20;

                for (Account acc : accountManager.getAccounts()) {

                    float tWidth = nvg.getTextWidth(acc.getName(), 9.5F, Fonts.REGULAR);

                    if (tWidth > maxUserWidth) {
                        maxUserWidth = tWidth;
                    }
                }

                if (MouseUtils.isInside(mouseX, mouseY, maxUserWidth + 28, 9, 15, 15)) {
                    currentScene = getSceneByClass(AccountScene.class);
                }

                for (Account acc : accountManager.getAccounts()) {

                    if (!acc.equals(currentAccount)) {

                        if (MouseUtils.isInside(mouseX, mouseY, maxUserWidth + 28, 8 + offsetY, 15, 15)) {
                            removeAccount = acc;
                        }

                        if (MouseUtils.isInside(mouseX, mouseY, 6, 6 + offsetY, maxUserWidth + 20, 20)) {
                            switch (acc.getType()) {
                                case MICROSOFT:
                                    ShindoLogger.info("Microsoft Account Login");
                                    if (acc.getRefreshToken() == null || acc.getRefreshToken().isEmpty()) {
                                        Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, "Re-login required for this Microsoft account", NotificationType.ERROR);
                                        break;
                                    }
                                    try {
                                        SessionUtils.getInstance().loginMicrosoftAccount(acc);
                                        accountManager.setCurrentAccount(acc);
                                        accountManager.save();
                                        Shindo.getInstance().getNotificationManager().post(TranslateText.ADDED, "Microsoft Account " + acc.getName(), NotificationType.SUCCESS);
                                    } catch (MicrosoftAuthenticationException e) {
                                        ShindoLogger.error("Microsoft Authentication Failed", e);
                                        Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, "Microsoft login failed", NotificationType.ERROR);
                                    }
                                    break;
                                case OFFLINE:
                                    ShindoLogger.info("Offline Account Login");
                                    ((IMixinMinecraft) mc).setSession(new Session(acc.getName(), "0", "0", "mojang"));
                                    accountManager.setCurrentAccount(acc);
                                    break;
                                default:
                                    ShindoLogger.error("Invalid Account Type");
                                    break;
                            }

                            accountManager.save();
                        }

                        offsetY += 20;
                    }
                }
            }
        }

        currentScene.mouseClicked(mouseX, mouseY, mouseButton);
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (IOException e) {
            ShindoLogger.error("An error occurred while handling mouse released event", e);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        currentScene.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        currentScene.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleInput() throws IOException {
        super.handleInput();
    }

    @Override
    public void onGuiClosed() {
        currentScene.onGuiClosed();
    }

    public MainMenuScene getCurrentScene() {
        return currentScene;
    }

    public void setCurrentScene(MainMenuScene currentScene) {

        if (this.currentScene != null) {
            this.currentScene.onSceneClosed();
        }

        this.currentScene = currentScene;

        if (this.currentScene != null) {
            this.currentScene.initScene();
        }
    }

    public boolean isDoneBackgroundAnimation() {
        return fadeBackgroundAnimation != null && fadeBackgroundAnimation.isDone(Direction.FORWARDS);
    }

    public MainMenuScene getSceneByClass(Class<? extends MainMenuScene> clazz) {

        for (MainMenuScene s : scenes) {
            if (s.getClass().equals(clazz)) {
                return s;
            }
        }

        return null;
    }

    public Color getBackgroundColor() {
        return new Color(230, 230, 230, 120);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
