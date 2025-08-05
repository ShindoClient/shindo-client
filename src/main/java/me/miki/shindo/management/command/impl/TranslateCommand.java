package me.miki.shindo.management.command.impl;

import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.command.Command;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.ChatTranslateMod;
import me.miki.shindo.management.mods.settings.impl.ComboSetting;
import me.miki.shindo.management.mods.settings.impl.combo.Option;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.translate.Translator;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class TranslateCommand extends Command {

    private String to = Translator.JAPANESE;

    public TranslateCommand() {
        super("translate");
    }

    @Override
    public void onCommand(String message) {

        ComboSetting setting = ChatTranslateMod.getInstance().getLanguageSetting();
        Option option = setting.getOption();

        if (option.getTranslate().equals(TranslateText.JAPANESE)) {
            to = Translator.JAPANESE;
        } else if (option.getTranslate().equals(TranslateText.ENGLISH)) {
            to = Translator.ENGLISH;
        } else if (option.getTranslate().equals(TranslateText.CHINESE)) {
            to = Translator.CHINESE_SIMPLIFIED;
        } else if (option.getTranslate().equals(TranslateText.POLISH)) {
            to = Translator.POLISH;
        }

        String text = message;

        Multithreading.runAsync(() -> {
            try {
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[Translate] " + EnumChatFormatting.WHITE + Translator.translate(text, Translator.AUTO_DETECT, to)));
            } catch (Exception e) {
                ShindoLogger.error("Failed translate", e);
            }
        });
    }
}
