package me.miki.shindo.gui.modmenu.category.impl.game.scenes;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.category.impl.GamesCategory;
import me.miki.shindo.gui.modmenu.category.impl.game.GameScene;
import me.miki.shindo.gui.modmenu.category.impl.game.scenes.score.ScoreSaver;
import me.miki.shindo.gui.modmenu.category.impl.game.util.DeltaTime;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public class SnakeScene extends GameScene {
    private static final int TILE_SIZE = 10;
    private static final float MOVE_INTERVAL = 0.15f;
    private final LinkedList<Point> snake = new LinkedList<>();
    private final Random random = new Random();
    private Point food;
    private int direction = Keyboard.KEY_RIGHT;
    private boolean gameOver = false;
    private int score = 0;
    private float moveTimer = 0f;

    public SnakeScene(GamesCategory parent) {
        super(parent, "Snake", "Eat apples and don't die!", LegacyIcon.PLAY);
    }

    @Override
    public void initGui() {
        snake.clear();
        snake.add(new Point(5 * TILE_SIZE, 5 * TILE_SIZE));
        direction = Keyboard.KEY_RIGHT;
        spawnFood();
        gameOver = false;
        score = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        DeltaTime.getInstance().update();
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        ColorManager cm = Shindo.getInstance().getColorManager();
        ColorPalette palette = cm.getPalette();
        float delta = DeltaTime.getInstance().getDeltaTime();

        nvg.save();
        nvg.scissor(getX(), getY(), getWidth(), getHeight());

        drawBackground(nvg, palette);

        if (!gameOver) {
            moveTimer += delta;
            if (moveTimer >= MOVE_INTERVAL) {
                move();
                moveTimer = 0f;
            }
        }

        if (food != null) {
            nvg.drawRoundedRect(getX() + food.x, getY() + food.y, TILE_SIZE, TILE_SIZE, 2, cm.getCurrentColor().getColor1());
        }

        for (Point p : snake) {
            nvg.drawRoundedRect(getX() + p.x, getY() + p.y, TILE_SIZE, TILE_SIZE, 2, cm.getCurrentColor().getColor2());
        }

        if (gameOver) {
            nvg.drawCenteredText("Game Over", getX() + getWidth() / 2F, getY() + getHeight() / 2F - 10, Color.RED, 12, Fonts.SEMIBOLD);
            nvg.drawCenteredText("Score: " + score, getX() + getWidth() / 2F, getY() + getHeight() / 2F + 5, palette.getFontColor(ColorType.NORMAL), 10, Fonts.MEDIUM);
        }

        nvg.restore();
        nvg.drawOutlineRoundedRect(getX(), getY(), getWidth(), getHeight(), 10, 8, palette.getBackgroundColor(ColorType.NORMAL));
    }

    private void move() {
        Point head = new Point(snake.getFirst());
        switch (direction) {
            case Keyboard.KEY_UP:
                head.translate(0, -TILE_SIZE);
                break;
            case Keyboard.KEY_DOWN:
                head.translate(0, TILE_SIZE);
                break;
            case Keyboard.KEY_LEFT:
                head.translate(-TILE_SIZE, 0);
                break;
            case Keyboard.KEY_RIGHT:
                head.translate(TILE_SIZE, 0);
                break;
        }

        // check collision
        if (head.x < 0 || head.y < 0 || head.x >= getWidth() || head.y >= getHeight() || snake.contains(head)) {
            gameOver = true;
            ScoreSaver.saveScore("Snake", score);
            return;
        }

        snake.addFirst(head);

        if (head.equals(food)) {
            score++;
            spawnFood();
        } else {
            snake.removeLast();
        }
    }

    private void spawnFood() {
        int cols = getWidth() / TILE_SIZE;
        int rows = getHeight() / TILE_SIZE;
        Point p;
        do {
            p = new Point(random.nextInt(cols) * TILE_SIZE, random.nextInt(rows) * TILE_SIZE);
        } while (snake.contains(p));
        food = p;
    }


    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (gameOver && keyCode == Keyboard.KEY_SPACE) {
            initGui();
            return;
        }

        // prevent reverse movement
        switch (keyCode) {
            case Keyboard.KEY_UP:
                if (direction != Keyboard.KEY_DOWN) direction = keyCode;
                break;
            case Keyboard.KEY_DOWN:
                if (direction != Keyboard.KEY_UP) direction = keyCode;
                break;
            case Keyboard.KEY_LEFT:
                if (direction != Keyboard.KEY_RIGHT) direction = keyCode;
                break;
            case Keyboard.KEY_RIGHT:
                if (direction != Keyboard.KEY_LEFT) direction = keyCode;
                break;
        }
    }
}
