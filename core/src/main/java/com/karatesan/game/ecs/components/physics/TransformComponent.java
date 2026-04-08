package com.karatesan.game.ecs.components.physics;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class TransformComponent implements Component, Poolable {
    public float x;
    public float y;
    public int z;
    public float rotation;
    public float size;

    @Override
    public void reset() {
        x = 0;
        y = 0;
        rotation = 0;
        size = 0;
        z = 0;
    }
}
