package com.karatesan.game.data.perk.handlers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.JsonValue;
import com.karatesan.game.data.blueprints.PlayerBlueprint;
import com.karatesan.game.data.perk.PerkLevel;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.perks.LifeStealComponent;

public class BloodthirstPerkHandler implements PerkHandler {

    private final PlayerBlueprint playerBlueprint;

    public BloodthirstPerkHandler(PlayerBlueprint playerBlueprint) {
        this.playerBlueprint = playerBlueprint;
    }

    @Override
    public void create(JsonValue perkData) {

    }

    @Override
    public void onAcquire(Entity player, int level, PerkLevel[] levels, Engine engine) {
        LifeStealComponent lifeStealComponent = Mappers.lifeSteal.get(player);
        if (lifeStealComponent == null) {
            lifeStealComponent = engine.createComponent(LifeStealComponent.class);
            player.add(lifeStealComponent);
        }
        lifeStealComponent.flatHpPerKill = playerBlueprint.flatHpPerKill;
        for (int i = 0; i < level; i++) {
            for (JsonValue effectNode = levels[i].behavioralConfig.child; effectNode != null; effectNode = effectNode.next) {
                String type = effectNode.getString("target");
                if (type.equals("flatHpPerKill")) {
                    int value = effectNode.getInt("value");
                    lifeStealComponent.flatHpPerKill += value;
                }
            }
        }
    }
}
