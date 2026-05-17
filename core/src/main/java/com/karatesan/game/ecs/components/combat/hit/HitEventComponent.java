package com.karatesan.game.ecs.components.combat.hit;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

public class HitEventComponent implements Component, Pool.Poolable {
    public Entity target;
    public Entity source;
    public Entity attacker;

    public HitSourceType sourceType;
    public HitOutcome outcome = HitOutcome.PENDING;

    public float rawDamage;
    public float finalDamage;

    public boolean crit;
    public boolean lethal;

    public float x;
    public float y;

    @Override
    public void reset() {
        target = null;
        source = null;
        attacker = null;

        sourceType = null;
        outcome = HitOutcome.PENDING;

        rawDamage = 0f;
        finalDamage = 0f;

        crit = false;
        lethal = false;

        x = 0f;
        y = 0f;
    }

    @Override
    public String toString() {
        return "HitEventComponent{" + "target=" + target + ", source=" + source + ", attacker=" + attacker + ", sourceType=" + sourceType + ", outcome=" + outcome + ", rawDamage=" + rawDamage + ", finalDamage=" + finalDamage + ", crit=" + crit + ", lethal=" + lethal + ", x=" + x + ", y=" + y + '}';
    }
}
