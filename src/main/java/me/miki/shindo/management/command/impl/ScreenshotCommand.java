package me.miki.shindo.management.command.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.command.Command;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.utils.transferable.FileTransferable;
import net.minecraft.util.ChatComponentText;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ScreenshotCommand extends Command {

    public ScreenshotCommand() {
        super("screenshot");
    }

    @Override
    public void onCommand(String message) {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        String[] args = message.split(" ");
        File file = new File(fileManager.getScreenshotDir(), args[1]);

        if (args.length < 1) {
            return;
        }

        if (args[0].equals("open")) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (args[0].equals("copy")) {
            FileTransferable selection = new FileTransferable(file);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }

        if (args[0].equals("del")) {
            file.delete();
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(args[1] + " has been deleted"));
        }
    }
}
