package com.karatesan.game.ecs.components.event;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

public class HitEventComponent implements Component, Pool.Poolable {
    public Entity targetEntity; // The enemy we hit

    @Override
    public void reset() {
        targetEntity = null;
    }
}
