package com.karatesan.game.ecs.logic.combat.effects;

import com.karatesan.game.ecs.logic.combat.projectile.ProjectileImpactContext;

/*
ProjectileHitEffect:
    does side effects
    does not decide projectile life/death
 */
public interface ProjectileHitEffect {
    void apply(ProjectileImpactContext context);
}
