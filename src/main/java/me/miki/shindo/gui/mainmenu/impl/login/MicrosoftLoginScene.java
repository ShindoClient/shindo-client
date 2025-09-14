package me.miki.shindo.gui.mainmenu.impl.login;

import com.mojang.util.UUIDTypeAdapter;
import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.gui.mainmenu.impl.MainScene;
import me.miki.shindo.gui.mainmenu.impl.welcome.LastMessageScene;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.account.Account;
import me.miki.shindo.management.account.AccountManager;
import me.miki.shindo.management.account.AccountType;
import me.miki.shindo.management.account.skin.SkinDownloader;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.notification.NotificationType;
import me.miki.shindo.ui.comp.field.CompMainMenuTextBox;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.SessionUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.File;


public class MicrosoftLoginScene extends MainMenuScene {

    private final SkinDownloader skinDownloader;
    public CompMainMenuTextBox emailBox = new CompMainMenuTextBox();
    public CompMainMenuTextBox passwordBox = new CompMainMenuTextBox();

    public MicrosoftLoginScene(GuiShindoMainMenu parent) {
        super(parent);
        skinDownloader = new SkinDownloader();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        ScaledResolution sr = new ScaledResolution(mc);
        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG(mouseX, mouseY, partialTicks, sr, instance, nvg));
    }

    public void drawNanoVG(int mouseX, int mouseY, float partialTicks, ScaledResolution sr, Shindo instance, NanoVGManager nvg) {

        int acWidth = 220;
        int acHeight = 138;
        int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
        int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
        String loginMessage = TranslateText.LOGIN_MESSAGE.getText();
        String microsoftLogin = TranslateText.MICROSOFT_LOGIN.getText();

        nvg.drawRoundedRect(acX, acY, acWidth, acHeight, 8, this.getBackgroundColor());

        nvg.drawText(LegacyIcon.ARROW_LEFT, acX + 10, acY + 10, Color.WHITE, 9, Fonts.LEGACYICON);

        nvg.drawCenteredText(loginMessage, acX + (acWidth / 2F), acY + 9, Color.WHITE, 14, Fonts.REGULAR);
        nvg.drawCenteredText(microsoftLogin, acX + (acWidth / 2F), acY + 40, Color.WHITE, 14, Fonts.REGULAR);

        emailBox.setBackgroundColor(this.getBackgroundColor());
        emailBox.setFontColor(Color.WHITE);
        emailBox.setPosition(acX + 20, acY + 56, 180, 20);
        emailBox.setEmptyText(LegacyIcon.PENCIL, "EMAIL");
        emailBox.draw(mouseX, mouseY, partialTicks);

        passwordBox.setBackgroundColor(this.getBackgroundColor());
        passwordBox.setFontColor(Color.WHITE);
        passwordBox.setPosition(acX + 20, acY + 86, 180, 20);
        passwordBox.setEmptyText(LegacyIcon.PENCIL, "PASSWORD");
        passwordBox.setPasswordMode(true);
        passwordBox.draw(mouseX, mouseY, partialTicks);


        nvg.drawRoundedRect(acX + (acWidth / 2F) - (96 / 2F), acY + 86 + 25, 96, 20, 4, this.getBackgroundColor());
        nvg.drawCenteredText(TranslateText.LOGIN.getText(), acX + (acWidth / 2F), acY + 86 + 31, Color.WHITE, 10F, Fonts.REGULAR);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution sr = new ScaledResolution(mc);

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        AccountManager accountManager = instance.getAccountManager();
        FileManager fileManager = instance.getFileManager();
        File headDir = new File(fileManager.getCacheDir(), "head");
        int acWidth = 220;
        int acHeight = 140;
        int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
        int acY = sr.getScaledHeight() / 2 - (acHeight / 2);

        if (mouseButton == 0) {

            if (MouseUtils.isInside(mouseX, mouseY, acX + (acWidth / 2F) - (96 / 2F), acY + 86 + 25, 96, 20)) {

                if (emailBox.getText().isEmpty() || passwordBox.getText().isEmpty()) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "Please fill in all fields", NotificationType.ERROR);
                    return;
                }

                if (emailBox.getText().length() < 3 || passwordBox.getText().length() < 3) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "Email and Password must be at least 3 characters long", NotificationType.ERROR);
                    return;
                }

                if (!emailBox.getText().contains("@")) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "Invalid email format", NotificationType.ERROR);
                    return;
                }

                if (accountManager.getAccountByEmail(emailBox.getText()) != null) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "An account with this email already exists", NotificationType.ERROR);
                    return;
                }

                Multithreading.runAsync(() -> {
                    try {
                        Account acc = new Account("", "", emailBox.getText(), passwordBox.getText(), AccountType.MICROSOFT);

                        if (!headDir.exists()) {
                            fileManager.createDir(headDir);
                        }

                        SessionUtils.getInstance().setUserMicrosoft(acc.getEmail(), acc.getPassword());

                        acc.setName(mc.getSession().getUsername());
                        acc.setUuid(mc.getSession().getProfile().getId().toString());

                        skinDownloader.downloadFace(headDir, acc.getName(), UUIDTypeAdapter.fromString(acc.getUuid()));

                        if (accountManager.getAccountByName(acc.getName()) == null) {
                            accountManager.getAccounts().add(acc);
                        }

                        accountManager.setCurrentAccount(acc);
                        accountManager.save();

                        instance.getNotificationManager().post(TranslateText.ADDED, "Microsoft Account " + accountManager.getCurrentAccount().getName(), NotificationType.SUCCESS);

                        getAfterLoginRunnable().run();
                    } catch (Exception e) {
                        ShindoLogger.error("AN error occurred while logging in with Microsoft", e);
                        instance.getNotificationManager().post(TranslateText.ERROR, "Login Failed", NotificationType.ERROR);
                    }
                });

            }

            if (MouseUtils.isInside(mouseX, mouseY, acX + 10, acY + 10, nvg.getTextWidth(LegacyIcon.BACK, 9, Fonts.LEGACYICON), nvg.getTextHeight(LegacyIcon.BACK, 9, Fonts.LEGACYICON))) {
                this.setCurrentScene(getSceneByClass(AccountScene.class));
            }
        }

        emailBox.mouseClicked(mouseX, mouseY, mouseButton);
        passwordBox.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

        emailBox.keyTyped(typedChar, keyCode);
        passwordBox.keyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_ESCAPE && Shindo.getInstance().getAccountManager().getCurrentAccount() != null) {
            this.setCurrentScene(this.getSceneByClass(MainScene.class));
        }
    }

    private Runnable getAfterLoginRunnable() {

        return new Runnable() {
            @Override
            public void run() {
                if (Shindo.getInstance().getShindoAPI().isFirstLogin()) {
                    setCurrentScene(getSceneByClass(LastMessageScene.class));
                } else {
                    setCurrentScene(getSceneByClass(MainScene.class));
                }
            }
        };
    }

    @Override
    public void onSceneClosed() {
        emailBox.setText("");
        passwordBox.setText("");
    }
}
