package com.karatesan.game.ecs.logic.combat.continuation;

import com.karatesan.game.ecs.logic.combat.projectile.ProjectileImpactContext;

/*
ProjectileContinuationRule:
    returns true if projectile survived/continued
    returns false if this rule did nothing
 */
public interface ProjectileContinuationRule {
    boolean tryContinue(ProjectileImpactContext context);
}
