package com.karatesan.game.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.components.event.DeathEventComponent;
import com.karatesan.game.ecs.components.SessionComponent;

public class ScoreSystem extends IteratingSystem {

    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);
    private Entity sessionEntity;

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        // Find the one and only Session Entity and cache it!
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
    }

    public ScoreSystem() {
        super(Family.all(DeathEventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SessionComponent sessionComponent = sm.get(sessionEntity);
        sessionComponent.kilLCount++;
        entity.remove(DeathEventComponent.class);
        entity.add(getEngine().createComponent(DeadComponent.class));

    }
}
