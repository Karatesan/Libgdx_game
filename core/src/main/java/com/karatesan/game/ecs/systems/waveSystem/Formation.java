package com.karatesan.game.ecs.systems.waveSystem;

public enum Formation {
    SCATTER, // Enemies spawn at random angles (Ambient pressure)
    ARC,     // Enemies spawn in a wall from one direction (Directional pressure)
    CIRCLE   // Enemies spawn in a perfect ring around the player (The Trap)
}
