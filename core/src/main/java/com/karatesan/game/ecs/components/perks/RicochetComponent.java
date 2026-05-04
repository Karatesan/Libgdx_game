package com.karatesan.game.ecs.components.perks;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

public class RicochetComponent implements Component, Pool.Poolable {
    public float ricochetChance;
    public int ricochetCount;
    public float ricochetDamageRetention;
    public Entity recentEnemyHit;

    @Override
    public void reset() {
        ricochetChance = 0;
        ricochetCount = 0;
        ricochetDamageRetention = 0;
        recentEnemyHit = null;
    }
}
