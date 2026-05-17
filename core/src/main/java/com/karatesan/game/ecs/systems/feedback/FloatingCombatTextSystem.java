package com.karatesan.game.ecs.systems.feedback;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.combat.hit.HitEventComponent;
import com.karatesan.game.ecs.components.combat.hit.HitSourceType;
import com.karatesan.game.ecs.components.combat.hit.ResolvedHitComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.factory.FloatingTextStyle;

public class FloatingCombatTextSystem extends IteratingSystem {

    private final EntityFactory factory;

    public FloatingCombatTextSystem(EntityFactory factory) {
        super(Family.all(HitEventComponent.class, ResolvedHitComponent.class).get());
        this.factory = factory;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        HitEventComponent hit = Mappers.hitEvent.get(entity);

        switch (hit.outcome) {
            case DODGED:
                factory.createFloatingText(hit.x, hit.y, "DODGE", FloatingTextStyle.DODGE);
                break;
            case IMMUNE:
                factory.createFloatingText(hit.x, hit.y, "IMMUNE", FloatingTextStyle.ARMORED);
                break;
            case KILLED:
            case DAMAGED:
                if (hit.crit) {
                    factory.createDamageText(hit.x, hit.y, hit.finalDamage, FloatingTextStyle.CRIT);
                } else if (hit.finalDamage < hit.rawDamage) {
                    factory.createDamageText(hit.x, hit.y, hit.finalDamage, FloatingTextStyle.ARMORED);
                } else {
                    factory.createDamageText(hit.x, hit.y, hit.finalDamage, hit.sourceType.equals(
                        HitSourceType.EXPLOSION) ? FloatingTextStyle.EXPLOSION : FloatingTextStyle.DAMAGE);
                }
                break;
            default:
                break;
        }
    }
}
