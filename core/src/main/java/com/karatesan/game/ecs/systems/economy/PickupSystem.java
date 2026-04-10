package com.karatesan.game.ecs.systems.economy;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.economy.CollectibleComponent;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.economy.XpComponent;
import com.karatesan.game.ecs.components.event.CollectedEventComponent;
import com.karatesan.game.ecs.components.physics.HitboxComponent;
import com.karatesan.game.ecs.components.physics.TransformComponent;
import com.karatesan.game.ecs.utility.ECSUtils;

public class PickupSystem extends IteratingSystem {

    private final ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<HitboxComponent> hm = ComponentMapper.getFor(HitboxComponent.class);
    private final ComponentMapper<XpComponent> xm = ComponentMapper.getFor(XpComponent.class);
    private final ComponentMapper<SessionComponent> sm = ComponentMapper.getFor(SessionComponent.class);

    private Entity playerEntity;

    public PickupSystem() {
        super(Family.all(CollectibleComponent.class, TransformComponent.class, HitboxComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        playerEntity = ECSUtils.getPlayer(getEngine());
        super.update(deltaTime);
    }

    @Override
    protected void processEntity(Entity item, float deltaTime) {
        if (playerEntity == null) return;

        TransformComponent pPos = tm.get(playerEntity);
        HitboxComponent pBox = hm.get(playerEntity);

        TransformComponent xpPos = tm.get(item);
        HitboxComponent xpBox = hm.get(item);

        // Check Collision
        float dx = pPos.x - xpPos.x;
        float dy = pPos.y - xpPos.y;
        float distSq = (dx * dx) + (dy * dy);

        float combinedRadii = pBox.radius + xpBox.radius;

        // IF THE PLAYER TOUCHES THE XP...
        if (distSq <= (combinedRadii * combinedRadii)) {
            // 1. Tag it as Collected!
            item.add(getEngine().createComponent(CollectedEventComponent.class));

            // 2. Remove Collectible so we don't pick it up twice in one frame
            item.remove(CollectibleComponent.class);
        }
    }
}
