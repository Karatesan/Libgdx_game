package com.karatesan.game.ecs.utility;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.karatesan.game.ecs.components.tag.PlayerComponent;

public class ECSUtils {
    public static Entity getPlayer(Engine engine) {
        var players = engine.getEntitiesFor(Family.all(PlayerComponent.class).get());
        return players.size() > 0 ? players.first() : null;
    }
}
