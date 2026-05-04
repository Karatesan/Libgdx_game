package com.karatesan.game.config;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.components.tag.PlayerComponent;

public class GameContext {

    private Entity playerEntity;
    private Entity sessionEntity;

    public Entity getPlayer() {
        return playerEntity;
    }

    public Entity getSession() {
        return sessionEntity;
    }

    /**
     * Call once after creating the engine. Registers EntityListeners
     * that automatically track singleton entities.
     */
    public void registerListeners(Engine engine) {
        engine.addEntityListener(Family.all(PlayerComponent.class).get(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                playerEntity = entity;
            }

            @Override
            public void entityRemoved(Entity entity) {
                playerEntity = null;
            }
        });

        engine.addEntityListener(Family.all(SessionComponent.class).get(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                sessionEntity = entity;
            }

            @Override
            public void entityRemoved(Entity entity) {
                sessionEntity = null;
            }
        });
    }
}
