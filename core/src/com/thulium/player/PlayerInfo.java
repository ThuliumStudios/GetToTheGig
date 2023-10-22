package com.thulium.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.stream.IntStream;

public class PlayerInfo {
	private Label fps;
	private Label velocity;
	private Label status;

	// HUD objects
	private Table hudTable;
	private Stage stage;
	private Image face;
	private Image plectrum;

	private TextureAtlas hudAtlas;

	private int hearts;
	private Player player;

	public PlayerInfo(Player player, TextureAtlas hudAtlas, Skin skin) {
		this.player = player;
		this.hudAtlas = hudAtlas;
		stage = new Stage();
		
		fps = new Label("" + Gdx.graphics.getFramesPerSecond(), skin);
		velocity = new Label("" + player.getBody().getLinearVelocity(), skin);

		status = new Label("", skin);
		status.setFontScale(5);
		status.setPosition(stage.getWidth() / 4f, stage.getHeight() / 2f);

		hearts = player.getHP();
		face = new Image(hudAtlas.findRegion("hudface", player.getHP()));
		plectrum = new Image(hudAtlas.findRegion("hp"));

		hudTable = new Table(skin);
		hudTable.defaults().size(stage.getWidth() / 16f, stage.getWidth() / 16f).grow();
		hudTable.add(face).pad(stage.getHeight() / 32f);
		for (int i = 0; i < player.getHP(); i++)
			hudTable.add(new Image(hudAtlas.findRegion("hp"))).size(Value.percentWidth(3));

		Table table = new Table(skin);
		table.setFillParent(true);
		table.defaults().expandX().top().left();

		table.add(hudTable).expand().row();
		table.add(fps).expand().row();
		table.add(velocity);

		stage.addActor(table);
		stage.addActor(status);
	}
	
	public void render(float delta) {
		fps.setText("" + Gdx.graphics.getFramesPerSecond());
		velocity.setText(roundedText());
		
		//stage.act(delta);
		stage.draw();
	}

	public void update(float delta) {
		stage.act(delta);
		setHearts();
	}

	public void setHearts() {
		if (hearts == player.getHP())
			return;

		face.setDrawable(new TextureRegionDrawable(hudAtlas.findRegion("hudface", player.getHP())));
		IntStream.range(1, 5)
				.forEach(i -> hudTable.getCells().get(i).setActor(new Image(hudAtlas.findRegion("hp_empty"))));
		IntStream.range(1, player.getHP() + 1)
				.forEach(i -> hudTable.getCells().get(i).setActor(new Image(hudAtlas.findRegion("hp"))));
		hearts = player.getHP();
	}

	public void setStatus(String status) {
		this.status.setText(status);
	}
	
	public String roundedText() {
		Vector2 vel = player.getBody().getLinearVelocity();
		return String.format("%.2f", vel.x) + ", " + String.format("%.2f", vel.y);
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public void dispose() {
		stage.dispose();
	}
}
