package me.miki.shindo.gui.modmenu.category.impl.game.scenes;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.category.impl.GamesCategory;
import me.miki.shindo.gui.modmenu.category.impl.game.GameScene;
import me.miki.shindo.gui.modmenu.category.impl.game.scenes.score.ScoreSaver;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class PongScene extends GameScene {
    private static final float PADDLE_HEIGHT = 50, PADDLE_WIDTH = 5, BALL_SIZE = 5;
    private float ballX, ballY, ballVX = 2, ballVY = 2;
    private float paddleY, enemyY;
    private boolean gameOver = false;
    private int score = 0;

    public PongScene(GamesCategory parent) {
        super(parent, "Pong", "Hit the ball, don't miss!", LegacyIcon.PLAY);
    }

    @Override
    public void initGui() {
        ballX = getWidth() / 2f;
        ballY = getHeight() / 2f;
        paddleY = enemyY = getHeight() / 2f - PADDLE_HEIGHT / 2;
        ballVX = 2;
        ballVY = 2;
        gameOver = false;
        score = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        ColorPalette palette = Shindo.getInstance().getColorManager().getPalette();

        nvg.save();
        nvg.scissor(getX(), getY(), getWidth(), getHeight());

        drawBackground(nvg, palette);
        if (!gameOver) {
            ballX += ballVX;
            ballY += ballVY;

            if (ballY <= 0 || ballY >= getHeight() - BALL_SIZE) ballVY *= -1;

            if (ballY < enemyY + PADDLE_HEIGHT / 2) enemyY -= 1.5;
            if (ballY > enemyY + PADDLE_HEIGHT / 2) enemyY += 1.5;

            if (ballX <= 10 && ballY + BALL_SIZE > paddleY && ballY < paddleY + PADDLE_HEIGHT) {
                ballVX *= -1;
                score++;
            }

            if (ballX >= getWidth() - 10 && ballY + BALL_SIZE > enemyY && ballY < enemyY + PADDLE_HEIGHT) {
                ballVX *= -1;
            }

            if (ballX < -BALL_SIZE || ballX > getWidth() + BALL_SIZE) {
                gameOver = true;
                ScoreSaver.saveScore("Pong", score);
            }
        }

        nvg.drawRect(getX() + 5, getY() + paddleY, PADDLE_WIDTH, PADDLE_HEIGHT, palette.getFontColor(ColorType.DARK));
        nvg.drawRect(getX() + getWidth() - 10, getY() + enemyY, PADDLE_WIDTH, PADDLE_HEIGHT, palette.getFontColor(ColorType.DARK));
        nvg.drawRect(getX() + ballX, getY() + ballY, BALL_SIZE, BALL_SIZE, palette.getFontColor(ColorType.NORMAL));

        if (gameOver) {
            nvg.drawCenteredText("Game Over", getX() + getWidth() / 2, getY() + getHeight() / 2 - 10, Color.RED, 12, Fonts.SEMIBOLD);
            nvg.drawCenteredText("Score: " + score, getX() + getWidth() / 2, getY() + getHeight() / 2 + 5, palette.getFontColor(ColorType.NORMAL), 10, Fonts.MEDIUM);
        }

        // limitar paddle do jogador à tela
        if (paddleY < 0) paddleY = 0;
        if (paddleY > getHeight() - PADDLE_HEIGHT) paddleY = getHeight() - PADDLE_HEIGHT;

        // limitar paddle da IA à tela
        if (enemyY < 0) enemyY = 0;
        if (enemyY > getHeight() - PADDLE_HEIGHT) enemyY = getHeight() - PADDLE_HEIGHT;

        nvg.restore();
        nvg.drawOutlineRoundedRect(getX(), getY(), getWidth(), getHeight(), 10, 8, palette.getBackgroundColor(ColorType.NORMAL));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (gameOver && keyCode == Keyboard.KEY_SPACE) {
            initGui();
            return;
        }

        if (keyCode == Keyboard.KEY_UP) paddleY -= 10;
        if (keyCode == Keyboard.KEY_DOWN) paddleY += 10;
    }
}