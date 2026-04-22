package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

public class StatsComponent implements Component, Poolable {
    // === EXISTING ===
    public float critChance = 0.1f;
    public float critMultiplier = 2.0f;
    public float damageMultiplier = 1.0f;

    // === PRIORITY 1: Unlocks defensive perks ===
    public float armor = 0f;            // Flat damage reduction per hit
    public float dodgeChance = 0f;      // 0.0 to 0.5 cap
    public float moveSpeed = 200f;      // Whatever your current hardcoded value is

    // === PRIORITY 2: Unlocks utility perks ===
    public float pickupRadius = 50f;    // Auto-collect radius for XP/drops
    public float xpMultiplier = 1.0f;   // % increase to XP gained
    public float luck = 0f;             // Shifts rarity weights when offered perks

    // === PRIORITY 3: Unlocks explosion/knockback ===
    public float knockbackForce = 0f;   // Applied to enemies on bullet hit
    public float explosionRadius = 60f; // Base radius when explosion procs
    public float explosionDamage = 0.5f;// % of bullet damage

    @Override
    public void reset() {
        //TODO add reset
    }
}


/*
    1. dodajemy kalkulacje armora - w momencie uderzenia w gracza. Najlepiej jako % dmg chyba
    2. Dodajemy dodge - Przed zadaniem obrazen - sprawdzamy czy szansa na dodge jest wieksza niz random i jak tak to nei zadajemy.
        trzeba bedzie zmodyfikwoac FloatTextSystem zeby wyswietlal napis dodge
    3. Movement speed idzie do stats component (gdzies jest hardcoded)
    4. Dodac hp regen w jakims update
    5.




 */
