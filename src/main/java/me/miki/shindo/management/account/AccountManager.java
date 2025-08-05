package me.miki.shindo.management.account;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.miki.shindo.Shindo;
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

        if (getAccountByName(currentAccount) != null) {

            if (getAccountByName(currentAccount).getType().equals(AccountType.MICROSOFT)) {
                ShindoLogger.info("Login into Microsoft account");
                Account acc = getAccountByName(currentAccount);
                Multithreading.runAsync(() -> {
                    SessionUtils.getInstance().setUserMicrosoft(acc.getEmail(), acc.getPassword());
                });
            } else {
                ShindoLogger.info("Login into Offline account");
                Account acc = getAccountByName(currentAccount);
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
                accJsonObject.addProperty("Email", acc.getEmail());
                accJsonObject.addProperty("Password", acc.getPassword());
                accJsonObject.addProperty("Account Type", acc.getType().getId());

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

                        accounts.add(
                                new Account(
                                        JsonUtils.getStringProperty(accJsonObject, "Name", "null"),
                                        JsonUtils.getStringProperty(accJsonObject, "UUID", "null"),
                                        JsonUtils.getStringProperty(accJsonObject, "Email", "0"),
                                        JsonUtils.getStringProperty(accJsonObject, "Password", "0"),
                                        AccountType.getAccountTypeById(JsonUtils.getIntProperty(accJsonObject, "Account Type", 0)
                                        )
                                )
                        );
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

    public Account getAccountByEmail(String email) {

        for (Account acc : accounts) {
            if (acc.getEmail().equals(email)) {
                return acc;
            }
        }

        return null;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

}
