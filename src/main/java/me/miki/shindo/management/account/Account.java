package me.miki.shindo.management.account;

import java.io.File;

public class Account {

    private String name;
    private String uuid;
    private String email;
    private String password;
    private AccountType type;

    private File skinFile;

    public Account(String name, String uuid, String email, String password, AccountType type) {
        this.name = name;
        this.uuid = uuid;
        this.email = email;
        this.password = password;
        this.type = type;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public File getSkinFile() {
        return skinFile;
    }

    public void setSkinFile(File skinFile) {
        this.skinFile = skinFile;
    }
}
