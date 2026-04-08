package com.karatesan.game.ecs.systems.waveSystem;

public class WaveData {
    public float startTime; // When does this wave start? (in seconds)
    public float spawnRate; // How often to spawn?
    public int spawnAmount; // How many enemies per spawn?
    public Formation formation;

    public WaveData(float startTime, float spawnRate, int spawnAmount, Formation formation) {
        this.startTime = startTime;
        this.spawnRate = spawnRate;
        this.spawnAmount = spawnAmount;
        this.formation = formation;
    }
}
