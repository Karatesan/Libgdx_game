package com.karatesan.game.ecs.components.event;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;
import com.karatesan.game.ecs.components.combat.hit.HitSourceType;

public class DeathEventComponent implements Component, Pool.Poolable {
    public Entity killer;
    public Entity source;
    public HitSourceType sourceType;

    public float x;
    public float y;

    @Override
    public void reset() {
        killer = null;
        source = null;
        sourceType = null;
        x = 0f;
        y = 0f;
    }
}
