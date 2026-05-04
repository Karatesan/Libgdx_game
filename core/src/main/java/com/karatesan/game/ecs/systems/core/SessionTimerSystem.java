package com.karatesan.game.ecs.systems.core;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.EntitySystem;
import com.karatesan.game.config.GameConfig;
import com.karatesan.game.config.GameContext;
import com.karatesan.game.ecs.components.core.SessionComponent;
import com.karatesan.game.ecs.utility.PausableSystem;

public class SessionTimerSystem extends EntitySystem implements PausableSystem {

    private final ComponentMapper<SessionComponent> sessionMapper = ComponentMapper.getFor(SessionComponent.class);
    private final GameConfig config;
    private final GameContext context;

    public SessionTimerSystem(GameConfig config, GameContext context) {
        this.config = config;
        this.context = context;
    }

    @Override
    public void update(float deltaTime) {
        if (context.getSession() == null) return;
        SessionComponent session = sessionMapper.get(context.getSession());

        session.timeSurvived += deltaTime;

        if (session.waveTextTimer > 0) {
            session.waveTextTimer -= deltaTime;
        }

        int calculatedWave = (int) (session.timeSurvived / config.waveChangeTime) + 1;
        if (calculatedWave > session.currentWave) {
            session.currentWave = calculatedWave;
            session.waveTextTimer = config.waveTextDuration;
        }
    }
}
