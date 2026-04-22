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
    public Entity lastHit;
    public int pierceCount;
    public int ricochetCount;

    @Override
    public void reset() {
        distanceTravelled = 0;
        startX = 0;
        startY = 0;

        ricochetChance = 0;
        lastHit = null;
        pierceCount = 0;
    }
}
