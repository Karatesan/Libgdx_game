package com.karatesan.game.ecs.systems.combat.perks;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.ecs.components.combat.PierceComponent;
import com.karatesan.game.ecs.components.event.HitEventComponent;
import com.karatesan.game.ecs.components.event.PardonedComponent;
import com.karatesan.game.ecs.components.perks.PiercePerkComponent;
import com.karatesan.game.ecs.systems.core.PausableSystem;
import com.karatesan.game.ecs.utility.ECSUtils;

public class PiercePerkSystem extends IteratingSystem implements PausableSystem {

    private final ComponentMapper<PiercePerkComponent> perkMapper = ComponentMapper.getFor(PiercePerkComponent.class);
    private final ComponentMapper<PierceComponent> pierceMapper = ComponentMapper.getFor(PierceComponent.class);
    private final ComponentMapper<HitEventComponent> eventMapper = ComponentMapper.getFor(HitEventComponent.class);


    private Entity playerEntity;


    public PiercePerkSystem() {
        super(Family.all(HitEventComponent.class).get());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        playerEntity = ECSUtils.getPlayer(engine);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PiercePerkComponent piercePerkComponent = perkMapper.get(playerEntity);
        if (piercePerkComponent != null) {
            HitEventComponent hitEventComponent = eventMapper.get(entity);
            PierceComponent pierceComponent = pierceMapper.get(hitEventComponent.bullet);
            if (!pierceComponent.wasHit(hitEventComponent.targetEntity)) {
                pierceComponent.addEntityHit(hitEventComponent.targetEntity);
                hitEventComponent.bullet.add(getEngine().createComponent(PardonedComponent.class));
            }
        }
    }
}
