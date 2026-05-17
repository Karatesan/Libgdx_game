package com.karatesan.game.ecs.components.combat.projectile;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class RicochetStampComponent implements Component, Pool.Poolable {
    public float chance;
    public int remaining;
    public float damageRetention;

    @Override
    public void reset() {
        chance = 0;
        remaining = 0;
        damageRetention = 0;
    }
}
