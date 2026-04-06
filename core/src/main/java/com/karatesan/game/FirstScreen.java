package com.karatesan.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.karatesan.game.ecs.factory.EntityFactory;
import com.karatesan.game.ecs.systems.*;

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
        engine.addSystem(new MovementSystem());
        engine.addSystem(new WeaponSystem(entityFactory));
        engine.addSystem(new InvincibilitySystem());
        engine.addSystem(new EnemySystem());
        engine.addSystem(new BulletSystem());
        engine.addSystem(new CollisionSystem(entityFactory));
        engine.addSystem(new BulletLifecycleSystem());
        engine.addSystem(new CameraSystem(camera));
        engine.addSystem(new GameStateSystem());
        //engine.addSystem(new FloatingTextSystem());
        engine.addSystem(new RenderSystem(game.spriteBatch, game.shapeDrawer, game.uiFont, camera));
        engine.addSystem(new UISystem(game.spriteBatch, game.uiFont, uiCamera));
        engine.addSystem(new WaveSpawnerSystem(entityFactory));
        engine.addSystem(new ScoreSystem());
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

    public enum State { PLAYING, GAME_OVER }
}
