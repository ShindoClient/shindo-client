package me.miki.shindo.gui.mainmenu.impl.login;

import com.mojang.util.UUIDTypeAdapter;
import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.gui.mainmenu.impl.MainScene;
import me.miki.shindo.gui.mainmenu.impl.welcome.LastMessageScene;
import me.miki.shindo.libs.openauth.microsoft.AuthTokens;
import me.miki.shindo.libs.openauth.microsoft.MicrosoftAuthResult;
import me.miki.shindo.libs.openauth.microsoft.MicrosoftAuthenticationException;
import me.miki.shindo.libs.openauth.microsoft.MicrosoftAuthenticator;
import me.miki.shindo.libs.openauth.microsoft.model.response.MicrosoftDeviceCodePollResponse;
import me.miki.shindo.libs.openauth.microsoft.model.response.MicrosoftDeviceCodeResponse;
import me.miki.shindo.libs.openauth.microsoft.model.response.MicrosoftRefreshResponse;
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
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.SessionUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class MicrosoftLoginScene extends MainMenuScene {

    private final SkinDownloader skinDownloader;
    private volatile MicrosoftDeviceCodeResponse deviceCode;
    private volatile String statusMessage = TranslateText.LOADING.getText();
    private volatile boolean cancelPolling;
    private volatile boolean requestingCode;
    private volatile long expiresAtMillis;
    private volatile boolean urlOpened;
    private volatile long copyTooltipUntil;
    private volatile String verificationUrl = "";
    private final AtomicBoolean polling = new AtomicBoolean(false);

    public MicrosoftLoginScene(GuiShindoMainMenu parent) {
        super(parent);
        skinDownloader = new SkinDownloader();
    }

    @Override
    public void initScene() {
        super.initScene();
        startDeviceCodeFlow();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG(mouseX, mouseY, sr, nvg));
    }

    private void drawNanoVG(int mouseX, int mouseY, ScaledResolution sr, NanoVGManager nvg) {
        int acWidth = 260;
        int acHeight = 190;
        int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
        int acY = sr.getScaledHeight() / 2 - (acHeight / 2);

        nvg.drawRoundedRect(acX, acY, acWidth, acHeight, 8, this.getBackgroundColor());

        nvg.drawText(LegacyIcon.ARROW_LEFT, acX + 10, acY + 10, Color.WHITE, 9, Fonts.LEGACYICON);

        String loginMessage = TranslateText.LOGIN_MESSAGE.getText();
        String microsoftLogin = TranslateText.MICROSOFT_LOGIN.getText();
        nvg.drawCenteredText(loginMessage, acX + (acWidth / 2F), acY + 9, Color.WHITE, 14, Fonts.REGULAR);
        nvg.drawCenteredText(microsoftLogin, acX + (acWidth / 2F), acY + 35, Color.WHITE, 14, Fonts.REGULAR);

        int contentY = acY + 52;

        MicrosoftDeviceCodeResponse currentCode = this.deviceCode;
        if (currentCode == null) {
            nvg.drawCenteredText(statusMessage, acX + (acWidth / 2F), contentY + 22, Color.WHITE, 12, Fonts.REGULAR);
        } else {
            String message = currentCode.getMessage() != null ? currentCode.getMessage() : "";
            String[] lines = message.split("\\n");
            int offset = 0;
            for (String line : lines) {
                nvg.drawCenteredText(line.trim(), acX + (acWidth / 2F), contentY + offset, Color.WHITE, 10, Fonts.REGULAR);
                offset += 12;
            }

            int codeBoxY = contentY + offset + 6;
            int codeBoxWidth = 140;
            int codeBoxHeight = 30;
            int codeBoxX = acX + (acWidth / 2) - (codeBoxWidth / 2);
            nvg.drawRoundedRect(codeBoxX, codeBoxY, codeBoxWidth, codeBoxHeight, 6, new Color(0, 0, 0, 60));
            String userCode = currentCode.getUserCode();
            if (userCode == null || userCode.trim().isEmpty()) {
                userCode = statusMessage != null ? statusMessage : "";
            }
            nvg.drawCenteredText(userCode, acX + (acWidth / 2F), codeBoxY + 10, Color.WHITE, 16, Fonts.REGULAR);

            long remaining = Math.max(0L, (expiresAtMillis - System.currentTimeMillis()) / 1000L);
            String expiresText = String.format(TranslateText.MICROSOFT_LOGIN_EXPIRES.getText(), remaining);
            nvg.drawCenteredText(expiresText, acX + (acWidth / 2F), codeBoxY + codeBoxHeight + 10, Color.WHITE, 10, Fonts.REGULAR);

            nvg.drawCenteredText(statusMessage, acX + (acWidth / 2F), codeBoxY + codeBoxHeight + 24, Color.WHITE, 10, Fonts.REGULAR);

            int buttonWidth = 120;
            int buttonHeight = 20;
            int copyButtonX = acX + (acWidth / 2) - (buttonWidth / 2);
            int copyButtonY = codeBoxY + codeBoxHeight + 40;

            boolean copyHover = MouseUtils.isInside(mouseX, mouseY, copyButtonX, copyButtonY, buttonWidth, buttonHeight);
            Color copyColor = copyHover ? new Color(255, 255, 255, 80) : new Color(255, 255, 255, 40);
            nvg.drawRoundedRect(copyButtonX, copyButtonY, buttonWidth, buttonHeight, 4, copyColor);
            nvg.drawCenteredText(TranslateText.MICROSOFT_LOGIN_COPY_LINK_BUTTON.getText(), acX + (acWidth / 2F), copyButtonY + 6, Color.BLACK, 10, Fonts.REGULAR);

            long now = System.currentTimeMillis();
            if (now < copyTooltipUntil) {
                nvg.drawCenteredText(TranslateText.MICROSOFT_LOGIN_COPIED.getText(), acX + (acWidth / 2F), copyButtonY + buttonHeight + 12, Color.WHITE, 9, Fonts.REGULAR);
            } else {
                nvg.drawCenteredText(TranslateText.MICROSOFT_LOGIN_CLICK_MANUAL.getText(), acX + (acWidth / 2F), copyButtonY + buttonHeight + 12, Color.WHITE, 9, Fonts.REGULAR);
            }

            int retryY = copyButtonY + buttonHeight + 30;
            boolean retryHover = MouseUtils.isInside(mouseX, mouseY, copyButtonX, retryY, buttonWidth, buttonHeight);
            Color retryColor = retryHover ? new Color(255, 255, 255, 55) : new Color(255, 255, 255, 30);
            nvg.drawRoundedRect(copyButtonX, retryY, buttonWidth, buttonHeight, 4, retryColor);
            nvg.drawCenteredText(TranslateText.MICROSOFT_LOGIN_RETRY_BUTTON.getText(), acX + (acWidth / 2F), retryY + 6, Color.BLACK, 10, Fonts.REGULAR);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution sr = new ScaledResolution(mc);
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        int acWidth = 260;
        int acHeight = 190;
        int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
        int acY = sr.getScaledHeight() / 2 - (acHeight / 2);

        if (mouseButton == 0) {
            if (MouseUtils.isInside(mouseX, mouseY, acX + 10, acY + 10, nvg.getTextWidth(LegacyIcon.BACK, 9, Fonts.LEGACYICON), nvg.getTextHeight(LegacyIcon.BACK, 9, Fonts.LEGACYICON))) {
                cancelPendingPolling();
                this.setCurrentScene(getSceneByClass(AccountScene.class));
                return;
            }

            MicrosoftDeviceCodeResponse currentCode = this.deviceCode;
            if (currentCode != null) {
                int buttonWidth = 120;
                int buttonHeight = 20;
                int codeBoxY = acY + 52 + computeMessageHeight(currentCode) + 6;
                int copyButtonX = acX + (acWidth / 2) - (buttonWidth / 2);
                int copyButtonY = codeBoxY + 30 + 40;

                if (MouseUtils.isInside(mouseX, mouseY, copyButtonX, copyButtonY, buttonWidth, buttonHeight)) {
                    copyVerificationUrl();
                    return;
                }

                int retryY = copyButtonY + buttonHeight + 30;
                if (MouseUtils.isInside(mouseX, mouseY, copyButtonX, retryY, buttonWidth, buttonHeight)) {
                    startDeviceCodeFlow();
                    return;
                }
            } else {
                startDeviceCodeFlow();
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (Shindo.getInstance().getAccountManager().getCurrentAccount() != null) {
            if (keyCode == 1) {
                cancelPendingPolling();
                this.setCurrentScene(this.getSceneByClass(MainScene.class));
            }
        }
    }

    private void setStatus(TranslateText translateText) {
        statusMessage = translateText.getText();
    }

    private void setStatus(TranslateText translateText, Object... args) {
        statusMessage = String.format(Locale.ROOT, translateText.getText(), args);
    }

    private void startDeviceCodeFlow() {
        if (requestingCode) {
            return;
        }

        cancelPendingPolling();
        requestingCode = true;
        statusMessage = TranslateText.LOADING.getText();
        deviceCode = null;
        verificationUrl = "";
        copyTooltipUntil = 0;
        urlOpened = false;

        Multithreading.runAsync(() -> {
            while (polling.get()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    requestingCode = false;
                    return;
                }
            }

            cancelPolling = false;
            MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
            try {
                MicrosoftDeviceCodeResponse response = authenticator.requestDeviceCode();
                if (cancelPolling) {
                    return;
                }

                deviceCode = response;
                verificationUrl = response.getVerificationUriComplete() != null && !response.getVerificationUriComplete().isEmpty()
                        ? response.getVerificationUriComplete()
                        : response.getVerificationUri();
                expiresAtMillis = System.currentTimeMillis() + response.getExpiresIn() * 1000L;
                setStatus(TranslateText.MICROSOFT_LOGIN_STATUS_WAITING);

                openVerificationUrl(verificationUrl);
                pollDeviceCode(authenticator, response);
            } catch (MicrosoftAuthenticationException e) {
                ShindoLogger.error("Failed to request Microsoft device code", e);
                setStatus(TranslateText.MICROSOFT_LOGIN_STATUS_START_FAILED);
                Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, TranslateText.MICROSOFT_LOGIN_NOTIFY_START_FAILED.getText(), NotificationType.ERROR);
            } finally {
                requestingCode = false;
            }
        });
    }

    private void pollDeviceCode(MicrosoftAuthenticator authenticator, MicrosoftDeviceCodeResponse response) {
        if (!polling.compareAndSet(false, true)) {
            return;
        }

        long intervalMillis = Math.max(5L, response.getInterval()) * 1000L;

        while (!cancelPolling) {
            if (expiresAtMillis > 0 && System.currentTimeMillis() >= expiresAtMillis) {
                setStatus(TranslateText.MICROSOFT_LOGIN_STATUS_CODE_EXPIRED);
                Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, TranslateText.MICROSOFT_LOGIN_NOTIFY_CODE_EXPIRED.getText(), NotificationType.ERROR);
                break;
            }

            try {
                MicrosoftDeviceCodePollResponse pollResponse = authenticator.pollDeviceCode(response.getDeviceCode());
                if (cancelPolling) {
                    break;
                }

                if (pollResponse.isSuccess()) {
                    handleSuccessfulLogin(authenticator, pollResponse);
                    break;
                }

                String errorCode = pollResponse.getError() != null ? pollResponse.getError().getError() : null;
                if ("authorization_pending".equalsIgnoreCase(errorCode)) {
                    setStatus(TranslateText.MICROSOFT_LOGIN_STATUS_WAITING);
                    sleep(intervalMillis);
                    continue;
                }

                if ("slow_down".equalsIgnoreCase(errorCode)) {
                    intervalMillis += 2000L;
                    setStatus(TranslateText.MICROSOFT_LOGIN_STATUS_WAITING);
                    sleep(intervalMillis);
                    continue;
                }

                if ("code_expired".equalsIgnoreCase(errorCode) || "expired_token".equalsIgnoreCase(errorCode)) {
                    setStatus(TranslateText.MICROSOFT_LOGIN_STATUS_CODE_EXPIRED);
                    Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, TranslateText.MICROSOFT_LOGIN_NOTIFY_CODE_EXPIRED.getText(), NotificationType.ERROR);
                    break;
                }

                if ("authorization_declined".equalsIgnoreCase(errorCode) || "access_denied".equalsIgnoreCase(errorCode)) {
                    setStatus(TranslateText.MICROSOFT_LOGIN_STATUS_CANCELLED);
                    Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, TranslateText.MICROSOFT_LOGIN_NOTIFY_CANCELLED.getText(), NotificationType.ERROR);
                    break;
                }

                String errorMessage = pollResponse.getError() != null ? pollResponse.getError().getErrorDescription() : TranslateText.MICROSOFT_LOGIN_UNKNOWN_ERROR.getText();
                setStatus(TranslateText.MICROSOFT_LOGIN_STATUS_FAILED_WITH_REASON, errorMessage);
                Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, TranslateText.MICROSOFT_LOGIN_NOTIFY_FAILED.getText(), NotificationType.ERROR);
                break;
            } catch (MicrosoftAuthenticationException e) {
                if (cancelPolling) {
                    break;
                }
                ShindoLogger.error("Failed to poll Microsoft device code", e);
                setStatus(TranslateText.MICROSOFT_LOGIN_STATUS_FAILED);
                Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, TranslateText.MICROSOFT_LOGIN_NOTIFY_FAILED.getText(), NotificationType.ERROR);
                break;
            }
        }

        polling.set(false);
    }

    private void handleSuccessfulLogin(MicrosoftAuthenticator authenticator, MicrosoftDeviceCodePollResponse pollResponse) throws MicrosoftAuthenticationException {
        MicrosoftRefreshResponse tokens = pollResponse.getTokens();
        MicrosoftAuthResult result = authenticator.loginWithTokens(new AuthTokens(tokens.getAccessToken(), tokens.getRefreshToken()), true);

        SessionUtils.getInstance().applyMicrosoftAuthResult(result);
        persistAccount(result);

        setStatus(TranslateText.MICROSOFT_LOGIN_STATUS_SUCCESS);
        Shindo.getInstance().getNotificationManager().post(TranslateText.ADDED,
                String.format(Locale.ROOT, TranslateText.MICROSOFT_LOGIN_NOTIFY_SUCCESS.getText(), result.getProfile().getName()), NotificationType.SUCCESS);

        cancelPolling = true;
        getAfterLoginRunnable().run();
    }

    private void persistAccount(MicrosoftAuthResult result) {
        Shindo instance = Shindo.getInstance();
        AccountManager accountManager = instance.getAccountManager();
        FileManager fileManager = instance.getFileManager();
        File headDir = new File(fileManager.getCacheDir(), "head");

        if (!headDir.exists()) {
            fileManager.createDir(headDir);
        }

        Account account = accountManager.getAccountByUuid(result.getProfile().getId());
        if (account == null) {
            account = accountManager.getAccountByName(result.getProfile().getName());
        }

        if (account == null) {
            account = new Account(result.getProfile().getName(), result.getProfile().getId(), AccountType.MICROSOFT);
            accountManager.getAccounts().add(account);
        }

        account.setName(result.getProfile().getName());
        account.setUuid(result.getProfile().getId());
        account.setRefreshToken(result.getRefreshToken());

        try {
            skinDownloader.downloadFace(headDir, account.getName(), UUIDTypeAdapter.fromString(account.getUuid()));
        } catch (Exception e) {
            ShindoLogger.error("Failed to download skin face for account " + account.getName(), e);
        }

        accountManager.setCurrentAccount(account);
        accountManager.save();
    }

    private void copyVerificationUrl() {
        if (verificationUrl == null || verificationUrl.isEmpty()) {
            return;
        }

        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(verificationUrl), null);
            copyTooltipUntil = System.currentTimeMillis() + 2000L;
        } catch (Exception e) {
            ShindoLogger.error("Failed to copy Microsoft login URL", e);
        }
    }

    private void openVerificationUrl(String url) {
        if (urlOpened || url == null || url.isEmpty()) {
            return;
        }

        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create(url));
                urlOpened = true;
            } catch (IOException | IllegalArgumentException e) {
                ShindoLogger.error("Failed to open Microsoft login URL", e);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cancelPolling = true;
        }
    }

    private void cancelPendingPolling() {
        cancelPolling = true;
    }

    private int computeMessageHeight(MicrosoftDeviceCodeResponse response) {
        String message = response.getMessage() != null ? response.getMessage() : "";
        return message.split("\\n").length * 12;
    }

    private Runnable getAfterLoginRunnable() {
        return () -> {
            if (Shindo.getInstance().getShindoAPI().isFirstLogin()) {
                setCurrentScene(getSceneByClass(LastMessageScene.class));
            } else {
                setCurrentScene(getSceneByClass(MainScene.class));
            }
        };
    }

    @Override
    public void onSceneClosed() {
        cancelPendingPolling();
        deviceCode = null;
        statusMessage = TranslateText.LOADING.getText();
        verificationUrl = "";
        copyTooltipUntil = 0;
        urlOpened = false;
        requestingCode = false;
    }
}
