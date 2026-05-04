package com.karatesan.game.ecs.components.perks;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool;

public class PierceComponent implements Component, Pool.Poolable {
    public int pierceCount;
    public float pierceDamageRetention;
    public Entity lastHit;

    @Override
    public void reset() {
        pierceCount = 0;
        pierceDamageRetention = 0;
        lastHit = null;
    }
}
