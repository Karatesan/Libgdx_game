package com.karatesan.game.ecs.components.stats;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class OffensiveStatsComponent implements Component, Pool.Poolable {
    public float critChance = 0.1f;
    public float critMultiplier = 2.0f;
    public float damageMultiplier = 1.0f;
    @Override
    public void reset() {
        critChance = 0.1f;
        critMultiplier = 1f;
        damageMultiplier = 1f;
    }
}
