package com.thulium.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.thulium.game.PauseMenu;
import com.thulium.util.Jukebox;
import com.thulium.world.GameWorld;
import com.thulium.main.MainGame;

import java.util.Arrays;

public class GameScreen implements Screen {
	private GameWorld world;
	private PauseMenu pause;
	private final MainGame game;

	private boolean isPaused;
	
	public GameScreen(MainGame game) {
		this.game = game;
	}

	@Override
	public void show() {
		world = new GameWorld(game);
		pause = new PauseMenu(game.getSkin());

		// Set input processors
		InputMultiplexer input = new InputMultiplexer();
		input.setProcessors(pauseInput, pause.getStage(), world.getInputProcessors());
		Gdx.input.setInputProcessor(input);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(.1f, .1f, .1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// TODO: Either mute Jukebox or change song on pause
		if (isPaused) {
			pause.render(delta);
			pause.renderDebug(world.getDebug());
		} else {
			world.update(delta);
			world.render(game.getBatch(), delta);
		}
	}

	@Override
	public void resize(int width, int height) {
		world.resize(width, height);
		pause.resize(width, height);
	}

	@Override
	public void pause() {
		isPaused = true;
	}

	@Override
	public void resume() {}

	@Override
	public void hide() {
		isPaused = true;
	}

	@Override
	public void dispose() {
		world.dispose();
		pause.dispose();
	}

	public InputAdapter pauseInput = new InputAdapter() {
		@Override
		public boolean keyDown(int keycode) {
			if (keycode == Input.Keys.ESCAPE) {
				isPaused = !isPaused;
			}
			return false;
		}
	};
}