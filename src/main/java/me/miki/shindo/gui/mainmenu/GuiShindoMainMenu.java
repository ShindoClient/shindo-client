package me.miki.shindo.gui.mainmenu;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.impl.*;
import me.miki.shindo.gui.mainmenu.impl.welcome.*;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.Theme;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
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
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.SkinUtils;
import me.miki.shindo.utils.Sound;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;

public class GuiShindoMainMenu extends GuiScreen {

    private final ArrayList<MainMenuScene> scenes = new ArrayList<MainMenuScene>();

    private final SimpleAnimation skinFocusAnimation = new SimpleAnimation();
    private final SimpleAnimation shopFocusAnimation = new SimpleAnimation();
    private final SimpleAnimation backgroundSelectFocusAnimation = new SimpleAnimation();
    private final SimpleAnimation closeFocusAnimation = new SimpleAnimation();

    private final SimpleAnimation[] backgroundAnimations = new SimpleAnimation[2];

    @Getter
    private MainMenuScene currentScene;


    private boolean soundPlayed = false;


    private Animation fadeIconAnimation, fadeBackgroundAnimation;

    public GuiShindoMainMenu() {

        Shindo instance = Shindo.getInstance();
        boolean firstLogin = instance.getShindoAPI().isFirstLogin();
        ensureDefaultColorScheme(instance, firstLogin);

        for (int i = 0; i < backgroundAnimations.length; i++) {
            backgroundAnimations[i] = new SimpleAnimation();
        }

        scenes.add(new MainScene(this));
        scenes.add(new BackgroundScene(this));
        scenes.add(new ShopScene(this));
        scenes.add(new SkinScene(this));

        scenes.add(new UpdateScene(this));

        scenes.add(new WelcomeMessageScene(this));
        scenes.add(new LanguageSelectScene(this));
        scenes.add(new ThemeSelectScene(this));
        scenes.add(new AccentColorSelectScene(this));
        scenes.add(new CheckingDataScene(this));
        scenes.add(new LastMessageScene(this));


        if (firstLogin) {
            currentScene = getSceneByClass(WelcomeMessageScene.class);
        } else {
            if (instance.getUpdateNeeded()) {
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

        backgroundAnimations[0].setAnimation(Mouse.getX(), 16);
        backgroundAnimations[1].setAnimation(Mouse.getY(), 16);

        nvg.setupAndDraw(() -> {

            drawNanoVG(sr, instance, nvg);

            if (!isFirstLogin) {
                drawButtons(mouseX, mouseY, sr, nvg);
            }
        });

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
            ShaderBackgroundRenderer.renderShaderBackground(nvg, bg.getShaderFile(), -21 + backgroundAnimations[0].getValue() / 90, backgroundAnimations[1].getValue() * -1 / 90, sr.getScaledWidth() + 21, sr.getScaledHeight() + 20);
        }

        nvg.drawText(copyright, sr.getScaledWidth() - (nvg.getTextWidth(copyright, 9, Fonts.REGULAR)) - 4, sr.getScaledHeight() - 12, new Color(255, 255, 255), 9, Fonts.REGULAR);
        nvg.drawText("Shindo Client v" + instance.getVersion(), 4, sr.getScaledHeight() - 12, new Color(255, 255, 255), 9, Fonts.REGULAR);
    }

    private void drawButtons(int mouseX, int mouseY, ScaledResolution sr, NanoVGManager nvg) {


        Color controlColor = getControlFillColor();

        closeFocusAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - 28, 6, 22, 22) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(sr.getScaledWidth() - 28, 6, 22, 22, 4, controlColor);
        nvg.drawCenteredText(LegacyIcon.X, sr.getScaledWidth() - 19F, 8F, new Color(255, 255 - (int) (closeFocusAnimation.getValue() * 200), 255 - (int) (closeFocusAnimation.getValue() * 200)), 18, Fonts.LEGACYICON);

        backgroundSelectFocusAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - 28 - 28, 6, 22, 22) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(sr.getScaledWidth() - (28 * 2), 6, 22, 22, 4, controlColor);
        nvg.drawCenteredText(LegacyIcon.IMAGE, sr.getScaledWidth() - (26 * 2) + 6.5F - 1.5F, 9.5F - 1.5F, new Color(255 - (int) (backgroundSelectFocusAnimation.getValue() * 200), 255, 255 - (int) (backgroundSelectFocusAnimation.getValue() * 200)), 18, Fonts.LEGACYICON);

        shopFocusAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - (28 * 3), 6, 22, 22) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(sr.getScaledWidth() - (28 * 3), 6, 22, 22, 4, controlColor);
        nvg.drawCenteredText(LegacyIcon.SHOPPING, sr.getScaledWidth() - (26 * 3) + 4.5F, 9.5F, new Color(255 - (int) (shopFocusAnimation.getValue() * 200), 255, 255), 15, Fonts.LEGACYICON);

        skinFocusAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - (28 * 4), 6, 22, 22) ? 1.0F : 0.0F, 16);

        nvg.drawRoundedRect(sr.getScaledWidth() - (28 * 4), 6, 22, 22, 4, controlColor);
        nvg.drawCenteredText(LegacyIcon.SKIN, sr.getScaledWidth() - (26 * 4) + 3.5F, 9.5F, new Color(255 - (int) (skinFocusAnimation.getValue() * 200), 255, 255), 15, Fonts.LEGACYICON);

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

        boolean isFirstLogin = Shindo.getInstance().getShindoAPI().isFirstLogin();

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
        }

        currentScene.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
    public void handleInput() {
        super.handleInput();
    }

    @Override
    public void onGuiClosed() {
        currentScene.onGuiClosed();
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

    private void ensureDefaultColorScheme(Shindo instance, boolean forceDefaults) {
        if (instance == null) {
            return;
        }
        ColorManager colorManager = instance.getColorManager();
        if (colorManager == null) {
            return;
        }
        if (forceDefaults || colorManager.getCurrentColor() == null) {
            colorManager.setCurrentColor(colorManager.getColorByName("Default"));
        }
        if (forceDefaults || colorManager.getTheme() == null) {
            colorManager.setTheme(Theme.DARK);
        }
    }

    private Color getControlFillColor() {
        Shindo instance = Shindo.getInstance();
        ColorPalette palette = instance != null && instance.getColorManager() != null ? instance.getColorManager().getPalette() : null;
        Color base = palette != null ? palette.getBackgroundColor(ColorType.NORMAL) : getBackgroundColor();
        return ColorUtils.applyAlpha(base, Math.min(255, base.getAlpha() + 5));
    }

    public Color getBackgroundColor() {
        Shindo instance = Shindo.getInstance();
        ColorPalette palette = instance != null && instance.getColorManager() != null ? instance.getColorManager().getPalette() : null;
        Color base = palette != null ? palette.getBackgroundColor(ColorType.DARK) : new Color(30, 30, 30);
        return ColorUtils.applyAlpha(base, 235);
    }

}
