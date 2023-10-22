package com.thulium.main;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.thulium.screen.GameScreen;
import com.thulium.util.Jukebox;
import com.thulium.util.SpriteAccessor;

import java.util.Arrays;
import java.util.List;

public class MainGame extends Game {
	private AssetManager assets;
	private Skin skin;
	private Batch batch;

	private Jukebox jukebox;

	@Override
	public void create() {
		batch = new SpriteBatch();
		
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		jukebox = new Jukebox();

		Tween.registerAccessor(Sprite.class, new SpriteAccessor());

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

		// Load backgrounds
		List.of("BG_Decor", "Ground", "Middle_Decor", "snowymountains", "trees_fg", "whiteclouds")
				.forEach(img -> assets.load("maps/" + img + ".png", Texture.class));

		// Load atlas files
		List.of("coon", "hud", "squirrel")
				.forEach(atlas -> assets.load("img/" + atlas + ".atlas", TextureAtlas.class));

		// Load music
		List.of("level_1")
				.forEach(m -> assets.load("raw/" + m + ".ogg", Music.class));

		assets.finishLoading();
	}

	@Override
	public void dispose() {
		super.dispose();
		assets.dispose();
		batch.dispose();
		skin.dispose();
	}
}
