package me.miki.shindo.management.account;

import java.io.File;

public class Account {

    private String name;
    private String uuid;
    private AccountType type;
    private String refreshToken;

    private File skinFile;

    public Account(String name, String uuid, AccountType type) {
        this(name, uuid, type, "");
    }

    public Account(String name, String uuid, AccountType type, String refreshToken) {
        this.name = name;
        this.uuid = uuid;
        this.type = type;
        this.refreshToken = refreshToken == null ? "" : refreshToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public File getSkinFile() {
        return skinFile;
    }

    public void setSkinFile(File skinFile) {
        this.skinFile = skinFile;
    }
}
