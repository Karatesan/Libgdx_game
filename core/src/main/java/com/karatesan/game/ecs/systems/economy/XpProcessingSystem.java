package com.karatesan.game.ecs.systems.economy;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.economy.LevelDataComponent;
import com.karatesan.game.ecs.components.economy.XpComponent;
import com.karatesan.game.ecs.components.event.CollectedEventComponent;
import com.karatesan.game.ecs.components.event.LevelUpComponent;
import com.karatesan.game.ecs.components.stats.UtilityStatsComponent;
import com.karatesan.game.ecs.components.tag.DeadComponent;
import com.karatesan.game.ecs.utility.PausableSystem;
import com.karatesan.game.ecs.utility.ECSUtils;

public class XpProcessingSystem extends IteratingSystem implements PausableSystem {

    private final GameContext context;

    public XpProcessingSystem(GameContext context) {
        super(Family.all(XpComponent.class, CollectedEventComponent.class).get());
        this.context = context;
    }

    @Override
    protected void processEntity(Entity xpEntity, float deltaTime) {
        Entity player = context.getPlayer();
        LevelDataComponent levelData = Mappers.level.get(player);
        UtilityStatsComponent utility = Mappers.utility.get(player);

        XpComponent xpComp = Mappers.xp.get(xpEntity);
        levelData.currentXp += (int) (xpComp.value * utility.xpMultiplier);

        xpEntity.add(getEngine().createComponent(DeadComponent.class));
    }
}
