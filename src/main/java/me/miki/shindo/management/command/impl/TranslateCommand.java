package me.miki.shindo.management.command.impl;

import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.command.Command;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.ChatTranslateMod;
import me.miki.shindo.management.mods.impl.ChatTranslateMod.Language;
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

        Language language = ChatTranslateMod.getInstance().getLanguage();

        switch (language) {
            case JAPANESE:
                to = Translator.JAPANESE;
                break;
            case ENGLISH:
                to = Translator.ENGLISH;
                break;
            case CHINESE:
                to = Translator.CHINESE_SIMPLIFIED;
                break;
            case POLISH:
                to = Translator.POLISH;
                break;
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
