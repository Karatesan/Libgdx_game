package com.karatesan.game.ecs.components.event;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

    public class HitEventComponent implements Component, Pool.Poolable {
        public Entity targetEntity; // The enemy we hit
        public Entity bullet;
        public float damage;
        public boolean isCrit;
        public float eventX;
        public float eventY;

    @Override
    public void reset() {
        targetEntity = null;
        bullet = null;
        eventX = 0;
        eventY = 0;
    }
}
