package com.thulium.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class PlayerInfo {
	private Label fps;
	private Label velocity;
	
	private Stage stage;
	
	private Player player;

	public PlayerInfo(Player player, Skin skin) {
		this.player = player;
		stage = new Stage();
		
		fps = new Label("" + Gdx.graphics.getFramesPerSecond(), skin);
		velocity = new Label("" + player.getBody().getLinearVelocity(), skin);
		
		Table table = new Table(skin);
		table.setFillParent(true);
		table.defaults().expandX().left();
		
		table.add(fps).row();
		table.add(velocity);
		
		stage.addActor(table);
	}
	
	public void render(float delta) {
		fps.setText("" + Gdx.graphics.getFramesPerSecond());
		velocity.setText(roundedText());
		
		stage.act(delta);
		stage.draw();
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
