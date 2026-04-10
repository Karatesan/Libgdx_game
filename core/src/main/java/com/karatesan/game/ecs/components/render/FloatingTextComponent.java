package com.karatesan.game.ecs.components.render;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool.Poolable;

public class FloatingTextComponent implements Component, Poolable {
    public int damageValue;
    public Color color;
    public float scale = 1f; // ADD THIS

    @Override
    public void reset() {
        damageValue = 0;
        color = null;
        scale = 1f; // Reset it!
    }
}
