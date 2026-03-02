package com.thulium.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.GL20;
import com.thulium.game.PauseMenu;
import com.thulium.input.PlayerInput;
import com.thulium.util.Jukebox;
import com.thulium.world.GameWorld;
import com.thulium.main.MainGame;

public class GameScreen implements Screen {
	public enum GameState { Running, Paused, Stopped }
	public static GameState state = GameState.Running;
	private Jukebox jukebox;
    private GameWorld world;
	private PauseMenu pause;

	private final MainGame game;

	public GameScreen(MainGame game) {
		this.game = game;
	}

	@Override
	public void show() {
		world = new GameWorld(game);
		pause = new PauseMenu(game.getSkin());
		jukebox = Jukebox.getInstance();

        PlayerInput input = new PlayerInput(world.getPlayer());
		Controllers.addListener(input);
		Gdx.input.setInputProcessor(new InputMultiplexer(pause.getStage(), input));

		jukebox.setVolume(0); // TODO: Delete
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(.1f, .1f, .1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		switch (state) {
			case Running -> {
				// jukebox.setVolume(1); // TODO: Un-comment
				world.update(delta);
				world.render(game.getBatch(), delta);
			} case Paused -> {
				// jukebox.setVolume(.4f); // TODO: Un-comment
				pause.render(delta);
				pause.renderDebug(world.getDebug());
			} case Stopped -> {
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		world.resize(width, height);
		pause.resize(width, height);
	}

	@Override
	public void pause() {
		state = GameState.Paused;
		jukebox.pause();
	}

	@Override
	public void resume() {
		state = GameState.Running;
		jukebox.resume();
	}

	@Override
	public void hide() {
		state = GameState.Paused;
		jukebox.pause();
	}

	@Override
	public void dispose() {
		state = GameState.Stopped;
		jukebox.stop();
		world.dispose();
		pause.dispose();
	}
}