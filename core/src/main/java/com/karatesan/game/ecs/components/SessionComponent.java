package com.karatesan.game.ecs.components;

import com.badlogic.ashley.core.Component;
import com.karatesan.game.ecs.utility.State;

public class SessionComponent implements Component {
    public State currentState = State.PLAYING;
    public int kilLCount = 0;
}
