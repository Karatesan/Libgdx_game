package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.economy.LootDropComponent;
import com.karatesan.game.ecs.components.event.DeathEventComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.PendingRemovalComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.utility.PausableSystem;

/**
 * post death system - what happens with corpse
 */
public class OnDeathSystem extends IteratingSystem implements PausableSystem {

    private final EntityFactory entityFactory;
    private final GameContext gameContext;
//TODO disjon dropping xp and killing entity - as this prevents player from entering here
    public OnDeathSystem(EntityFactory entityFactory, GameContext context) {
        super(Family.all(DeathEventComponent.class, TransformComponent.class, LootDropComponent.class).get());
        this.entityFactory = entityFactory;
        this.gameContext = context;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = Mappers.transform.get(entity);

        //handle score update
        SessionComponent sessionComponent = Mappers.session.get(gameContext.getSession());
        sessionComponent.kilLCount++;
        // If it's an enemy, drop XP!
        if (Mappers.enemy.has(entity)) {
            LootDropComponent lootDrop = Mappers.lootDrop.get(entity);
            entityFactory.createXpDrop(transform.x, transform.y, lootDrop.xpValue);
        }

        entity.add(getEngine().createComponent(PendingRemovalComponent.class));
        entity.remove(DeathEventComponent.class);
    }
}
