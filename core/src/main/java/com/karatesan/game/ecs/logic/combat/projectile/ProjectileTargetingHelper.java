package com.karatesan.game.ecs.logic.combat.projectile;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.projectile.ProjectileHitHistoryComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;

public final class ProjectileTargetingHelper {

    private ImmutableArray<Entity> enemies;

    public void setEnemies(ImmutableArray<Entity> enemies) {
        this.enemies = enemies;
    }

    public Entity findNearestValidTarget(Entity bullet, Entity excludedTarget) {
        if (enemies == null || enemies.size() == 0) {
            return null;
        }

        TransformComponent bulletTransform = Mappers.transform.get(bullet);

        ProjectileHitHistoryComponent history = Mappers.hitHistory.get(bullet);

        Entity bestTarget = null;
        float bestDistance2 = Float.MAX_VALUE;

        for (int i = 0; i < enemies.size(); i++) {
            Entity candidate = enemies.get(i);

            if (candidate == excludedTarget) {
                continue;
            }

            if (Mappers.dead.has(candidate)) {
                continue;
            }

            if (history.hitTargets.contains(candidate)) {
                continue;
            }

            TransformComponent candidateTransform = Mappers.transform.get(candidate);

            float dx = candidateTransform.x - bulletTransform.x;
            float dy = candidateTransform.y - bulletTransform.y;
            float distance2 = dx * dx + dy * dy;

            if (distance2 < bestDistance2) {
                bestDistance2 = distance2;
                bestTarget = candidate;
            }
        }

        return bestTarget;
    }
}
