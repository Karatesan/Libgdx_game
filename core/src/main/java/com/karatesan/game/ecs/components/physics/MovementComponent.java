package com.karatesan.game.ecs.components.physics;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class MovementComponent implements Component, Poolable {
    public float maxSpeed = 0f;

    @Override
    public void reset() {
        maxSpeed = 0f;
    }
}
