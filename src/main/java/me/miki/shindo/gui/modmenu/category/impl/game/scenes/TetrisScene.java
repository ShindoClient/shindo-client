package me.miki.shindo.gui.modmenu.category.impl.game.scenes;

import me.miki.shindo.gui.modmenu.category.impl.GamesCategory;
import me.miki.shindo.gui.modmenu.category.impl.game.GameScene;
import me.miki.shindo.gui.modmenu.category.impl.game.scenes.score.ScoreSaver;
import me.miki.shindo.gui.modmenu.category.impl.game.util.DeltaTime;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.Shindo;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Random;

public class TetrisScene extends GameScene {
    private static final int TILE_SIZE = 10;
    private static final int ROWS = 20;
    private static final int COLS = 10;
    private static final float FALL_INTERVAL = 0.5f;

    private int[][] board = new int[ROWS][COLS];
    private Tetromino current;
    private float fallTimer = 0f;
    private boolean gameOver = false;
    private int score = 0;

    private final Random random = new Random();

    public TetrisScene(GamesCategory parent) {
        super(parent, "Tetris", "Fit the falling blocks", LegacyIcon.PLAY);
    }

    @Override
    public void initGui() {
        board = new int[ROWS][COLS];
        score = 0;
        gameOver = false;
        spawnTetromino();
        fallTimer = 0f;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        DeltaTime.getInstance().update();
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        ColorPalette palette = Shindo.getInstance().getColorManager().getPalette();

        int fieldWidth = COLS * TILE_SIZE;
        int fieldHeight = ROWS * TILE_SIZE;
        int xOffset = (getWidth() - fieldWidth) / 2;
        int yOffset = (getHeight() - fieldHeight) / 2;

        nvg.save();
        nvg.scissor(getX(), getY(), getWidth(), getHeight());
        //nvg.translate(getX() + xOffset, getY() + yOffset);

        drawBackground(nvg, palette);
        // Movimento contínuo ao segurar teclas
        if (!gameOver) {
            // Substituir no TetrisScene.java

            fallTimer += DeltaTime.getInstance().getDeltaTime();
            if (fallTimer >= FALL_INTERVAL) {
                fallTimer = 0f;
                if (!move(0, 1)) {
                    mergeToBoard();
                    clearLines();
                    spawnTetromino();
                }
            }
        }

        drawBoard(nvg, palette, getX() + xOffset, getY() + yOffset);
        drawCurrentPiece(nvg, palette, getX() + xOffset, getY() + yOffset);

        if (gameOver) {
            nvg.drawCenteredText("Game Over", getX() + xOffset + fieldWidth / 2F, getY() + yOffset + fieldHeight / 2F - 10, Color.RED, 12, Fonts.SEMIBOLD);
            nvg.drawCenteredText("Score: " + score, getX() + xOffset + fieldWidth / 2F, getY() + yOffset + fieldHeight / 2F + 5, palette.getFontColor(ColorType.NORMAL), 10, Fonts.MEDIUM);
        }

        // desenha contorno da área de jogo
        nvg.drawOutlineRoundedRect(getX() + xOffset, getY() + yOffset,  fieldWidth, fieldHeight, 2, 1.5f, palette.getFontColor(ColorType.DARK));

        nvg.restore();
        nvg.drawOutlineRoundedRect(getX(), getY(), getWidth(), getHeight(), 10, 8, palette.getBackgroundColor(ColorType.NORMAL));
    }

    private void drawBoard(NanoVGManager nvg, ColorPalette palette, int xOffset, int yOffset) {
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (board[y][x] != 0) {
                    nvg.drawRect(xOffset + x * TILE_SIZE, yOffset + y * TILE_SIZE, TILE_SIZE, TILE_SIZE, palette.getFontColor(ColorType.DARK));
                }
            }
        }
    }

    private void drawCurrentPiece(NanoVGManager nvg, ColorPalette palette, int xOffset, int yOffset) {
        if (current == null) return;

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (current.shape[y][x] != 0) {
                    int drawX = xOffset + (current.x + x) * TILE_SIZE;
                    int drawY = yOffset + (current.y + y) * TILE_SIZE;
                    nvg.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE, palette.getFontColor(ColorType.NORMAL));
                }
            }
        }
    }

    private void spawnTetromino() {
        current = Tetromino.random(random);
        if (!canMove(current, current.x, current.y)) {
            gameOver = true;
            ScoreSaver.saveScore("Tetris", score);
        }
    }

    private void rotate() {
        int[][] rotated = new int[4][4];
        for (int y = 0; y < 4; y++)
            for (int x = 0; x < 4; x++)
                rotated[x][3 - y] = current.shape[y][x];

        Tetromino rotatedTetromino = new Tetromino(rotated);
        rotatedTetromino.x = current.x;
        rotatedTetromino.y = current.y;

        if (canMove(rotatedTetromino, rotatedTetromino.x, rotatedTetromino.y)) {
            current.shape = rotated;
        }
    }

    private boolean move(int dx, int dy) {
        if (canMove(current, current.x + dx, current.y + dy)) {
            current.x += dx;
            current.y += dy;
            return true;
        }
        return false;
    }

    private boolean canMove(Tetromino tet, int newX, int newY) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (tet.shape[y][x] != 0) {
                    int tx = newX + x;
                    int ty = newY + y;
                    if (tx < 0 || tx >= COLS || ty < 0 || ty >= ROWS || board[ty][tx] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void mergeToBoard() {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (current.shape[y][x] != 0) {
                    int bx = current.x + x;
                    int by = current.y + y;
                    if (bx >= 0 && bx < COLS && by >= 0 && by < ROWS) {
                        board[by][bx] = 1;
                    }
                }
            }
        }
    }

    private void clearLines() {
        for (int y = ROWS - 1; y >= 0; y--) {
            boolean full = true;
            for (int x = 0; x < COLS; x++) {
                if (board[y][x] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                score += 100;
                for (int i = y; i > 0; i--) {
                    board[i] = board[i - 1].clone();
                }
                board[0] = new int[COLS];
                y++; // recheck this line
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (gameOver && keyCode == Keyboard.KEY_SPACE) {
            initGui();
            return;
        }
        if (current == null) return;

        switch (keyCode) {
            case Keyboard.KEY_LEFT:
                move(-1, 0);
                break;
            case Keyboard.KEY_RIGHT:
                move(1, 0);
                break;
            case Keyboard.KEY_DOWN:
                move(0, 1);
                break;
            case Keyboard.KEY_UP:
                rotate();
                break;
        }
    }


    private static class Tetromino {
        public int[][] shape;
        public int x = 3, y = 0;

        public Tetromino(int[][] shape) {
            this.shape = shape;
        }

        public static Tetromino random(Random random) {
            int[][][] shapes = {
                    {{1,1,1,1}},                      // I
                    {{1,1},{1,1}},                    // O
                    {{0,1,0},{1,1,1}},                // T
                    {{1,1,0},{0,1,1}},                // S
                    {{0,1,1},{1,1,0}},                // Z
                    {{1,0,0},{1,1,1}},                // J
                    {{0,0,1},{1,1,1}}                 // L
            };
            int[][] shape = shapes[random.nextInt(shapes.length)];
            Tetromino t = new Tetromino(new int[4][4]);
            for (int y = 0; y < shape.length; y++)
                System.arraycopy(shape[y], 0, t.shape[y], 0, shape[y].length);
            return t;
        }
    }
}