package me.miki.shindo.management.account;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.miki.shindo.Shindo;
import me.miki.shindo.libs.openauth.microsoft.MicrosoftAuthenticationException;
import me.miki.shindo.injection.interfaces.IMixinMinecraft;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.utils.JsonUtils;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.SessionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class AccountManager {

    private final ArrayList<Account> accounts = new ArrayList<Account>();
    private String currentAccount;

    public AccountManager() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File microsoftDir = new File(fileManager.getExternalDir(), "microsoft");
        File accountFile = new File(fileManager.getShindoDir(), "Account.json");
        File skinDir = new File(fileManager.getCacheDir(), "skin");

        if (!microsoftDir.exists()) {
            fileManager.createDir(microsoftDir);
        }

        if (!accountFile.exists()) {
            fileManager.createFile(accountFile);
        }

        if (!skinDir.exists()) {
            fileManager.createDir(skinDir);
        }

        if (accountFile.length() > 0) {
            load();
        }

        Account storedAccount = getAccountByName(currentAccount);
        if (storedAccount != null) {

            if (storedAccount.getType().equals(AccountType.MICROSOFT)) {
                ShindoLogger.info("Login into Microsoft account");
                Account acc = storedAccount;
                if (acc.getRefreshToken() == null || acc.getRefreshToken().isEmpty()) {
                    ShindoLogger.warn("Stored Microsoft account is missing a refresh token. Please sign in again.");
                } else {
                    Multithreading.runAsync(() -> {
                        try {
                            SessionUtils.getInstance().loginMicrosoftAccount(acc);
                            save();
                        } catch (MicrosoftAuthenticationException e) {
                            ShindoLogger.error("Failed to login Microsoft account", e);
                        }
                    });
                }
            } else {
                ShindoLogger.info("Login into Offline account");
                Account acc = storedAccount;
                File f = new File(skinDir, acc.getName() + ".png");

                UUID offlineId = UUID.nameUUIDFromBytes(
                        ("OfflinePlayer:" + acc.getName()).getBytes(StandardCharsets.UTF_8)
                );

                Minecraft mc = Minecraft.getMinecraft();
                ((IMixinMinecraft) mc).setSession(new Session(acc.getName(), offlineId.toString(), "0", "mojang"));

                if (f.exists()) {
                    acc.setSkinFile(f);
                } else {
                    mc.getTextureManager().bindTexture(new ResourceLocation("textures/entity/steve.png"));
                }
            }
        }
    }

    public void save() {

        FileManager fileManager = Shindo.getInstance().getFileManager();

        try (FileWriter writer = new FileWriter(new File(fileManager.getShindoDir(), "Account.json"))) {

            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();

            jsonObject.addProperty("Current Account", currentAccount);

            for (Account acc : accounts) {

                JsonObject accJsonObject = new JsonObject();

                accJsonObject.addProperty("Name", acc.getName());
                accJsonObject.addProperty("UUID", acc.getUuid());
                accJsonObject.addProperty("Account Type", acc.getType().getId());
                accJsonObject.addProperty("RefreshToken", acc.getRefreshToken());

                jsonArray.add(accJsonObject);
            }

            jsonObject.add("Accounts", jsonArray);

            gson.toJson(jsonObject, writer);
        } catch (Exception e) {
            ShindoLogger.error("Failed to save account", e);
        }
    }

    public void load() {

        FileManager fileManager = Shindo.getInstance().getFileManager();

        try (FileReader reader = new FileReader(new File(fileManager.getShindoDir(), "Account.json"))) {

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            if (jsonObject != null && jsonObject.isJsonObject()) {

                JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "Accounts");

                currentAccount = JsonUtils.getStringProperty(jsonObject, "Current Account", "null");

                if (jsonArray != null) {

                    Iterator<JsonElement> iterator = jsonArray.iterator();

                    while (iterator.hasNext()) {

                        JsonElement jsonElement = iterator.next();
                        JsonObject accJsonObject = gson.fromJson(jsonElement, JsonObject.class);

                        Account account = new Account(
                                JsonUtils.getStringProperty(accJsonObject, "Name", "null"),
                                JsonUtils.getStringProperty(accJsonObject, "UUID", "null"),
                                AccountType.getAccountTypeById(JsonUtils.getIntProperty(accJsonObject, "Account Type", 0)
                                )
                        );
                        account.setRefreshToken(JsonUtils.getStringProperty(accJsonObject, "RefreshToken", ""));
                        accounts.add(account);
                    }
                }
            }
        } catch (Exception e) {
            ShindoLogger.error("Failed to load account", e);
        }
    }

    public Account getCurrentAccount() {
        return getAccountByName(currentAccount);
    }

    public void setCurrentAccount(Account account) {
        this.currentAccount = account.getName();
    }

    public Account getAccountByName(String name) {

        for (Account acc : accounts) {
            if (acc.getName().equals(name)) {
                return acc;
            }
        }

        return null;
    }

    public Account getAccountByUuid(String uuid) {
        if (uuid == null || uuid.equalsIgnoreCase("null")) {
            return null;
        }

        for (Account acc : accounts) {
            if (acc.getUuid() != null && acc.getUuid().equalsIgnoreCase(uuid)) {
                return acc;
            }
        }

        return null;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

}
