package com.karatesan.game.ecs.components.combat.projectile;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class ExplosionComponent implements Component, Pool.Poolable {
    public boolean damageApplied;

    @Override
    public void reset() {
        damageApplied = false;
    }
}
