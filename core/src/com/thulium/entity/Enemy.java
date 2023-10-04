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
    private int limit = 5;
    private boolean left;
    private float speed = 3;

    private SpawnProperties properties;

    public Enemy(TextureAtlas atlas, SpawnProperties properties) {
        super(atlas);
        this.properties = properties;

        force = new Vector2();
        animate("run", .1f, true);
    }

    @Override
    public Body createBody(Body body, Object name, float width, float height, float x, float y, boolean hasFoot) {
        Body b = super.createBody(body, name, width, height, x, y, hasFoot);

        b.getFixtureList().forEach(f -> {
            f.setSensor(true);
            Filter filter = f.getFilterData();
            filter.categoryBits = Units.GROUND_FLAG;
            filter.maskBits = Units.ENTITY_FLAG | Units.ALL_FLAG;
        });

        b.setType(BodyDef.BodyType.KinematicBody);
        b.setAwake(true);
        b.getFixtureList().first().setUserData("squirrel");

        return b;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        // updateAnimation();


        if (getBody().getPosition().x < properties.getX() - limit) {
            left = false;
        }
        if (getBody().getPosition().x > properties.getX() + limit) {
            left = true;
        }
        setFlipState(!left);

        force.set(left ? -speed : speed, 0);

        getBody().setLinearVelocity(force);
    }
}
