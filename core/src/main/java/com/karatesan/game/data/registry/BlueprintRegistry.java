package com.karatesan.game.data.registry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.karatesan.game.data.blueprints.EnemyBlueprint;
import com.karatesan.game.data.blueprints.PlayerBlueprint;
import com.karatesan.game.data.blueprints.WeaponBlueprint;
import com.karatesan.game.debug.DebugDisplay;
import com.karatesan.game.ecs.factory.EnemyType;

public class BlueprintRegistry {

    private PlayerBlueprint playerBlueprint;
    private WeaponBlueprint weaponBlueprint;
    private final ObjectMap<EnemyType, EnemyBlueprint> enemies = new ObjectMap<>();

    public void load() {
        Json json = new Json();
        json.setIgnoreUnknownFields(true);

        // Player
        playerBlueprint = json.fromJson(PlayerBlueprint.class, Gdx.files.internal("data/blueprints/player.json"));
        System.out.println(playerBlueprint.xpMultiplier);
        // Weapon
        weaponBlueprint = json.fromJson(WeaponBlueprint.class,
            Gdx.files.internal("data/blueprints/weapons/basicWeapon.json"));


        // Enemies — filename must match EnemyType name (lowercase)
        for (EnemyType type : EnemyType.values()) {
            FileHandle file = Gdx.files.internal("data/blueprints/enemies/" + type.name().toLowerCase() + ".json");
            if (file.exists()) {
                EnemyBlueprint enemy = json.fromJson(EnemyBlueprint.class, file);
                enemies.put(type, enemy);
            }
        }
    }

    public PlayerBlueprint getPlayer() {
        return playerBlueprint;
    }

    public WeaponBlueprint getWeapon() {
        return weaponBlueprint;
    }

    public EnemyBlueprint getEnemy(EnemyType type) {
        return enemies.get(type);
    }
}
