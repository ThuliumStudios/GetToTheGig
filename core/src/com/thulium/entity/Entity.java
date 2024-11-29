package com.thulium.entity;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.thulium.util.Units;

public class Entity extends BaseEntity {
	private Body body;
	private boolean isPositionLocked;
	private float originalMass;

	private final Vector2 velocity;
	private final Vector2 lockedPosition;

	public Entity(TextureAtlas atlas, float width, float height) {
		super(atlas, width, height);
		velocity = new Vector2();
		lockedPosition = new Vector2();
	}

	public void render(Batch batch) {
		super.render(batch);
		setPosition((body.getPosition().x) - (getWidth() / 2f),
				(body.getPosition().y) - (getHeight() / 2f));
	}

	public void update(float delta) {
		super.update(delta);

		if (Math.abs(body.getLinearVelocity().x) <= Units.MAX_VELOCITY && !isPositionLocked)
			body.applyForceToCenter(velocity, true);
	}

	public void setPositionLocked(boolean isPositionLocked) {
		this.isPositionLocked = isPositionLocked;
		lockedPosition.set(body.getPosition());
	}

	public boolean isPositionLocked() {
		return isPositionLocked;
	}

	public Vector2 getLockedPosition() {
		return lockedPosition;
	}

	public BodyDef getBodyDef(float x, float y) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		bodyDef.fixedRotation = true;
		return bodyDef;
	}

	public Body getBody() {
		return body;
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public void setVelocity(float x, float y) {
		velocity.set(x, y);
	}
	
	public void setXVelocity(float x) {
		velocity.x = x * 10f;

	}

	public void applyOpposingForce() {
		body.applyLinearImpulse(new Vector2(-body.getLinearVelocity().x / 2f * body.getMass() * 1.5f, 0),
				body.getWorldCenter(), true);
	}

	public void die() {
		animate("death");
	}

	public boolean inMargin(float value) {
		return value < .01f && value > -.01f;
	}

	public float getOriginalMass() { return originalMass; }

	public void setOriginalMass(float originalMass) {
		this.originalMass = originalMass;
	}

	public void createBody(Body body, float width, float height) {
		createBody(body, "entity", width, height, width, height, true);
	}

	public Body createBody(Body body, float width, float height, float x, float y) {
		return createBody(body, "entity", width, height, x, y, true);
	}

	public Body createBody(Body body, Object name, float width, float height, boolean hasFoot) {
		return createBody(body, "entity", width, height, width, height, hasFoot);
	}

	public Body createBody(Body body, Object name, float width, float height, float x, float y, boolean hasFoot) {
		this.body = body;

		PolygonShape box = new PolygonShape();
		box.setAsBox((width), (height), new Vector2(x, y), 0);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = box;
		fixtureDef.filter.categoryBits = Units.ENTITY_FLAG;
		fixtureDef.filter.maskBits = Units.GROUND_FLAG | Units.PLAYER_FLAG;
		fixtureDef.filter.groupIndex = 2;
		fixtureDef.friction = 1;
		fixtureDef.density = 1.5f;
		body.createFixture(fixtureDef).setUserData(name);
		originalMass = body.getMass();

		if (hasFoot) {
			box.setAsBox((width * .9f),.15f, new Vector2(x, y - height * 1.5f), 0);
			fixtureDef.isSensor = true;
			body.createFixture(fixtureDef).setUserData("foot");
		}

		box.dispose();
		return body;
	}

	public void changeCollisionFilters(short categoryBits, short maskBits) {
		body.getFixtureList().forEach(f -> {
			Filter filter = f.getFilterData();
			filter.categoryBits = categoryBits;
			filter.maskBits = maskBits;
			body.getFixtureList().first().setFilterData(filter);
		});
	}

	public void changeCollisionGroup(short group) {
		body.getFixtureList().forEach(f -> {
			Filter filter = f.getFilterData();
			filter.groupIndex = group;
			body.getFixtureList().first().setFilterData(filter);
		});
	}
	
	public void dispose() {
		super.dispose();
	}
}
