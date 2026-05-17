package com.karatesan.game.ecs.components.perks;

import com.badlogic.ashley.core.Component;

public class LifeStealComponent implements Component {
    public float flatHpPerHit;
    public float flatHpPerKill;
    public float percentageOfDamageDealtHeal;
}
