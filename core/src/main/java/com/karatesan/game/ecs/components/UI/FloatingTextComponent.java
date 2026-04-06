package com.karatesan.game.ecs.components.UI;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool.Poolable;

public class FloatingTextComponent implements Component, Poolable {
    public String text;
    public Color color;
    public Entity anchorEntity;
    public float offsetY = 0f;


    @Override
    public void reset() {
        offsetY = 0f;
        anchorEntity = null;
        text = null;
    }
}
