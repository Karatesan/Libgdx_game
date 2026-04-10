package com.karatesan.game;

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.systems.combat.*;
import com.karatesan.game.ecs.systems.core.CameraSystem;
import com.karatesan.game.ecs.systems.core.CleanupSystem;
import com.karatesan.game.ecs.systems.core.GameStateSystem;
import com.karatesan.game.ecs.systems.core.LifeTimeSystem;
import com.karatesan.game.ecs.systems.economy.MagnetSystem;
import com.karatesan.game.ecs.systems.economy.PickupSystem;
import com.karatesan.game.ecs.systems.economy.XpProcessingSystem;
import com.karatesan.game.ecs.systems.movement.EnemySeparationSystem;
import com.karatesan.game.ecs.systems.movement.EnemySystem;
import com.karatesan.game.ecs.systems.movement.MovementSystem;
import com.karatesan.game.ecs.systems.movement.PlayerInputSystem;
import com.karatesan.game.ecs.systems.render.RenderSystem;
import com.karatesan.game.ecs.systems.render.UISystem;
import com.karatesan.game.ecs.systems.waveSystem.WaveSpawnerSystem;

/**
 * First screen of the application. Displayed after the application is created.
 */
public class FirstScreen implements Screen {

    private final Main game;
    private OrthographicCamera camera;
    private ExtendViewport viewport;
    private OrthographicCamera uiCamera;
    private ExtendViewport uiViewport;
    private EntityFactory entityFactory;
    public State gameState = State.PLAYING;

    // The heart of our new architecture
    private PooledEngine engine;

    public FirstScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        // Our game world is 800x600 units
        viewport = new ExtendViewport(800, 600, camera);

        uiCamera = new OrthographicCamera();
        uiViewport = new ExtendViewport(800, 600, uiCamera);

        engine = new PooledEngine();
        entityFactory = new EntityFactory(engine);

        entityFactory.createPlayer();
        entityFactory.createSession();

        engine.addSystem(new PlayerInputSystem(camera));
        engine.addSystem(new WeaponSystem(entityFactory));
        engine.addSystem(new InvincibilitySystem());
        engine.addSystem(new EnemySystem());
        engine.addSystem(new EnemySeparationSystem());
        engine.addSystem(new MovementSystem());
        engine.addSystem(new BulletSystem());
        engine.addSystem(new CollisionSystem(entityFactory));
        engine.addSystem(new RicochetPerkSystem());
        engine.addSystem(new BulletLifecycleSystem());
        engine.addSystem(new CameraSystem(camera));
        engine.addSystem(new MagnetSystem());
        engine.addSystem(new PickupSystem());
        engine.addSystem(new XpProcessingSystem());
        engine.addSystem(new GameStateSystem());
        engine.addSystem(new RenderSystem(game.spriteBatch, game.shapeDrawer, game.uiFont, camera, game.floorTexture));
        engine.addSystem(new UISystem(game.spriteBatch, game.uiFont, game.shapeDrawer, uiViewport));
        engine.addSystem(new WaveSpawnerSystem(entityFactory));
        engine.addSystem(new DeathSystem(entityFactory));
        engine.addSystem(new LifeTimeSystem());
        engine.addSystem(new CleanupSystem());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        viewport.apply();
        // 3. Tell the batch to use the Camera's coordinates
        game.spriteBatch.setProjectionMatrix(camera.combined);

        engine.update(delta);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        engine.removeAllEntities();
    }

    public enum State {PLAYING, GAME_OVER}
}
