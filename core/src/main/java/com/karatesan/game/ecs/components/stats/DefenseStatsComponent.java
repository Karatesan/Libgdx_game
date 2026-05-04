package com.karatesan.game.ecs.components.stats;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class DefenseStatsComponent implements Component, Pool.Poolable {
    public float armor;
    public float dodgeChance;

    @Override
    public void reset() {
        armor = 0;
        dodgeChance = 0;
    }
}
