package com.karatesan.game.ecs.components.physics;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class VelocityComponent implements Component, Poolable {
    public float x;
    public float y;


    @Override
    public void reset() {
        x = 0;
        y = 0;
    }
}
