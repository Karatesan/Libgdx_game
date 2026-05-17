package com.karatesan.game.ecs.components.combat.projectile;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class ExplosionStampComponent implements Component, Pool.Poolable {

    public float explosionChance;
    public float explosionRadius;
    public float explosionDamageRatio;

    @Override
    public void reset() {
        explosionChance = 0;
        explosionRadius = 0;
        explosionDamageRatio = 0;

    }
}
