package com.thulium.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.thulium.util.Units;

/**
 * Handles camera, viewport, and scene-related visuals
 */
public class GameWorldEnvironment {
    private final Vector2 min = new Vector2();
    private final Vector2 max = new Vector2();

    private final OrthographicCamera camera;
    private final OrthographicCamera textCamera;
    private final Viewport viewport;

    private final CameraHelper shaker;

    private final int worldWidth;
    private final int worldHeight;

    public GameWorldEnvironment(int worldWidth, int worldHeight) {
        // Set world variables
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        // Create camera and view
        viewport = new ExtendViewport(Units.WIDTH, Units.HEIGHT);// viewport = new StretchViewport(Units.WIDTH, Units.HEIGHT);
        camera = new OrthographicCamera();
        viewport.setCamera(camera);
        viewport.apply();
        camera.update();

        // Create text camera
        textCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shaker = new CameraHelper();
    }

    public void updateCamera(Vector2 position, float delta) {
        float camX = MathUtils.clamp(position.x, min.x, max.x);
        float camY = MathUtils.clamp(position.y, min.y, max.y);
        camera.position.set(camX, camY, 0);
        if (shaker.isShaking())
            shaker.update(camera, camX, camY, delta);

        textCamera.update();
        camera.update();
    }

    public boolean shake() {
        if (!shaker.isShaking()) {
            shaker.shake(.2f, .05f);
            return true;
        }
        return false;
    }

    public void project(Batch batch) {
        batch.setProjectionMatrix(camera.combined);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        min.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);
        max.set((worldWidth / 2f) - (viewport.getWorldWidth() / 2f), worldHeight / 2f);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
