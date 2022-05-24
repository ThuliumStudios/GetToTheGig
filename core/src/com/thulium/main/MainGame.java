package com.thulium.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.thulium.screen.GameScreen;

import java.util.Arrays;

public class MainGame extends Game {
	private AssetManager assets;
	private Skin skin;
	private Batch batch;

	@Override
	public void create() {
		batch = new SpriteBatch();
		
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

		loadAssets();
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

	public void loadAssets() {
		assets = new AssetManager();

		String[] mapBg = {"snowymountains", "whiteclouds", "trees_fg", "Ground"};
		Arrays.asList(mapBg).forEach(img -> assets.load("maps/" + img + ".png", Texture.class));

		assets.finishLoading();
	}

	@Override
	public void dispose() {
		super.dispose();
		batch.dispose();
		skin.dispose();
	}
}
