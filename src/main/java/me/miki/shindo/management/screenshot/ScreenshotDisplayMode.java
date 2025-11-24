package me.miki.shindo.management.screenshot;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.PropertyEnum;

public enum ScreenshotDisplayMode implements PropertyEnum {
    GRID(TranslateText.SCREENSHOT_MODE_GRID, TranslateText.SCREENSHOT_MODE_GRID_DESCRIPTION),
    FILMSTRIP(TranslateText.SCREENSHOT_MODE_FILMSTRIP, TranslateText.SCREENSHOT_MODE_FILMSTRIP_DESCRIPTION);

    private final TranslateText title;
    private final TranslateText description;

    ScreenshotDisplayMode(TranslateText title, TranslateText description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public TranslateText getTranslate() {
        return title;
    }

    public String getDescription() {
        return description.getText();
    }
}
