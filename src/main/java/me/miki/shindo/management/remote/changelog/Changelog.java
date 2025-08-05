package me.miki.shindo.management.remote.changelog;

public class Changelog {

    private final String text;
    private final ChangelogType type;

    public Changelog(String text, ChangelogType type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public ChangelogType getType() {
        return type;
    }
}
