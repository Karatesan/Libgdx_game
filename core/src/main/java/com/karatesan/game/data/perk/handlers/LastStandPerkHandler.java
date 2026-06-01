package com.karatesan.game.data.perk.handlers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.JsonValue;
import com.karatesan.game.data.perk.PerkLevel;
import com.karatesan.game.debug.DebugDisplay;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.ecs.components.perks.LastStandComponent;

public class LastStandPerkHandler implements PerkHandler {
    @Override
    public void create(JsonValue perkData) {

    }

    @Override
    public void onAcquire(Entity player, int level, PerkLevel[] levels, Engine engine) {
        LastStandComponent lastStand = Mappers.lastStand.get(player);
        if (lastStand == null) {
            lastStand = engine.createComponent(LastStandComponent.class);
            player.add(lastStand);
        }

        lastStand.hpThresholdActivation = 0;
        lastStand.damageMultiplier = 0;
        lastStand.moveSpeedMultiplier = 0;
        lastStand.critChance = 0;
        lastStand.projectileCount = 0;

        for (int i = 0; i < level; i++) {
            for (JsonValue effectNode = levels[i].behavioralConfig.child; effectNode != null; effectNode = effectNode.next) {
                lastStand.hpThresholdActivation = effectNode.getFloat("conditionValue");
                JsonValue buffs = effectNode.get("buffs");

                for (JsonValue buff = buffs.child; buff != null; buff = buff.next) {
                    String target = buff.getString("target");
                    float value = buff.getFloat("value");
                    if (target.equals("damageMultiplier")) lastStand.damageMultiplier += value;
                    if (target.equals("moveSpeedMultiplier")) lastStand.moveSpeedMultiplier += value;
                    if (target.equals("critChance")) lastStand.critChance += value;
                    if (target.equals("projectileCount")) lastStand.projectileCount += (int) value;
                }
            }
        }
        DebugDisplay.logDebug(
            lastStand.hpThresholdActivation + " " + lastStand.damageMultiplier + "  " + lastStand.moveSpeedMultiplier);
    }
}

