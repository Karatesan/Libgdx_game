package com.karatesan.game.ecs.components.perks;

import com.badlogic.ashley.core.Component;

public class LastStandComponent implements Component {
    public float hpThresholdActivation;
    public float damageMultiplier;
    public float moveSpeedMultiplier;
    public float critChance;
    public int projectileCount;
    public boolean isActive;
}
