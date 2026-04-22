package com.karatesan.game.perks;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;

public class PerkRegistry {
    private final ObjectMap<String, PerkDefinition> perks = new ObjectMap<>();
    //TODO remove once all perks are working
    private final Array<String> included = new Array<>(new String[]{"bouncing_chaos", "depleted_uranium"});

    public PerkRegistry(FileHandle file) {
        Json json = new Json();
        json.setIgnoreUnknownFields(true);

        PerkDefinition[] definitions = json.fromJson(PerkDefinition[].class, file);
        for (PerkDefinition def : definitions) {
            if (included.contains(def.id, false)) perks.put(def.id, def);
        }
    }

    public PerkDefinition get(String id) {
        return perks.get(id);
    }

    public Array<PerkOffer> generateOffers(PerkInventoryComponent inventory, int count, float luck) {
        // 1. Build eligible pool with weights
        Array<PerkDefinition> eligible = new Array<>();
        FloatArray weights = new FloatArray();

        for (PerkDefinition def : perks.values()) {
            if (!inventory.isMaxed(def.id, def.maxLevel)) {
                eligible.add(def);
                weights.add(getWeight(def.rarity, luck));
            }
        }

        // 2. Weighted pick without replacement
        int pickCount = Math.min(count, eligible.size);
        Array<PerkOffer> offers = new Array<>(pickCount);

        for (int i = 0; i < pickCount; i++) {
            int index = pickWeightedRandom(weights);

            PerkDefinition def = eligible.get(index);
            int nextLevel = inventory.getLevel(def.id) + 1;
            offers.add(new PerkOffer(def, nextLevel));

            // Remove from pool so it can't be picked again
            eligible.removeIndex(index);
            weights.removeIndex(index);
        }

        return offers;
    }

    private int pickWeightedRandom(FloatArray weights) {
        float total = 0;
        for (int i = 0; i < weights.size; i++) {
            total += weights.get(i);
        }

        float roll = MathUtils.random() * total;
        float cumulative = 0;

        for (int i = 0; i < weights.size; i++) {
            cumulative += weights.get(i);
            if (roll <= cumulative) {
                return i;
            }
        }

        return weights.size - 1;
    }

    private float getWeight(Rarity rarity, float luck) {
        float base;
        float luckScale;

        switch (rarity) {
            case COMMON:
                base = 28f;
                luckScale = -0.006f;  // luck reduces common weight
                break;
            case UNCOMMON:
                base = 7f;
                luckScale = 0.003f;
                break;
            case RARE:
                base = 6f;
                luckScale = 0.008f;
                break;
            case LEGENDARY:
                base = 3f;
                luckScale = 0.015f;
                break;
            default:
                base = 7f;
                luckScale = 0f;
        }

        return Math.max(0.1f, base * (1f + luck * luckScale));
    }
}
