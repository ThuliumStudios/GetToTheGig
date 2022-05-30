package com.thulium.entity;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.thulium.util.Units;

public class Amp {
	private Sprite sprite;
	private Body body;

	private float originalMass;
	private boolean isPullingPlayer;
	private boolean isStateLocked;

	public Amp(TextureRegion region) {
		sprite = new Sprite(region);
		sprite.setSize(1, 1);
		sprite.setOriginCenter();
	}

	public void render(Batch batch) {
		sprite.draw(batch);
		sprite.setPosition((body.getPosition().x) - (sprite.getWidth() / 2f),
				(body.getPosition().y) - (sprite.getHeight() / 2f));
		sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
	}
	
	public void kick(float xMul, float yMul) {
		body.applyLinearImpulse(new Vector2(Units.ATTACK_FORCE).scl(xMul, yMul), body.getWorldCenter(), true);
	}

	public void pullPlayer(boolean isPullingPlayer) {
		setMass(isPullingPlayer ? 100 : 1);
		this.isPullingPlayer = isPullingPlayer;
	}

	public boolean isPullingPlayer() {
		return isPullingPlayer;
	}

	public void setMass(float massMul) {
		MassData massData = getBody().getMassData();
		massData.mass = originalMass * massMul;
		getBody().setMassData(massData);
	}

	public void setStateLocked(boolean isStateLocked) {
		this.isStateLocked = isStateLocked;
	}

	public boolean isStateLocked() {
		return isStateLocked;
	}

	public void dispose() {
	}

	public Body getBody() {
		return body;
	}

	public BodyDef getBodyDef(float x, float y) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		return bodyDef;
	}
	
	public void createBody(Body body, Object name, float width, float height, boolean hasFoot) {
		this.body = body;

		PolygonShape box = new PolygonShape();
		box.setAsBox((width), (height));
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = box;
		fixtureDef.filter.categoryBits = Units.ENTITY_FLAG;
		fixtureDef.filter.maskBits = Units.GROUND_FLAG | Units.ALL_FLAG;
		fixtureDef.filter.groupIndex = 1;
		fixtureDef.density = 1;
		body.createFixture(fixtureDef).setUserData(name);
		originalMass = body.getMass();

		// Add sensor
			// fixtureDef.isSensor = true;
			// body.createFixture(fixtureDef).setUserData("amp");

		box.dispose();
	}

	public void changeCollisionFilters(short categoryBits, short maskBits) {
		if (isStateLocked)
			return;
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
}
