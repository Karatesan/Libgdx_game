package com.karatesan.game.data.blueprints;

public class PlayerBlueprint {

    public float size = 30f;

    public float maxHp = 100f;
    public float hpRegen = 0;
    public float hpRegenMultiplier = 1f;
    public float moveSpeed = 160f;

    // Offensive
    public float critChance = 0.05f;
    public float critMultiplier = 1.50f;
    public float damageMultiplier = 1.f;
    public float flatHpPerKill = 0;
    public float flatHpPerHit = 0;
    public float percentageOfDamageDealtHeal = 0;


    // Defensive
    public float armor = 0f;
    public float dodgeChance = 0f;

    // Utility
    public float pickupRadius = 50f;
    public float xpMultiplier = 1.0f;
    public float luck = 0f;
}
