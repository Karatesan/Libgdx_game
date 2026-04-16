package com.karatesan.game.ecs.systems.combat;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.event.DeathEventComponent;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.tag.EnemyComponent;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.systems.core.PausableSystem;

//Handles enemy death
public class DeathSystem extends IteratingSystem implements PausableSystem {

    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);
    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<EnemyComponent> em = ComponentMapper.getFor(EnemyComponent.class);
    private final EntityFactory entityFactory;

    private Entity sessionEntity;

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        // Find the one and only Session Entity and cache it!
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
    }

    public DeathSystem(EntityFactory entityFactory) {
        super(Family.all(DeathEventComponent.class, TransformComponent.class).get());
        this.entityFactory = entityFactory;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = tm.get(entity);

        //handle score update
        SessionComponent sessionComponent = sm.get(sessionEntity);
        sessionComponent.kilLCount++;
        // If it's an enemy, drop XP!
        if (em.has(entity)) {
            EnemyComponent enemyComp = em.get(entity);
            entityFactory.createXpDrop(transform.x, transform.y, enemyComp.xpDropValue);
        }

        entity.add(getEngine().createComponent(DeadComponent.class));
        entity.remove(DeathEventComponent.class);
    }
}
