package com.karatesan.game.perks;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;

public class PerkRegistry {
    private final ObjectMap<String, PerkDefinition> perks = new ObjectMap<>();

    public PerkRegistry(FileHandle file) {
        Json json = new Json();
        json.setIgnoreUnknownFields(true);

        PerkDefinition[] definitions = json.fromJson(PerkDefinition[].class, file);
        for (PerkDefinition def : definitions) {
            perks.put(def.id, def);
        }
    }

    public PerkDefinition get(String id) {
        return perks.get(id);
    }

    public Array<PerkOffer> generateOffers(PerkInventoryComponent inventory, int count) {
        // 1. Build eligible pool — exclude maxed perks
        Array<PerkDefinition> eligible = new Array<>();
        for (PerkDefinition def : perks.values()) {
            if (!inventory.isMaxed(def.id, def.maxLevel)) {
                eligible.add(def);
            }
        }

        // 2. Shuffle and pick
        eligible.shuffle();
        int pickCount = Math.min(count, eligible.size);

        // 3. Build offers with correct next level
        Array<PerkOffer> offers = new Array<>(pickCount);
        for (int i = 0; i < pickCount; i++) {
            PerkDefinition def = eligible.get(i);
            int nextLevel = inventory.getLevel(def.id) + 1;
            offers.add(new PerkOffer(def, nextLevel));
        }

        return offers;
    }
}
