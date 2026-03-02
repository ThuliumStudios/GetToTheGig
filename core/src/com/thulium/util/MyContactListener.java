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
	private int numLedgeContacts;
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
			// Enemy enemy = a.getUserData() instanceof Enemy ? (Enemy) a.getUserData() : (Enemy) b.getUserData();
			Enemy enemy = getType(Enemy.class, a, b);
			if (collisionContains("player", a, b)) {
				player.damage(1);
			} else if (collisionContains("hit", a, b)) {
				System.out.println("Hitbox is hitting enemy!");
				enemy.die();
				world.hitPlayer();
			} else if (isType(PlayerProjectile.class, a, b)) {	// TODO: Add flag/filter to axe instead of explicit check
				enemy.die();
				// PlayerProjectile p = a.getUserData() instanceof PlayerProjectile ? (PlayerProjectile) a.getUserData() : (PlayerProjectile) b.getUserData();
				PlayerProjectile p = getType(PlayerProjectile.class, a, b);
				p.collide();
			}
		} if (collisionContains("hand", "ledge", a, b)) {
			numLedgeContacts++;
			player.setCanGrabLedge(numLedgeContacts > 0);
			player.setLedgePoint(getFixture("ledge", a, b).getBody().getTransform().getPosition());
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture a = contact.getFixtureA();
		Fixture b = contact.getFixtureB();

		if (collisionContains("foot", a, b))
			numFootContacts--;
		if (collisionContains("hand", "ledge", a, b)) {
			numLedgeContacts--;
			player.setCanGrabLedge(numLedgeContacts > 0);
			// player.setLedgePoint(contact.getWorldManifold().getPoints()[0]);
		}
	}



	public void preSolve(Contact contact, Manifold oldManifold) {
		final Fixture a = contact.getFixtureA();
		final Fixture b = contact.getFixtureB();

		if (collisionContains("player", "entity", a, b))
			System.out.println("Colliding with enemy");

		// Handle pass-through platforms
		else if (collisionContains("player", "platform", a, b)) {
			Fixture platform = getFixture("platform", a, b);
			if (player.getBody().getLinearVelocity().y > .001f &&
				player.getBody().getPosition().y - (player.getHeight() / 2f) < (platform.getBody().getPosition().y)) {
				contact.setEnabled(false);
			}
		}
	}

	public void postSolve(Contact contact, ContactImpulse impulse) {

	}

	/*
	Returns whether either collision object contains user data
	 */
	public boolean collisionContains(Object o, Fixture a, Fixture b) {
		return (a.getUserData() != null && a.getUserData().equals(o)
				|| (b.getUserData() != null && b.getUserData().equals(o)));
	}

	public boolean collisionContains(Object o1, Object o2, Fixture a, Fixture b) {
		return collisionContains(o1, a, b) && collisionContains(o2, a, b);
	}

	/*
	You MUST validate before calling
	 */
	public Fixture getFixture(Object o, Fixture a, Fixture b) {
		return a.getUserData() == o ? a : b;
	}

	public boolean isType(Class<?> clazz, Fixture a, Fixture b) {
		return clazz.isInstance(a.getUserData()) || clazz.isInstance(b.getUserData());
	}

	public <T> T getType(Class<?> clazz, Fixture a, Fixture b) {
		return (T) (clazz.isInstance(a.getUserData()) ? a.getUserData() : b.getUserData());
	}

	public boolean isOnGround() {
		return numFootContacts > 0;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
}