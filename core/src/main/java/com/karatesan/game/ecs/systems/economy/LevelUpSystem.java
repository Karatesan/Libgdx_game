package com.karatesan.game.ecs.systems.economy;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.economy.LevelDataComponent;
import com.karatesan.game.ecs.components.event.LevelUpComponent;
import com.karatesan.game.ecs.utility.PausableSystem;

public class LevelUpSystem extends EntitySystem implements PausableSystem {

    private final GameContext context;
    private final GameConfig config;

    public LevelUpSystem(GameContext context, GameConfig config) {
        this.context = context;
        this.config = config;
    }

    @Override
    public void update(float deltaTime) {
        Entity player = context.getPlayer();
        if (player == null) return;

        // Don't trigger another level-up while one is pending
        if (Mappers.levelUp.has(player)) return;
        if (Mappers.perkChoice.has(player)) return;

        LevelDataComponent levelData = Mappers.level.get(player);
        if (levelData.currentXp >= levelData.xpToNextLevel) {
            levelData.currentXp -= levelData.xpToNextLevel;
            levelData.currentLevel++;
            levelData.xpToNextLevel = computeThreshold(levelData.currentLevel);
            player.add(getEngine().createComponent(LevelUpComponent.class));
        }
    }

    private int computeThreshold(int level) {
        return (int) Math.floor(config.xpFormulaBase * Math.pow(level, config.xpFormulaExponent));
    }
}
