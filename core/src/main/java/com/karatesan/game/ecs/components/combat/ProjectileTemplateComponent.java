package com.karatesan.game.ecs.components.combat;

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
    public float explosionRadius;
    public float explosionDamageRatio = 0.85f;

    //knockback-----------------------------------
    public float knockbackForce;

    @Override
    public void reset() {
        ricochetChance = 0f;
        ricochetCount = 0;
        pierceCount = 0;
        explosionRadius = 0;
    }
}
