package com.karatesan.game.ecs.systems.economy;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.event.LevelUpComponent;
import com.karatesan.game.ecs.components.event.StatsRecalculationFlag;
import com.karatesan.game.ecs.components.perks.PerkChoiceComponent;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;
import com.karatesan.game.ecs.Mappers;
import com.karatesan.game.data.perk.PerkDefinition;
import com.karatesan.game.data.registry.PerkRegistry;

public class PerkApplicationSystem extends EntitySystem {

    private static final String TAG = "PerkApplicationSystem";

    private final PerkRegistry registry;
    private final GameContext context;

    public PerkApplicationSystem(PerkRegistry registry, GameContext context) {
        this.registry = registry;
        this.context = context;
    }

    @Override
    public void update(float deltaTime) {
        Entity player = context.getPlayer();
        if (player == null) return;

        PerkChoiceComponent choice = Mappers.perkChoice.get(player);
        if (choice == null) return;

        PerkInventoryComponent inventory = Mappers.perks.get(player);
        PerkDefinition definition = registry.get(choice.perkId);

        if (definition == null) {
            Gdx.app.error(TAG, "Unknown perk: " + choice.perkId);
            cleanUp(player);
            return;
        }

        int nextLevel = inventory.getLevel(choice.perkId) + 1;
        if (nextLevel > definition.maxLevel) {
            Gdx.app.error(TAG, "Perk already maxed: " + choice.perkId);
            cleanUp(player);
            return;
        }
        // Record in inventory
        inventory.acquiredPerks.put(choice.perkId, nextLevel);

        // Flag for stat rebuild — StatRecalculationSystem handles the math
        if (!Mappers.recalcFlag.has(player)) {
            player.add((getEngine()).createComponent(StatsRecalculationFlag.class));
        }
        cleanUp(player);
    }

    private void cleanUp(Entity player) {
        player.remove(PerkChoiceComponent.class);
        player.remove(LevelUpComponent.class);
    }
}
