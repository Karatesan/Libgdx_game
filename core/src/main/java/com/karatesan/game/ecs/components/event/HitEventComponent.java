package com.karatesan.game.ecs.components.event;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

    public class HitEventComponent implements Component, Pool.Poolable {
        public Entity targetEntity; // The enemy we hit
        public Entity bullet;
        public float rawDamage;
        public float finalDamage;
        public boolean isCrit;
        public boolean isDodged;
        public float eventX;
        public float eventY;

    @Override
    public void reset() {
        targetEntity = null;
        bullet = null;
        isDodged = false;
        isCrit = false;
        eventX = 0;
        eventY = 0;
        rawDamage = 0;
        finalDamage = 0;
    }
}
