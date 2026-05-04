package com.karatesan.game.ecs.components.weapon;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class ProjectileDistanceTravelledComponent implements Component, Pool.Poolable {
    public float distanceTravelled = 0;

    @Override
    public void reset() {
        distanceTravelled = 0;
    }
}
