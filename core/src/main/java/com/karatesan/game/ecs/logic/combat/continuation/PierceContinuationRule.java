package com.karatesan.game.ecs.logic.combat.continuation;

import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.projectile.PierceStampComponent;
import com.karatesan.game.ecs.logic.combat.projectile.ProjectileImpactContext;

public final class PierceContinuationRule implements ProjectileContinuationRule {

    @Override
    public boolean tryContinue(ProjectileImpactContext context) {
        PierceStampComponent pierce = Mappers.pierce.get(context.bullet);

        if (pierce == null || pierce.remaining <= 0) {
            return false;
        }
        pierce.remaining--;
        context.payload.currentDamage *= pierce.damageRetention;

        return true;
    }
}
