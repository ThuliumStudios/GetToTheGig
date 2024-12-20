package com.thulium.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.thulium.game.SpawnProperties;
import com.thulium.main.MainGame;
import com.thulium.util.Units;

import java.util.Arrays;

public class GameMap {
	private final OrthogonalTiledMapRenderer mapRenderer;
	private final TiledMap map;

    private final Array<SpawnProperties> enemies = new Array<>();
	private final Array<SpawnProperties> npcs = new Array<>();
	private final Array<SpawnProperties> items = new Array<>();
	private final Array<SpawnProperties> scenes = new Array<>();

	private final ParallaxScene parallax;

	public GameMap(MainGame game, Batch batch) {
        TmxMapLoader loader = new TmxMapLoader();
		map = loader.load("maps/map0x0.tmx");
		mapRenderer = new OrthogonalTiledMapRenderer(map, 1/64f, batch);

		parallax = new ParallaxScene();
		parallax.addBackgrounds(game.getAsset("maps/BG_Decor.png", Texture.class),
				game.getAsset("maps/Middle_Decor.png", Texture.class));
		// parallax.addForegrounds(game.getAsset("maps/whiteclouds.png", Texture.class));
	}

	public void render(OrthographicCamera camera, String... layers) {
		camera.position.set(camera.position.x, camera.position.y, 0);
		mapRenderer.setView(camera);

		mapRenderer.getBatch().begin();
		Arrays.stream(layers).forEach(s -> mapRenderer.renderTileLayer(getLayer(s)));
		mapRenderer.getBatch().end();
	}

	public TiledMapTileLayer getLayer(String layer) {
		return (TiledMapTileLayer) map.getLayers().get(layer);
	}
	
	public void renderBG(Batch batch, OrthographicCamera camera) {
		parallax.renderBG(batch, camera);
	}

	public void renderFG(Batch batch, OrthographicCamera camera) {
		parallax.renderFG(batch, camera);
	}

	public void createBox2dObjects(World world, BodyDef bodyDef, FixtureDef fixtureDef) {
		// Create Box2d bodies
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("platform");
		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < layer.getWidth(); x++) {
				Cell cell = layer.getCell(x, y);

				if (cell == null || cell.getTile() == null || !cell.getTile().getProperties().containsKey("collision"))
					continue;
				bodyDef.type = BodyType.StaticBody;
				bodyDef.position.set((x + .5f) / 2f, (y + .8f) / 2f);

				ChainShape cs = new ChainShape();
				Vector2[] v = new Vector2[5];
				v[0] = new Vector2((-1 / 2f), (-1 / 8f));
				v[1] = new Vector2((-1 / 2f), (1 / 8f));
				v[2] = new Vector2((1 / 2f), (1 / 8f));
				v[3] = new Vector2((1 / 2f), (-1 / 8f));
				v[4] = new Vector2(v[0]);

				Arrays.asList(v).forEach(vec -> vec.scl(.5f));

				cs.createChain(v);
				fixtureDef.friction = .05f;//.5f;
				fixtureDef.shape = cs;
				fixtureDef.filter.categoryBits = Units.GROUND_FLAG;
				fixtureDef.filter.maskBits = Units.ENTITY_FLAG | Units.PLAYER_FLAG | Units.ALL_FLAG;
				fixtureDef.isSensor = false;
				world.createBody(bodyDef).createFixture(fixtureDef);
				cs.dispose();
			}
		}

		// Create spawn items
		TiledMapTileLayer spawns = (TiledMapTileLayer) map.getLayers().get("spawns");
		for (int y = 0; y < spawns.getHeight(); y++) {
			for (int x = 0; x < spawns.getWidth(); x++) {
				Cell cell = spawns.getCell(x, y);
				if (cell != null) {
					SpawnProperties spawn = new SpawnProperties();
					spawn.setName(cell.getTile().getProperties().get("spawn", String.class));
					spawn.setX(x);
					spawn.setY(y);
					spawn.setWidth(cell.getTile().getProperties().get("width", Float.class));
					spawn.setHeight(cell.getTile().getProperties().get("height", Float.class));

					switch (cell.getTile().getProperties().get("type", String.class)) {
						case "enemy":
							spawn.setType(SpawnProperties.SpawnType.Enemy);
							enemies.add(spawn);
							break;
						case "npc":
							spawn.setType(SpawnProperties.SpawnType.Npc);
							npcs.add(spawn);
							break;
						case "item":
							spawn.setType(SpawnProperties.SpawnType.Item);
							items.add(spawn);
							break;
						case "scene":
							spawn.setType(SpawnProperties.SpawnType.SceneObject);
							scenes.add(spawn);
							break;
						default:
							break;
					}
				}
			}
		}
	}

