package com.karatesan.game.ecs.systems.economy;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.economy.XpComponent;
import com.karatesan.game.ecs.components.event.CollectedEventComponent;
import com.karatesan.game.ecs.components.event.LevelUpComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.systems.core.PausableSystem;
import com.karatesan.game.ecs.utility.ECSUtils;
import com.karatesan.game.ecs.utility.State;

public class XpProcessingSystem extends IteratingSystem implements PausableSystem {
    private final ComponentMapper<XpComponent> xm = ComponentMapper.getFor(XpComponent.class);
    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);
    private Entity sessionEntity;

    public XpProcessingSystem() {
        // I only care about XP items that have been COLLECTED
        super(Family.all(XpComponent.class, CollectedEventComponent.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        sessionEntity = engine.getEntitiesFor(Family.all(SessionComponent.class).get()).first();
    }

    @Override
    protected void processEntity(Entity xpEntity, float deltaTime) {
        XpComponent xpComp = xm.get(xpEntity);
        SessionComponent session = sm.get(sessionEntity);

        // 1. Do the XP Math
        session.currentXp += xpComp.value;

        // 2. Level Up Logic
        if (session.currentXp >= session.xpToNextLevel) {
            session.currentXp -= session.xpToNextLevel;
            session.currentLevel++;
            session.xpToNextLevel *= 1.5f;
            Entity player = ECSUtils.getPlayer(getEngine());
            player.add(getEngine().createComponent(LevelUpComponent.class));
        }

        // 3. Kill the entity (It has been consumed)
        xpEntity.add(getEngine().createComponent(DeadComponent.class));
    }
}
