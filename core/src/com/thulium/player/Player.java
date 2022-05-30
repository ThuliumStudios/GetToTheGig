package com.thulium.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.thulium.entity.Entity;
import com.thulium.util.Units;

public class Player extends Entity {
	private boolean isOnGround;
	private boolean isDebugging;
	private boolean isPullingAmp;
	private boolean jumped;
	private boolean noJump = true;

	private PlayerAxe axe;

	public Player(TextureAtlas atlas) {
		super(atlas);

		axe = new PlayerAxe();
		animate("idle", 1, true);
	}

	public void render(Batch batch) {
		super.render(batch);
		axe.draw(batch, Gdx.graphics.getDeltaTime());
		update(Gdx.graphics.getDeltaTime());
		updateAnimation();
	}

	public void update(float delta) {
		super.update(delta);
	}
	
	public void attack(boolean attack) {
		if (attack) {
			animate("attack", .25f, false);
		} else {
			animate("rare", .75f, true);
		}
	}

	@Override
	public void animate(String animationName, float speed, boolean looping) {
		super.animate(animationName, speed, looping);
		axe.animate(animationName, speed, looping);
	}

	public void jump() {
		if (!isOnGround && noJump)
			return;

		float modifier = 1;
		getBody().setLinearVelocity(getBody().getLinearVelocity().x, Units.JUMP * modifier);
		// pullAmp(false);
		jumped = !isOnGround;
	}

	public void powerslide() {
		Vector2 force = new Vector2(5 / getBody().getLinearVelocity().x, 0);
		setXVelocity(0);

		getBody().applyLinearImpulse(force, getBody().getWorldCenter(), true);
		applyOpposingForce();
	}

	public void setOnGround(boolean isOnGround) {
		this.isOnGround = isOnGround;
		if (isOnGround)
			jumped = false;
	}

	public void pullAmp(boolean isPullingAmp) {
		setMass(isPullingAmp && isOnGround ? 100 : 1);
		this.isPullingAmp = (isPullingAmp && isOnGround);
	}

	public boolean isPullingAmp() {
		return isPullingAmp;
	}

	public void setMass(float massMul) {
		MassData massData = getBody().getMassData();
		massData.mass = getOriginalMass() * massMul;
		getBody().setMassData(massData);
	}
	
	public boolean isDebugging() {
		return isDebugging;
	}

	public void setDebugging() {
		isDebugging = !isDebugging;
	}

	public void dispose() {
		super.dispose();
	}

	@Override
	public void setFlipState(boolean isFlipped) {
		super.setFlipState(isFlipped);
		axe.flip(isFlipped);
	}

	// TODO: Delete
	private class PlayerAxe {
		private TextureAtlas atlas;
		private Sprite sprite;
		private boolean flip;

		private Animation<TextureRegion> animation;

		public PlayerAxe() {
			atlas = new TextureAtlas(Gdx.files.internal("img/axe.atlas"));
			sprite = new Sprite(atlas.findRegion("attack", 1));
			this.atlas = atlas;
			sprite.setSize(152/96f, 152/96f);

			animation = new Animation<TextureRegion>(.25f, atlas.findRegions("axe"));
			animation.setPlayMode(Animation.PlayMode.NORMAL);
		}

		public void draw(Batch batch, float delta) {
			sprite.setOriginCenter();
			sprite.setRegion(animation.getKeyFrame(getStateTime()));
			sprite.setPosition(getX() + (getWidth() / 2f) - sprite.getWidth()/2f, getY());
			sprite.flip(flip, false);

			sprite.draw(batch);
			update(delta);
		}

		public void animate(String name, float speed, boolean looping) {
			animation = new Animation<TextureRegion>(speed, atlas.findRegions(name));
			animation.setPlayMode(looping ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL);
		}

		public void flip(boolean flip) {
			this.flip = flip;
		}

		public void dispose() {
			atlas.dispose();
		}
	}
}
