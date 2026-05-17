package com.karatesan.game.ecs.components.combat.projectile;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Pool;

public class ProjectileHitHistoryComponent implements Component, Pool.Poolable {
    public final ObjectSet<Entity> hitTargets = new ObjectSet<>();

    @Override
    public void reset() {
        hitTargets.clear();
    }
}
