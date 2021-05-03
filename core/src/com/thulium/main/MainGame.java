package com.thulium.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.thulium.screen.GameScreen;

public class MainGame extends Game {
	private AssetManager assets;
	private Skin skin;
	private Batch batch;

	@Override
	public void create() {
		batch = new SpriteBatch();
		
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

		// TODO: Not this
		assets = new AssetManager();
		assets.load("maps/snowymountains.png", Texture.class);
		assets.load("maps/whiteclouds.png", Texture.class);
		assets.finishLoading();
		
		setScreen(new GameScreen(this));
	}
	
	@Override
	public void render() {
		super.render();
	}
	
	public Batch getBatch() {
		return batch;
	}
	
	public Skin getSkin() {
		return skin;
	}
	
	public <T> T getAsset(String name, Class<T> type) {
		return assets.get(name, type);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		batch.dispose();
		skin.dispose();
	}
}
