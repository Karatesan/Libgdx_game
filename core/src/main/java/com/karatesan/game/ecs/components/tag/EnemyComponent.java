package com.karatesan.game.ecs.components.tag;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.karatesan.game.ecs.factory.EnemyType;

public class EnemyComponent implements Component, Pool.Poolable {

    public EnemyType type = EnemyType.STANDARD;

    @Override
    public void reset() {
        type = EnemyType.STANDARD;
    }
}
