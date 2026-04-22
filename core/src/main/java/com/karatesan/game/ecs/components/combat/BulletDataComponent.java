package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

public class BulletDataComponent implements Component, Pool.Poolable {
    public float ricochetChance = 0f;
    public int pierceCount = 0;
    // future: explosionRadius, chainCount, knockback, etc.

    @Override
    public void reset() {
        ricochetChance = 0f;
        pierceCount = 0;
    }
}
