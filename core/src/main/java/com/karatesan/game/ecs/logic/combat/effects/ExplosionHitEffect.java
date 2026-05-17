package com.karatesan.game.ecs.logic.combat.effects;

import com.badlogic.gdx.math.MathUtils;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.hit.HitOutcome;
import com.karatesan.game.ecs.components.combat.projectile.ExplosionStampComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.logic.combat.projectile.ProjectileImpactContext;

public class ExplosionHitEffect implements ProjectileHitEffect {

    private final EntityFactory factory;

    public ExplosionHitEffect(EntityFactory factory) {
        this.factory = factory;
    }

    @Override
    public void apply(ProjectileImpactContext context) {

        if (context.hit.outcome != HitOutcome.DAMAGED && context.hit.outcome != HitOutcome.KILLED) {
            return;
        }

        ExplosionStampComponent explosionStampComponent = Mappers.explosionStamp.get(context.bullet);

        if (explosionStampComponent != null && MathUtils.random() < explosionStampComponent.explosionChance) {

            TransformComponent transform = Mappers.transform.get(context.target);
            factory.createExplosion(transform.x, transform.y,
                context.payload.currentDamage * explosionStampComponent.explosionDamageRatio, context.payload.owner,
                context.payload.critChance, context.payload.critMultiplier);
        }
    }
}
