package com.karatesan.game.ecs.factory;

public enum EnemyType {
    STANDARD, // Medium speed, medium health
    SWARMER,  // Very fast, low health, small hitbox (Creates panic)
    TANK      // Very slow, massive health, huge hitbox (Acts as a meat-shield)
}
