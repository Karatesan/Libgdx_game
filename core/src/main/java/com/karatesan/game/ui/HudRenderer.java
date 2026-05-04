package com.karatesan.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.CharArray;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class HudRenderer {

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final ShapeDrawer shapeDrawer;
    private final CharArray UIText = new CharArray();

    public HudRenderer(SpriteBatch batch, BitmapFont font, ShapeDrawer shapeDrawer) {
        this.batch = batch;
        this.font = font;
        this.shapeDrawer = shapeDrawer;
    }

    public void drawHP(float currentHp, float maxHp, float screenW, float screenH) {
        UIText.clear();
        UIText.append("HP: ").append((int) currentHp).append("/").append((int) maxHp);
        font.getData().setScale(.5f);

        // Anchored to Top-Left (20 pixels from left edge, 20 pixels from top edge)
        font.draw(batch, UIText, 20f, screenH - 20f);
        font.getData().setScale(1f);
    }


    public void drawXpBar(float currentXp, float xpToNextLevel, float screenW, float screenH) {
        float barHeight = 5f;
        // Center the bar horizontally
        float barX = 0;
        // Place it 50 pixels down from the top edge
        float barY = screenH - barHeight;

        float fillRatio = currentXp / xpToNextLevel;

        shapeDrawer.setColor(Color.DARK_GRAY);
        shapeDrawer.filledRectangle(barX, barY, screenW, barHeight);
        shapeDrawer.setColor(Color.CYAN);
        shapeDrawer.filledRectangle(barX, barY, screenW * fillRatio, barHeight);
    }

    public void drawGameOver(float screenW, float screenH) {
        font.setColor(Color.RED);
        font.draw(batch, "GAME OVER", (screenW / 2f) - 60f, (screenH / 2f) + 50f);
        font.setColor(Color.WHITE);
        font.draw(batch, "Press 'R' to Restart", (screenW / 2f) - 160f, (screenH / 2f));
    }

    public void drawWaveAnnouncement(int currentWave, float screenW, float screenH) {
        UIText.clear();
        UIText.append("WAVE ").append(currentWave);

        font.setColor(Color.YELLOW);
        // Center it perfectly in the middle of the screen
        font.draw(batch, UIText, (screenW / 2f) - 60f, (screenH / 2f) + 50f);

        font.setColor(Color.WHITE);
    }

    public void drawTimer(float timeSurvived, float screenW, float screenH) {
        int totalSeconds = (int) timeSurvived;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        UIText.clear();
        UIText.append(minutes).append(":");
        if (seconds < 10) UIText.append("0"); // Add leading zero (e.g., 1:05)
        UIText.append(seconds);
        font.getData().setScale(.5f); // Make it big!

        // screenW / 2 finds the exact dead center of the screen
        font.draw(batch, UIText, (screenW / 2f) - 20f, screenH - 20f);
        font.getData().setScale(1f); // Make it big!
    }

    public void drawKillCounter(int kilLCount, float screenW, float screenH) {
        UIText.clear();
        UIText.append("Killed: ").append(kilLCount);
        font.getData().setScale(.5f);
        // screenW - 150 pixels from the right edge, screenH - 20 pixels from the top
        font.draw(batch, UIText, screenW - 150f, screenH - 20f);
        font.getData().setScale(1f);
    }

    public void dimBackground(float screenW, float screenH) {
        batch.begin();
        shapeDrawer.setColor(0, 0, 0, 0.7f);
        shapeDrawer.filledRectangle(0, 0, screenW, screenH);
        batch.end();
    }
}
