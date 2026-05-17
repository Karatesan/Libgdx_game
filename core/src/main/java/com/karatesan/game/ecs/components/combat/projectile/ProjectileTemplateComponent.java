package com.karatesan.game.ecs.components.combat.projectile;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class ProjectileTemplateComponent implements Component, Pool.Poolable {
    //Ricochet-----------------------------------------------
    public float ricochetChance = 0f;
    public int ricochetCount = 0;
    public float ricochetDamageRetention = 0.85f;

    //Pierce------------------------------
    public int pierceCount = 0;
    public float pierceDamageRetention = 0.85f;

    //Explosive------------------------------------
    public float explosionChance;
    public float explosionRadius;
    public float explosionDamageRatio = 0.85f;

    //knockback-----------------------------------
    public float knockbackForce;

    //future perks

    @Override
    public void reset() {
        ricochetChance = 0f;
        ricochetCount = 0;
        ricochetDamageRetention = 0.85f;

        pierceCount = 0;
        pierceDamageRetention = 0.85f;

        explosionRadius = 0f;
        explosionDamageRatio = 0.85f;

        knockbackForce = 0f;
    }
}
