package com.karatesan.game.ecs.systems.render.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.karatesan.game.ecs.components.perks.PerkInventoryComponent;
import com.karatesan.game.ecs.utility.State;

public class LevelUpController {

    private boolean perkUIBuilt = false;
    private final LevelUpUIBuilder uiBuilder;
    private final Stage stage;
    private final PerkSelectionListener perkCallback;

    public LevelUpController(LevelUpUIBuilder uiBuilder, Stage stage, PerkSelectionListener perkCallback) {
        this.uiBuilder = uiBuilder;
        this.stage = stage;
        this.perkCallback = perkCallback;
    }

    public void update(State currentState, PerkInventoryComponent inventory, float playerLuckStat, float deltaTime) {
        if (currentState == State.LEVEL_UP) {
            if (!perkUIBuilt) {
                perkUIBuilt = uiBuilder.buildPerkUI(playerLuckStat, inventory, perkCallback);
            }
            stage.act(deltaTime);
            stage.draw();
        } else if (perkUIBuilt) {
            stage.clear();
            perkUIBuilt = false;
        }
    }

    public void reset() {
        stage.clear();
    }

    public boolean isActive() {
        return perkUIBuilt;
    }
}
