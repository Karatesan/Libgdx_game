package com.karatesan.game.ecs.components.economy;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

public class LootDropComponent implements Component, Pool.Poolable {

    public int xpValue = 0;

    @Override
    public void reset() {
        xpValue = 0;
    }
}
