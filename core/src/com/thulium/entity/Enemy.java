package com.thulium.entity;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.thulium.game.SpawnProperties;
import com.thulium.util.Units;

public class Enemy extends Entity {
    private Vector2 force;
    private boolean left;
    private boolean isAlive;
    private boolean isDestroyed;
    private int limit = 5;
    private float speed = 3;

    private SpawnProperties properties;

    public Enemy(TextureAtlas atlas, SpawnProperties properties) {
        super(atlas, properties.getWidth(), properties.getHeight());
        this.properties = properties;

        isAlive = true;
        force = new Vector2();
        addAnimation("run", new AnimationWrapper("run", .1f, atlas, Priority.High));
        addAnimation("death", new AnimationWrapper("death", .05f, atlas, Priority.Top));
        animate("run");
    }

    @Override
    public Body createBody(Body body, Object name, float width, float height, float x, float y, boolean hasFoot) {
        Body b = super.createBody(body, name, width, height, x, y, hasFoot);

        b.getFixtureList().forEach(f -> {
            f.setSensor(true);
            Filter filter = f.getFilterData();
            filter.categoryBits = Units.ENTITY_FLAG;
            filter.maskBits = Units.PLAYER_FLAG | Units.ALL_FLAG;
        });

        b.setType(BodyDef.BodyType.KinematicBody);
        b.setAwake(true);
        b.getFixtureList().first().setUserData(this);

        return b;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        // updateAnimation();

        if (getBody().getPosition().x < properties.getX() / 2f - limit) {
            left = false;
        }
        if (getBody().getPosition().x > properties.getX() / 2f + limit) {
            left = true;
        }
        setFlipState(!left);

        force.set(isAlive ? (left ? -speed : speed) : 0, 0);
        getBody().setLinearVelocity(force);

    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void destroyBody() {
        isDestroyed = true;
    }

    @Override
    public void die() {
        super.die();

        isAlive = false;
        animate("death");
    }
}
