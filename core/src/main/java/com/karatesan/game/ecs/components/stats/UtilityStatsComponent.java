package com.karatesan.game.ecs.components.stats;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class UtilityStatsComponent implements Component, Pool.Poolable {
    public float pickupRadius = 50f;    // Auto-collect radius for XP/drops
    public float xpMultiplier = 1.0f;   // % increase to XP gained
    public float luck = 0f;             // Shifts rarity weights when offered perks

    @Override
    public void reset() {
        pickupRadius = 50f;    // Auto-collect radius for XP/drops
        xpMultiplier = 1.0f;   // % increase to XP gained
        luck = 0f;             // Shifts rarity weights when offered perks
    }
}