//	public void createBox2dObjectsOLD(World world, BodyDef bodyDef, FixtureDef fixtureDef) {
//		// Create Box2d bodies
//		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("collision");
//		for (int y = 0; y < layer.getHeight(); y++) {
//			for (int x = 0; x < layer.getWidth(); x++) {
//				Cell cell = layer.getCell(x, y);
//
//				if (cell == null || cell.getTile() == null)
//					continue;
//				bodyDef.type = BodyType.StaticBody;
//				bodyDef.position.set((x + .5f) / 2f, (y + .8f) / 2f);
//
//
//				ChainShape cs = new ChainShape();
//				Vector2[] v = new Vector2[5];
//				v[0] = new Vector2((-1 / 2f), (-1 / 8f));
//				v[1] = new Vector2((-1 / 2f), (1 / 8f));
//				v[2] = new Vector2((1 / 2f), (1 / 8f));
//				v[3] = new Vector2((1 / 2f), (-1 / 8f));
//				v[4] = new Vector2(v[0]);
//
//				Arrays.asList(v).forEach(vec -> vec.scl(.5f));
//
//				cs.createChain(v);
//				fixtureDef.friction = .01f;//.5f;
//				fixtureDef.shape = cs;
//				fixtureDef.filter.categoryBits = Units.GROUND_FLAG;
//				fixtureDef.filter.maskBits = Units.ENTITY_FLAG | Units.PLAYER_FLAG | Units.ALL_FLAG;
//				fixtureDef.isSensor = false;
//				world.createBody(bodyDef).createFixture(fixtureDef);
//				cs.dispose();
//			}
//		}
//
//		// Create spawn items
//		TiledMapTileLayer spawns = (TiledMapTileLayer) map.getLayers().get("spawns");
//		for (int y = 0; y < spawns.getHeight(); y++) {
//			for (int x = 0; x < spawns.getWidth(); x++) {
//				Cell cell = spawns.getCell(x, y);
//				if (cell != null) {
//					SpawnProperties spawn = new SpawnProperties();
//					spawn.setName(cell.getTile().getProperties().get("spawn", String.class));
//					spawn.setX(x);
//					spawn.setY(y);
//					spawn.setWidth(cell.getTile().getProperties().get("width", Float.class));
//					spawn.setHeight(cell.getTile().getProperties().get("height", Float.class));
//
//					switch (cell.getTile().getProperties().get("type", String.class)) {
//						case "enemy":
//							spawn.setType(SpawnProperties.SpawnType.Enemy);
//							enemies.add(spawn);
//							break;
//						case "npc":
//							spawn.setType(SpawnProperties.SpawnType.Npc);
//							npcs.add(spawn);
//							break;
//						case "item":
//							spawn.setType(SpawnProperties.SpawnType.Item);
//							items.add(spawn);
//							break;
//						case "scene":
//							spawn.setType(SpawnProperties.SpawnType.SceneObject);
//							scenes.add(spawn);
//							break;
//						default:
//							break;
//					}
//				}
//			}
//		}
//	}

	public Array<SpawnProperties> get(SpawnProperties.SpawnType type) {
		switch (type) {
			case Enemy:
				return enemies;
			case Npc:
				return npcs;
			case Item:
				return items;
			case SceneObject:
				return scenes;
			default:
				return null;
		}
	}

	public <T> T getProperty(String property, Class<T> classType) {
		return map.getProperties().get(property, classType);
	}

	public void dispose() {
		map.dispose();
		mapRenderer.dispose();
	}
	
	/*
	 * Parallax background inner class
	 */
	private static class ParallaxScene {
		private final Array<TextureRegion> fgs;
        private final Array<TextureRegion> bgs;
		private final float mul = 512f;

		public ParallaxScene() {
			bgs = new Array<>();
			fgs = new Array<>();
		}

		public void renderBG(Batch batch, OrthographicCamera camera) {
			batch.setColor(1, 1, 1, 1f); // .25f alpha
			bgs.forEach(bg -> {
				int i = bgs.indexOf(bg, true);
				bg.setRegionX((int) (camera.position.x * Math.sqrt(mul) * i) % bg.getTexture().getWidth());
				bg.setRegionWidth(bg.getTexture().getWidth());

				batch.draw(bg, camera.position.x - camera.viewportWidth/2f, camera.position.y - camera.viewportHeight / 2f,
						Units.WIDTH * 1.25f, Units.HEIGHT * 1.25f);
			});
			batch.setColor(Color.WHITE);
		}

		public void renderFG(Batch batch, OrthographicCamera camera) {
			fgs.forEach(fg -> {
				fg.setRegionX((int) (camera.position.x * mul) % fg.getTexture().getWidth());
				fg.setRegionWidth(fg.getTexture().getWidth());

				// TODO: Delete
				fg.setRegionX((int) (camera.position.x * mul) % fg.getTexture().getWidth());
				fg.setRegionWidth(fg.getTexture().getWidth());

				batch.draw(fg, camera.position.x - camera.viewportWidth/2f, camera.position.y - camera.viewportHeight / 2f,
						Units.WIDTH, Units.HEIGHT);
			});
		}

		public void addBackgrounds(Texture... bgsToAdd) {
			Arrays.asList(bgsToAdd).forEach(bg -> {
				 bg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
				 bgs.add(new TextureRegion(bg));
			});
		}

		public void addForegrounds(Texture... fgsToAdd) {
			Arrays.asList(fgsToAdd).forEach(fg -> {
				fg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
				fgs.add(new TextureRegion(fg));
			});
		}
	}
}
