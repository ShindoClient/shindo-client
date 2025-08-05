package me.miki.shindo.gui.modmenu.category.impl.game.scenes.score;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.category.impl.GamesCategory;
import me.miki.shindo.gui.modmenu.category.impl.game.GameScene;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class ScoreScene extends GameScene {
    private final Map<String, List<Integer>> scores = new TreeMap<>();
    private final File scoreFile = new File(Shindo.getInstance().getFileManager().getGamesDir(), "scores.json");

    public ScoreScene(GamesCategory parent) {
        super(parent, "Scores", "See your records", LegacyIcon.STAR_FILL);
    }

    @Override
    public void initGui() {
        scores.clear();
        if (!scoreFile.exists()) return;

        try (FileReader reader = new FileReader(scoreFile)) {
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            for (String game : json.keySet()) {
                JsonArray array = json.getAsJsonArray(game);
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    list.add(array.get(i).getAsInt());
                }
                list.sort(Comparator.comparingInt(i -> game.equals("ClickyCat") ? i : -i));
                if (list.size() > 3) list = list.subList(0, 3);
                scores.put(game, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        ColorManager colorManager = Shindo.getInstance().getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accentColor = colorManager.getCurrentColor();

        nvg.save();
        nvg.scissor(getX(), getY(), getWidth(), getHeight());

        drawBackground(nvg, palette);

        int columns = 3;
        int cardWidth = (getWidth() - (columns + 1) * 10) / columns;
        int cardHeight = 70;
        int lineHeight = 12;

        int index = 0;
        for (Map.Entry<String, List<Integer>> entry : scores.entrySet()) {
            int col = index % columns;
            int row = index / columns;

            int x = getX() + 10 + col * (cardWidth + 10);
            int y = getY() + 20 + row * (cardHeight + 10);

            // card background
            nvg.drawRoundedRect(x, y, cardWidth, cardHeight, 6, palette.getBackgroundColor(ColorType.DARK));
            nvg.drawOutlineRoundedRect(x, y, cardWidth, cardHeight, 6, 1.2f, palette.getFontColor(ColorType.DARK));

            String game = entry.getKey();
            List<Integer> values = entry.getValue();

            // title
            nvg.drawText(game, x + 8, y + 14, palette.getFontColor(ColorType.NORMAL), 11, Fonts.SEMIBOLD);

            // background for score lines
            int scoreBoxY = y + 26;
            //int scoreBoxHeight = values.size() * lineHeight + 6;


            // scores
            for (int i = 0; i < values.size(); i++) {
                String label = game.equals("ClickyCat") ? values.get(i) + " ms" : String.valueOf(values.get(i));
                String text = (i + 1) + ". " + label;
                nvg.drawRoundedRect(x + 10, scoreBoxY + 2 + i * lineHeight, 90, nvg.getTextHeight(text, 9.5f, Fonts.REGULAR) + 2, 4, accentColor.getColor1());
                nvg.drawText(text, x + 12, scoreBoxY + 4 + i * lineHeight, palette.getFontColor(ColorType.NORMAL), 9.5f, Fonts.REGULAR);

            }

            index++;
        }

        if (scores.isEmpty()) {
            nvg.drawText("Nenhuma pontuação salva.", 10, 20, palette.getFontColor(ColorType.NORMAL), 10, Fonts.REGULAR);
        }

        nvg.restore();
        nvg.drawOutlineRoundedRect(getX(), getY(), getWidth(), getHeight(), 10, 8, palette.getBackgroundColor(ColorType.NORMAL));
    }
}