package com.karatesan.game.ecs.logic.combat.continuation;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.projectile.RicochetStampComponent;
import com.karatesan.game.ecs.logic.combat.projectile.ProjectileImpactContext;
import com.karatesan.game.ecs.logic.combat.projectile.ProjectileMotionHelper;
import com.karatesan.game.ecs.logic.combat.projectile.ProjectileTargetingHelper;

public final class RicochetContinuationRule implements ProjectileContinuationRule {

    private final ProjectileTargetingHelper targetingHelper;

    public RicochetContinuationRule(ProjectileTargetingHelper targetingHelper) {
        this.targetingHelper = targetingHelper;
    }

    @Override
    public boolean tryContinue(ProjectileImpactContext context) {
        RicochetStampComponent ricochet = Mappers.ricochet.get(context.bullet);

        if (ricochet == null || ricochet.remaining <= 0 || ricochet.chance <= 0f) {
            return false;
        }

        if (ricochet.chance < 1f && MathUtils.random() >= ricochet.chance) {
            return false;
        }

        Entity nextTarget = targetingHelper.findNearestValidTarget(
            context.bullet,
            context.target);

        if (nextTarget == null) {
            return false;
        }

        if (!ProjectileMotionHelper.redirectToward(context.bullet, nextTarget)) {
            return false;
        }

        ricochet.remaining--;

        context.payload.currentDamage *= ricochet.damageRetention;

        return true;
    }
}
