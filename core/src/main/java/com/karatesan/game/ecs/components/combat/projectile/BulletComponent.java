package com.karatesan.game.ecs.components.combat.projectile;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

public class BulletComponent implements Component, Poolable {
    public float range;
    public float startX = 0;
    public float startY = 0;
    public Entity lastHit;

    @Override
    public void reset() {
        startX = 0;
        startY = 0;
        lastHit = null;
    }
}
