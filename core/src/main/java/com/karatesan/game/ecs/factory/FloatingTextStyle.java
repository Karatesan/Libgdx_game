package com.karatesan.game.ecs.factory;

import com.badlogic.gdx.graphics.Color;

public enum FloatingTextStyle {
    DAMAGE       (Color.WHITE, 0.75f, 0.8f),
    CRIT         (Color.ORANGE, 1.25f, 1.0f),
    ARMORED      (Color.GRAY, 0.75f, 0.8f),
    DODGE        (Color.LIGHT_GRAY, 0.75f, 0.8f),
    PLAYER_HIT   (Color.RED, 0.75f, 0.8f),
    HEAL         (Color.GREEN, 1.0f, 0.8f);

    public final Color color;
    public final float scale;
    public final float lifetime;

    FloatingTextStyle(Color color, float scale, float lifetime) {
        this.color = color;
        this.scale = scale;
        this.lifetime = lifetime;
    }
}
