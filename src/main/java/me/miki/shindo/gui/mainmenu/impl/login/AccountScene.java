package me.miki.shindo.gui.mainmenu.impl.login;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.gui.mainmenu.impl.MainScene;
import me.miki.shindo.gui.mainmenu.impl.welcome.LastMessageScene;
import me.miki.shindo.injection.interfaces.IMixinMinecraft;
import me.miki.shindo.management.account.Account;
import me.miki.shindo.management.account.AccountManager;
import me.miki.shindo.management.account.AccountType;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.notification.NotificationType;
import me.miki.shindo.ui.comp.impl.field.CompMainMenuTextBox;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class AccountScene extends MainMenuScene {

    public CompMainMenuTextBox nameBox = new CompMainMenuTextBox();

    private File offlineSkin;

    public AccountScene(GuiShindoMainMenu parent) {
        super(parent);
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

        String offlineLogin = TranslateText.OFFLINE_LOGIN.getText();

        nvg.drawRoundedRect(acX, acY, acWidth, acHeight, 8, this.getBackgroundColor());
        nvg.drawCenteredText(loginMessage, acX + (acWidth / 2F), acY + 9, Color.WHITE, 14, Fonts.REGULAR);

        nvg.drawRoundedImage(new ResourceLocation("shindo/mainmenu/microsoft-background.png"), acX + 10, acY + 29, 200, 30, 5);

        nvg.drawText(microsoftLogin, acX + 45, acY + 40, Color.WHITE, 10, Fonts.REGULAR);

        nvg.drawRoundedRect(acX + 18, acY + 34, 9, 9, 1, new Color(247, 78, 30));
        nvg.drawRoundedRect(acX + 18 + 11, acY + 34, 9, 9, 1, new Color(127, 186, 0));
        nvg.drawRoundedRect(acX + 18, acY + 34 + 11, 9, 9, 1, new Color(0, 164, 239));
        nvg.drawRoundedRect(acX + 18 + 11, acY + 34 + 11, 9, 9, 1, new Color(255, 185, 0));

        nvg.drawCenteredText(offlineLogin, acX + (acWidth / 2F), acY + 67, Color.WHITE, 14, Fonts.REGULAR);

        nvg.drawRoundedRect(acX + acWidth - 30, acY + 86, 20, 20, 4, this.getBackgroundColor());
        nvg.drawText(LegacyIcon.USER, acX + acWidth - 25, acY + 91, Color.WHITE, 10, Fonts.LEGACYICON);

        nameBox.setBackgroundColor(this.getBackgroundColor());
        nameBox.setFontColor(Color.WHITE);
        nameBox.setPosition(acX + 10, acY + 86, 175, 20);
        nameBox.setEmptyText(LegacyIcon.PENCIL, TranslateText.NAME.getText());
        nameBox.draw(mouseX, mouseY, partialTicks);

        nvg.drawRoundedRect(acX + (acWidth / 2F) - (96 / 2F), acY + 86 + 25, 96, 20, 4, this.getBackgroundColor());
        nvg.drawCenteredText(TranslateText.LOGIN.getText(), acX + (acWidth / 2F), acY + 86 + 31, Color.WHITE, 10F, Fonts.REGULAR);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        ScaledResolution sr = new ScaledResolution(mc);

        Shindo instance = Shindo.getInstance();
        AccountManager accountManager = instance.getAccountManager();
        FileManager fileManager = instance.getFileManager();

        File headDir = new File(fileManager.getCacheDir(), "head");
        File renameFile = new File(fileManager.getCacheDir(), "skin/" + nameBox.getText() + ".png");

        int acWidth = 220;
        int acHeight = 140;
        int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
        int acY = sr.getScaledHeight() / 2 - (acHeight / 2);

        if (mouseButton == 0) {

            /*
            if (MouseUtils.isInside(mouseX, mouseY, acX + acWidth - 30, acY + 86, 20, 20)) {
                Multithreading.runAsync(() -> {

                    File selectSkin = FileUtils.selectImageFile();

                    if (selectSkin != null) {

                        try {

                            File copyFile = new File(fileManager.getCacheDir(), "skin/" + selectSkin.getName());
                            BufferedImage image = ImageIO.read(selectSkin);

                            if (image.getWidth() == 64 && image.getHeight() == 64) {
                                FileUtils.copyFile(selectSkin, copyFile);
                                offlineSkin = copyFile;
                            }

                        } catch (Exception e) {
                            ShindoLogger.error("An error occurred while copying image", e);
                            instance.getNotificationManager().post(TranslateText.ERROR, "An error occurred while copying the image", NotificationType.ERROR);
                        }
                    }
                });
            }

             */

            if (MouseUtils.isInside(mouseX, mouseY, acX + (acWidth / 2F) - (96 / 2F), acY + 86 + 25, 96, 20)) {

                if (nameBox.getText().isEmpty()) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "Please fill in all fields", NotificationType.ERROR);
                    return;
                }

                if (nameBox.getText().length() < 3) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "Name must be at least 3 characters long", NotificationType.ERROR);
                    return;
                }

                if (nameBox.getText().length() > 20) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "Name must be at most 20 characters long", NotificationType.ERROR);
                    return;
                }

                if (!nameBox.getText().matches("[a-zA-Z0-9_]+")) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "Name can only contain letters, numbers and underscores", NotificationType.ERROR);
                    return;
                }

                if (nameBox.getText().contains(" ")) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "Name cannot contain spaces", NotificationType.ERROR);
                    return;
                }

                if (accountManager.getAccountByName(nameBox.getText()) != null) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "An account with this name already exists", NotificationType.ERROR);
                    return;
                }

                if (renameFile.exists()) {
                    instance.getNotificationManager().post(TranslateText.ERROR, "A skin with this name already exists", NotificationType.ERROR);
                    return;
                }

                Account acc = new Account(nameBox.getText(), "0", AccountType.OFFLINE);

                /*
                if (offlineSkin != null) {
                    offlineSkin.renameTo(renameFile);
                    offlineSkin = renameFile;
                }



                if (offlineSkin != null && offlineSkin.exists()) {

                    acc.setSkinFile(offlineSkin);

                    try {

                        BufferedImage rawImage = ImageIO.read(offlineSkin);
                        BufferedImage headImage = ImageUtils.scissor(rawImage, 8, 8, 8, 8);
                        BufferedImage layerImage = ImageUtils.scissor(rawImage, 40, 8, 8, 8);
                        BufferedImage conbineImage = ImageUtils.combine(headImage, layerImage);

                        ImageIO.write(ImageUtils.resize(conbineImage, 128, 128), "png", new File(headDir, acc.getName() + ".png"));
                    } catch (Exception e) {
                        ShindoLogger.error("An error occurred while resizing the image", e);
                        instance.getNotificationManager().post(TranslateText.ERROR, "An error occurred while resizing the image", NotificationType.ERROR);
                    }
                }

                 */

                UUID offlineId = UUID.nameUUIDFromBytes(
                        ("OfflinePlayer:" + acc.getName()).getBytes(StandardCharsets.UTF_8)
                );

                ((IMixinMinecraft) mc).setSession(new Session(acc.getName(), offlineId.toString(), "0", "mojang"));

                if (accountManager.getAccountByName(acc.getName()) == null) {
                    accountManager.getAccounts().add(acc);
                }

                accountManager.setCurrentAccount(acc);
                accountManager.save();

                instance.getNotificationManager().post(TranslateText.ADDED, "Offline Account " + accountManager.getCurrentAccount().getName(), NotificationType.SUCCESS);

                offlineSkin = null;

                getAfterLoginRunnable().run();
            }

            if (MouseUtils.isInside(mouseX, mouseY, acX + 10, acY + 29, 200, 30)) {
                setCurrentScene(getSceneByClass(MicrosoftLoginScene.class));
            }
        }

        nameBox.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

        nameBox.keyTyped(typedChar, keyCode);

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
        nameBox.setText("");
    }
}
