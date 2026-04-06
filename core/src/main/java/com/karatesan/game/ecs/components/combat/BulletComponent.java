package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class BulletComponent implements Component, Poolable {
    public float range = 1000f;
    public float distanceTravelled = 0;

    @Override
    public void reset() {
        distanceTravelled = 0;
    }
}
