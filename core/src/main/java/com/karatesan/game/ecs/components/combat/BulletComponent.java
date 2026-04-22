package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

public class BulletComponent implements Component, Poolable {
    public float range = 1000f;
    public float distanceTravelled = 0;

    public float startX = 0;
    public float startY = 0;

    //PERKS ---------------------------------------

    public float ricochetChance = 0f;
    public int ricochetCount;

    public Entity lastHit = null;
    public int pierceCount;

    @Override
    public void reset() {
        distanceTravelled = 0;
        startX = 0;
        startY = 0;

        ricochetChance = 0;
        ricochetCount = 0;
        lastHit = null;
        pierceCount = 0;
    }
}
