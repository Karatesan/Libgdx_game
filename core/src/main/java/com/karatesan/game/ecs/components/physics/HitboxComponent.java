package com.karatesan.game.ecs.components.physics;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class HitboxComponent implements Component, Poolable {
    public float radius = 10f;

    @Override
    public void reset() {
        radius = 10f;
    }
}
