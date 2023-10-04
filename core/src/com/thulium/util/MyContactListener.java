package com.thulium.util;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.thulium.player.Player;

public class MyContactListener implements ContactListener {
	private int numFootContacts;
	private Player player;

	@Override
	public void beginContact(Contact contact) {
		final Fixture a = contact.getFixtureA();
		final Fixture b = contact.getFixtureB();

		if (collisionContains("foot", a, b)) {
			numFootContacts++;
		} if (collisionContains("squirrel", a, b) && collisionContains("entity", a, b)) { // TODO: Rename player's user data
			// System.out.println("Maybe colliding");
			System.out.println(a.getUserData() + ", " + b.getUserData());
			player.damage(1);
		}
	}

	@Override
	public void endContact(Contact contact) {
		Fixture a = contact.getFixtureA();
		Fixture b = contact.getFixtureB();

		if (collisionContains("foot", a, b))
			numFootContacts--;
	}

	/*
	 * Returns whether either collision object contains user data
	 */
	public boolean collisionContains(Object o, Fixture a, Fixture b) {
		return (a.getUserData() != null && a.getUserData().equals(o)
				|| (b.getUserData() != null && b.getUserData().equals(o)));
	}

	public boolean isOnGround() {
		// System.out.println(numFootContacts);
		return numFootContacts > 0;
	}

	public void setPlayer(Player player ) {
		this.player = player;
	}

	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
}