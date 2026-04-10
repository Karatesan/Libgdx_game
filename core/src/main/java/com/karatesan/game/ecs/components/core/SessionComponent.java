package com.karatesan.game.ecs.components.core;

import com.badlogic.ashley.core.Component;
import com.karatesan.game.ecs.utility.State;

public class SessionComponent implements Component {
    public State currentState = State.PLAYING;
    public int kilLCount = 0;

    public float timeSurvived = 0f;
    public int currentWave = 1;
    public float waveTextTimer = 3f; // How long to display "WAVE X" on screen

    public float currentXp = 0f;
    public float xpToNextLevel = 10f; // First level requires 10 XP
    public int currentLevel = 1;
}
