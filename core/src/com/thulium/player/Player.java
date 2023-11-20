package com.thulium.player;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Elastic;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.thulium.entity.AnimationWrapper;
import com.thulium.entity.Entity;
import com.thulium.entity.Priority;
import com.thulium.util.SpriteAccessor;
import com.thulium.util.Units;

public class Player extends Entity {
	private boolean isPaused;
	private boolean isOnGround;
	private boolean isDebugging;
	private boolean isPullingAmp;
	private boolean jumped;
	private boolean noJump = true;
	private float chargeTime;
	private int HP;

	public Player(TextureAtlas atlas) {
		super(atlas, 1, 1);
		setOriginCenter();

		HP = 4;

		System.out.println("Comparing enums");
		System.out.println(Priority.High.compareTo(Priority.Low));
		System.out.println(Priority.Low.compareTo(Priority.High));

		setSize(3, 3);
		addAnimation("idle", new AnimationWrapper("idle", 1f, atlas, Priority.Bottom));
		addAnimation("run", new AnimationWrapper("run", 1f, atlas, Priority.Normal));
		addAnimation("attack", new AnimationWrapper("attack", .1f, atlas, Priority.High));
		addAnimation("rare", new AnimationWrapper("attack", .5f, atlas, Priority.High));
		addAnimation("jump_up", new AnimationWrapper("attack", 1f, atlas, Priority.High));
		addAnimation("jump_down", new AnimationWrapper("attack", 1f, atlas, Priority.High));
		animate("idle");
	}

	public void render(Batch batch) {
		super.render(batch);
	}

	public void update(float delta) {
		super.update(delta);
		updateAnimation();

		if (getAnimationName().equals("rare"))
			chargeTime += delta;
		if (isPositionLocked()) {
			setPosition((getBody().getPosition().x) - (getWidth() / 2f),
					(getBody().getPosition().y) - (getHeight() / 2f));
		}
		if (isPullingAmp) {
			getBody().setTransform(getLockedPosition(), 0);
		}
	}
	
	public void attack(boolean attack) {
		chargeTime = 0;
		animate(attack ? "attack" : "rare");
		setPositionLocked(!attack);
	}

	// @Override
	public void updateAnimation() {
		if (!isAnmationFinished())
			return;

			// Process jump animations
//		if (body.getLinearVelocity().y > .1f)
//			animate("jump_up", 1, true);
//		else if (body.getLinearVelocity().y < - 1f)
//			animate("jump_down", 1, true);
		if (getAnimationName().equals("jump_down") || getAnimationName().equals("jump_up")) {
			animate("idle");
		}

		// Process run/stop animations
		if (getBody().getLinearVelocity().y == 0 && Math.abs(getBody().getLinearVelocity().x) > .01f)
			animate("run");
		else if (inMargin(getBody().getLinearVelocity().x) && getAnimationName().equals("run"))
			animate("idle");

		//super.updateAnimation();
		if (getBody().getLinearVelocity().y > .1f)
			animate("jump_up");
		else if (getBody().getLinearVelocity().y < - 1f)
			animate("jump_down");

		if (isAnmationFinished() && !Units.isLooping(getAnimationName())) {
			animate("idle");
		}
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
	}

	public void jump() {
		if (!isOnGround && noJump) {
			System.out.println("cant jump because");
			System.out.println("is on ground: " + isOnGround + " and has jump? " + !noJump);
			System.out.println();
			return;
		}

		float modifier = 1;
		getBody().setLinearVelocity(getBody().getLinearVelocity().x, Units.JUMP * modifier);
		// pullAmp(false);
		jumped = !isOnGround;
	}

	public void push() {

	}

	public void powerslide() {
		Vector2 force = getBody().getLinearVelocity().scl(1.5f, 1);//new Vector2(5 / getBody().getLinearVelocity().x, 0);
		setXVelocity(0);

		getBody().applyLinearImpulse(force, getBody().getWorldCenter(), true);
		applyOpposingForce();
	}

	public void setOnGround(boolean isOnGround) {
		this.isOnGround = isOnGround;
		if (isOnGround)
			jumped = false;
//
//		if (isOnGround && overrideAnimation())
//			setPositionLocked(true);
	}

	public boolean canPullAmp() {
		return isOnGround;
	}

	public void pullAmp(boolean isPullingAmp) {
		// setMass(isPullingAmp && isOnGround ? 100 : 1);
		setPositionLocked(isPullingAmp);
		setFriction(isPullingAmp && isOnGround ? Float.MAX_VALUE : 1);
		this.isPullingAmp = (isPullingAmp && isOnGround);
	}

	public boolean isPullingAmp() {
		return isPullingAmp;
	}

	public float getChargeTime() {
		return chargeTime;
	}

	public void setFriction(float friction) {
		getBody().getFixtureList().forEach(f -> {
			f.setFriction(friction);
		});
//		MassData massData = getBody().getMassData();
//		massData.mass = getOriginalMass() * massMul;
//		getBody().setMassData(massData);
	}

	public void setOriginalMass(float mass) {
		MassData massData = getBody().getMassData();
		massData.mass = getOriginalMass() * mass;
		getBody().setMassData(massData);
		super.setOriginalMass(getOriginalMass() * mass);
	}

	public void damage(int damage) {
		HP = MathUtils.clamp(HP - damage, 0, 4);
	}

	public int getHP() {
		return HP;
	}

	public void setHP(int HP) {
		this.HP = HP;
	}
	
	public boolean isDebugging() {
		return isDebugging;
	}

	public void setDebugging() {
		isDebugging = !isDebugging;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void setPaused() {
		isPaused = !isPaused;
	}

	public void dispose() {
		super.dispose();
	}

	@Override
	public void setFlipState(boolean isFlipped) {
		super.setFlipState(isFlipped);
	}
}
