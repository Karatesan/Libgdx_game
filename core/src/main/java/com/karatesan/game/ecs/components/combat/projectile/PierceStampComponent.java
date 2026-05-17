package com.karatesan.game.ecs.components.combat.projectile;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class PierceStampComponent implements Component, Pool.Poolable {
    public int remaining;
    public float damageRetention;

    @Override
    public void reset() {
        remaining = 0;
        damageRetention = 0;
    }
}
