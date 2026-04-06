package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class StatsComponent implements Component, Poolable {
    public float critChance = .1f;
    public float critMultiplier = 2.f;
    public float damageMultiplier = 1f;

    @Override
    public void reset() {
        critChance = .01f;
        critMultiplier = 2f;
        damageMultiplier = 1f;
    }
}
