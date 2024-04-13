package com.thulium.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.MassData;
import com.thulium.entity.AnimationWrapper;
import com.thulium.entity.Entity;
import com.thulium.entity.Priority;
import com.thulium.util.Units;

public class Player extends Entity {
	private boolean isMoving;
	private boolean isPaused;
	private boolean isOnGround;
	private boolean isDebugging;
	private boolean isPullingAmp;
	private boolean jumped;
	private boolean noJump = true;
	private float xVel;
	private int HP;

	private boolean isArmed = false;

	// TODO: Delete all variables below here. For testing purposes only
	private Sprite blood;
	private Animation<TextureRegion> bloodAnim;
	private float bloodStateTime;

	public Player(TextureAtlas atlas) {
		super(atlas, 1, 1);
		setOriginCenter();

		HP = 4;

		System.out.println("Comparing enums");
		System.out.println(Priority.High.compareTo(Priority.Low));
		System.out.println(Priority.Low.compareTo(Priority.High));

		setSize(3, 3);
		addAnimation("idle", new AnimationWrapper("idle", 1f, atlas, Priority.Bottom));
		addAnimation("run", new AnimationWrapper("run", 1/10f, atlas, Priority.Normal));
		addAnimation("attack", new AnimationWrapper("attack0", 1/13f, atlas, Priority.High));
		addAnimation("rare", new AnimationWrapper("rare", 1/2f, atlas, Priority.High));
		addAnimation("jump_up", new AnimationWrapper("jump_up", 1f, atlas, Priority.High));
		addAnimation("jump_down", new AnimationWrapper("jump_down", 1f, atlas, Priority.High));
		addAnimation("death", new AnimationWrapper("death", .15f, atlas, Priority.High));
		animate("idle");

		// TODO: Delete all variable declarations below here. For testing purposes only
		blood = new Sprite();
		blood.setBounds(-1, -1, getWidth(), getHeight());
		blood.setOriginCenter();
		bloodAnim = new Animation<>(.025f, atlas.findRegions("blood"));
		bloodStateTime = 1;
	}

	public void render(Batch batch) {
		super.render(batch);

		renderBlood(batch);
	}

	public void update(float delta) {
		super.update(delta);
		updateAnimation();

//		if (getAnimationName().equals("rare"))
//			chargeTime += delta;
		if (isPositionLocked()) {
			setPosition((getBody().getPosition().x) - (getWidth() / 2f),
					(getBody().getPosition().y) - (getHeight() / 2f));
		} else {
			setXVelocity(xVel);
		}
		if (isPullingAmp) {
			getBody().setTransform(getLockedPosition(), 0);
		}
	}
	
	public void attack(boolean attack) {
		attack = attack || !isArmed;


		if (!getAnimationName().contains("attack"))
			animate(attack || !isArmed ? "attack" : "rare");

		// setPositionLocked(!attack);
		// setXVelocity(0);
		setVelocity(0, getVelocity().y);
		getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
		getBody().setAngularVelocity(0);
	}

	// TODO: Delete this whole method
	private void renderBlood(Batch batch) {
		blood.setRegion(bloodAnim.getKeyFrame(bloodStateTime));
		blood.draw(batch);
		bloodStateTime += Gdx.graphics.getDeltaTime();
	}

	// @Override
	public void updateAnimation() {
		// Process run/stop animations
		if (getHP() < 1) {
			return;
		}
		if (isOnGround && Math.abs(getBody().getLinearVelocity().x) > .01f)
			animate("run");
		else if (inMargin(getBody().getLinearVelocity().x) && getAnimationName().equals("run"))
			animate("idle");

		if (!isOnGround) {
			if (getBody().getLinearVelocity().y > .1f)
				animate("jump_up");
			else if (getBody().getLinearVelocity().y < -1f)
				animate("jump_down");
		}

		// Check if player should be idle
		if (isOnGround) {
			if ((isAnimationFinished() && !Units.isLooping(getAnimationName())) ||
					(isAnimation("jump_down") || isAnimation("jump_up"))) {
				animate("idle");
			}
		}

		setPositionLocked(isAnimation("attack") && !isAnimationFinished());
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
		Vector2 force = getBody().getLinearVelocity().scl(5f, 1);//new Vector2(5 / getBody().getLinearVelocity().x, 0);
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

		if (damage > 0 && getHP() > 0) {
			bloodStateTime = 0;
			blood.setRotation(MathUtils.random(360));
			blood.setPosition(getX(), getY());
		}
	}

	public void switchStates(TextureAtlas atlas) {
		setAtlas(atlas);
	}

	public boolean collidesWith(Sprite sprite) {
		return getBoundingRectangle().overlaps(sprite.getBoundingRectangle());
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

	@Override
	public void setXVelocity(float x) {
		super.setXVelocity(x);
		xVel = x;
	}
}
