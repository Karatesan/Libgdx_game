package com.karatesan.game.ecs.systems.economy;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.karatesan.game.ecs.components.combat.StatsComponent;
import com.karatesan.game.ecs.components.combat.WeaponComponent;
import com.karatesan.game.ecs.components.event.LevelUpComponent;
import com.karatesan.game.ecs.components.perks.PerkChoiceComponent;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;
import com.karatesan.game.ecs.components.perks.RicochetPerkComponent;
import com.karatesan.game.perks.PerkDefinition;
import com.karatesan.game.perks.PerkEffect;
import com.karatesan.game.perks.PerkLevel;
import com.karatesan.game.perks.PerkRegistry;

public class PerkApplicationSystem extends IteratingSystem {
    private static final String TAG = "PerkApplicationSystem";

    private final PerkRegistry registry;

    private final ComponentMapper<PerkChoiceComponent> choiceM = ComponentMapper.getFor(PerkChoiceComponent.class);
    private final ComponentMapper<PerkInventoryComponent> inventoryM = ComponentMapper.getFor(
        PerkInventoryComponent.class);
    private final ComponentMapper<StatsComponent> statsM = ComponentMapper.getFor(StatsComponent.class);
    private final ComponentMapper<WeaponComponent> weaponM = ComponentMapper.getFor(WeaponComponent.class);

    public PerkApplicationSystem(PerkRegistry registry) {
        super(Family.all(PerkChoiceComponent.class, PerkInventoryComponent.class, StatsComponent.class,
            WeaponComponent.class).get());
        this.registry = registry;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PerkChoiceComponent choice = choiceM.get(entity);
        PerkInventoryComponent inventory = inventoryM.get(entity);

        PerkDefinition definition = registry.get(choice.perkId);
        if (definition == null) {
            Gdx.app.error(TAG, "Unknown perk: " + choice.perkId);
            entity.remove(PerkChoiceComponent.class);
            return;
        }

        int nextLevel = inventory.getLevel(choice.perkId) + 1;
        if (nextLevel > definition.maxLevel) {
            Gdx.app.error(TAG, "Perk already maxed: " + choice.perkId);
            entity.remove(PerkChoiceComponent.class);
            return;
        }

        // Apply all effects for this level
        PerkLevel levelData = definition.getLevel(nextLevel);
        for (PerkEffect effect : levelData.effects) {
            switch (effect.type) {
                case STAT_MODIFIER:
                    applyStatModifier(entity, effect);
                    break;
                case TRAIT_ADDITION:
                    applyTraitAddition(entity, effect);
                    break;
            }
        }

        // Record in inventory
        inventory.acquiredPerks.put(choice.perkId, nextLevel);

        // Clean up — level-up is complete
        entity.remove(PerkChoiceComponent.class);
        entity.remove(LevelUpComponent.class);
    }

    private void applyStatModifier(Entity entity, PerkEffect effect) {
        StatsComponent stats = statsM.get(entity);
        WeaponComponent weapon = weaponM.get(entity);

        switch (effect.target) {
            // StatsComponent
            case "damageMultiplier":
                stats.damageMultiplier += effect.value;
                break;
            case "critChance":
                stats.critChance += effect.value;
                break;
            case "critMultiplier":
                stats.critMultiplier += effect.value;
                break;

            // WeaponComponent
            case "minDamage":
                weapon.minDamage += effect.value;
                break;
            case "maxDamage":
                weapon.maxDamage += effect.value;
                break;
            case "fireRate":
                weapon.fireRate -= effect.value;
                break;
            case "projectileCount":
                weapon.projectileCount += (int) effect.value;
                break;
            case "spreadAngle":
                weapon.spreadAngle += effect.value;
                break;
            case "projectileSpeed":
                weapon.projectileSpeed += effect.value;
                break;
            case "range":
                weapon.range += effect.value;
                break;

            default:
                Gdx.app.error(TAG, "Unknown stat target: " + effect.target);
        }
    }

    private void applyTraitAddition(Entity entity, PerkEffect effect) {
        switch (effect.target) {
            case "RICOCHET_CHANCE": {
                RicochetPerkComponent c = entity.getComponent(RicochetPerkComponent.class);
                if (c == null) {
                    c = getEngine().createComponent(RicochetPerkComponent.class);
                    entity.add(c);
                }
                c.chance += effect.value;
                break;
            }


            // case "PIERCE": {
            //     PiercePerkComponent c = entity.getComponent(PiercePerkComponent.class);
            //     if (c == null) {
            //         c = getEngine().createComponent(PiercePerkComponent.class);
            //         entity.add(c);
            //     }
            //     c.pierceCount += (int) effect.value;
            //     break;
            // }

            default:
                Gdx.app.error(TAG, "Unknown trait target: " + effect.target);
        }
    }
}
