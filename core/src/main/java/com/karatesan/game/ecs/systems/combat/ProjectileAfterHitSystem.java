package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.DamagePayloadComponent;
import com.karatesan.game.ecs.components.combat.hit.HitEventComponent;
import com.karatesan.game.ecs.components.combat.hit.HitOutcome;
import com.karatesan.game.ecs.components.combat.hit.HitSourceType;
import com.karatesan.game.ecs.components.combat.hit.ResolvedHitComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.PendingRemovalComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.logic.combat.continuation.PierceContinuationRule;
import com.karatesan.game.ecs.logic.combat.continuation.ProjectileContinuationRule;
import com.karatesan.game.ecs.logic.combat.continuation.RicochetContinuationRule;
import com.karatesan.game.ecs.logic.combat.effects.ExplosionHitEffect;
import com.karatesan.game.ecs.logic.combat.effects.ProjectileHitEffect;
import com.karatesan.game.ecs.logic.combat.projectile.ProjectileImpactContext;
import com.karatesan.game.ecs.logic.combat.projectile.ProjectileTargetingHelper;
import com.karatesan.game.ecs.utility.PausableSystem;

public final class ProjectileAfterHitSystem extends IteratingSystem implements PausableSystem {

    private final Array<ProjectileHitEffect> hitEffects = new Array<>();
    private final Array<ProjectileContinuationRule> continuationRules = new Array<>();
    private final ProjectileImpactContext context = new ProjectileImpactContext();

    private final ProjectileTargetingHelper targetingHelper = new ProjectileTargetingHelper();

    public ProjectileAfterHitSystem(EntityFactory factory) {
        super(Family.all(HitEventComponent.class, ResolvedHitComponent.class).get());
        hitEffects.add(new ExplosionHitEffect(factory));

        continuationRules.add(new RicochetContinuationRule(targetingHelper));
        continuationRules.add(new PierceContinuationRule());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);

        targetingHelper.setEnemies(engine.getEntitiesFor(
            Family.all(EnemyComponent.class, TransformComponent.class)
                .exclude(PendingRemovalComponent.class)
                .get()));
    }

    @Override
    protected void processEntity(Entity eventEntity, float deltaTime) {
        HitEventComponent hit = Mappers.hitEvent.get(eventEntity);

        if (hit.sourceType != HitSourceType.PROJECTILE) {
            return;
        }

        if (!shouldProcessProjectileAftermath(hit.outcome)) {
            return;
        }

        Entity bullet = hit.source;

        if (bullet == null) {
            throw new IllegalStateException("Projectile hit has null source bullet.");
        }

        if (Mappers.dead.has(bullet)) {
            return;
        }

        DamagePayloadComponent payload = Mappers.damage.get(bullet);
        if (payload == null) {
            throw new IllegalStateException("Projectile source has no DamagePayloadComponent.");
        }

        context.set(getEngine(), eventEntity, hit, bullet, payload);

        for (int i = 0; i < hitEffects.size; i++) {
            hitEffects.get(i).apply(context);
        }

        for (int i = 0; i < continuationRules.size; i++) {
            if (continuationRules.get(i).tryContinue(context)) {
                //bullet should continue to live, we skip killing it
                context.clear();
                return;
            }
        }

        killBullet(bullet);
        context.clear();
    }

    private boolean shouldProcessProjectileAftermath(HitOutcome outcome) {
        return switch (outcome) {
            case DAMAGED, KILLED, IMMUNE, DODGED -> true;
            default -> false;
        };
    }

    private void killBullet(Entity bullet) {
        if (!Mappers.dead.has(bullet)) {
            bullet.add(getEngine().createComponent(PendingRemovalComponent.class));
        }
    }
}
