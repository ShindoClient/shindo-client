package me.miki.shindo.management.account;

public enum AccountType {
    MICROSOFT(1), OFFLINE(0);

    private final int id;

    AccountType(int id) {
        this.id = id;
    }

    public static AccountType getAccountTypeById(int id) {

        for (AccountType acc : AccountType.values()) {
            if (acc.getId() == id) {
                return acc;
            }
        }

        return null;
    }

    public int getId() {
        return id;
    }
}
