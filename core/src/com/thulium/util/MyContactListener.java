package com.thulium.util;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.thulium.entity.Enemy;
import com.thulium.player.Player;
import com.thulium.player.PlayerProjectile;
import com.thulium.world.GameWorld;

public class MyContactListener implements ContactListener {
	private int numFootContacts;
	private Player player;

	private final GameWorld world;

	public MyContactListener(GameWorld world) {
		this.world = world;
	}

	@Override
	public void beginContact(Contact contact) {
		final Fixture a = contact.getFixtureA();
		final Fixture b = contact.getFixtureB();

		if (collisionContains("foot", a, b)) {
			numFootContacts++;
		} if (isType(Enemy.class, a, b)) {
			Enemy enemy = a.getUserData() instanceof Enemy ? (Enemy) a.getUserData() : (Enemy) b.getUserData();
			if (collisionContains("entity", a, b)) {
				player.damage(1);
			} else if (collisionContains("hit", a, b)) {
				System.out.println("Hitbox is hitting enemy!");
				enemy.die();
				world.hitPlayer();
			} else if (isType(PlayerProjectile.class, a, b)) {	// TODO: Add flag/filter to axe instead of explicit check
				enemy.die();
				PlayerProjectile p = a.getUserData() instanceof PlayerProjectile ? (PlayerProjectile) a.getUserData() : (PlayerProjectile) b.getUserData();
				p.collide();
			}
		} if (collisionContains("hit", a, b)) {
			System.out.println(a.getUserData() + ", " + b.getUserData());
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture a = contact.getFixtureA();
		Fixture b = contact.getFixtureB();

		if (collisionContains("foot", a, b))
			numFootContacts--;
	}

	/**
	 * Returns whether either collision object contains user data
	 */
	public boolean collisionContains(Object o, Fixture a, Fixture b) {
		return (a.getUserData() != null && a.getUserData().equals(o)
				|| (b.getUserData() != null && b.getUserData().equals(o)));
	}

	public boolean isType(Class<?> clazz, Fixture a, Fixture b) {
		return clazz.isInstance(a.getUserData()) || clazz.isInstance(b.getUserData());
	}

	public boolean isOnGround() {
		return numFootContacts > 0;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
}