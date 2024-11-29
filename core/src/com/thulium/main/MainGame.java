package com.thulium.main;

import aurelienribon.tweenengine.Tween;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.ray3k.stripe.FreeTypeSkinLoader;
import com.thulium.screen.GameScreen;
import com.thulium.util.SpriteAccessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainGame extends Game {
	public enum Prefs {
		world("world"),
		settings("settings");

		final Preferences prefs;
		final Map<String, Object> setting = new HashMap<>();
		Prefs(String prefs) {
			this.prefs = Gdx.app.getPreferences(prefs);
		}

		public void saveValue(String field, Object value) {
			setting.put(field, value);
			prefs.put(setting);
			prefs.flush();
		}
	}
	private AssetManager assets;
	private Skin skin;
	private Batch batch;

	@Override
	public void create() {
		// Pre-check initialization
		Preferences worldPrefs = Gdx.app.getPreferences("world");
		if (!worldPrefs.contains("x")) {
			saveValue("x", 2.5f, Prefs.world);
			saveValue("y", 2.5f, Prefs.world);
		}

		batch = new SpriteBatch();
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

		// Load sprite sheets (.atlas files)
		List.of("axe", "coon", "hud", "items", "particles", "player", "playerwaxe", "scene", "squirrel")
				.forEach(atlas -> assets.load("img/" + atlas + ".atlas", TextureAtlas.class));

		// Load music
		List.of("level_1")
				.forEach(m -> assets.load("raw/" + m + ".ogg", Music.class));

		// Load sound effects
		List.of().forEach(s -> assets.load("raw/" + s + ".ogg", Sound.class));

		// Load skin with .ttf
		assets.setLoader(Skin.class, new FreeTypeSkinLoader(assets.getFileHandleResolver()));
		assets.load("skin/uiskin.json", Skin.class);
		assets.finishLoadingAsset("skin/uiskin.json");
		skin = assets.get("skin/uiskin.json");

		assets.finishLoading();
		assets.finishLoadingAsset("img/player.atlas");
	}

	public void saveValue(String field, Object value, Prefs prefs) {
		prefs.saveValue(field, value);
	}

	@Override
	public void dispose() {
		super.dispose();
		assets.dispose();
		batch.dispose();
		getScreen().dispose();
	}
}
