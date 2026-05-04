package com.karatesan.game.ecs.components.economy;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class LevelDataComponent implements Component, Pool.Poolable {
    public int currentXp = 0;
    public int xpToNextLevel = 10; // First level requires 10 XP
    public int currentLevel = 1;
    @Override
    public void reset() {

    }
}
