package com.karatesan.game.ecs.components.combat;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class PierceComponent implements Component, Pool.Poolable {
    public Array<Entity> entitiesHit = new Array<>();

    public boolean wasHit(Entity entity) {
        return entitiesHit.contains(entity, true);
    }

    public void addEntityHit(Entity entity){
        entitiesHit.add(entity);
    }

    @Override
    public void reset() {
        entitiesHit.clear();
    }
}
