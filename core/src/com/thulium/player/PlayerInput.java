package com.thulium.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.thulium.entity.Amp;
import com.thulium.entity.Cable;
import com.thulium.util.Units;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

public class PlayerInput implements InputProcessor {
	private Array<Integer> keysDown = new Array<>();
	private Player player;
	private Cable cable;
	private Amp amp;

	public PlayerInput(Player player) {
		this.player = player;
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO: Push/pull amp
		switch (keycode) {
			case Keys.LEFT:
			case Keys.A:
				player.setXVelocity(-Units.MAX_VELOCITY);
				player.setFlipState(true);
				break;
			case Keys.RIGHT:
			case Keys.D:
				player.setXVelocity(Units.MAX_VELOCITY);
				player.setFlipState(false);
				break;
			case Keys.UP:
			case Keys.W:
			case Keys.SPACE:
				player.jump();
				break;
			case Keys.SHIFT_LEFT:
			case Keys.SHIFT_RIGHT:
				if (keyIsDown(Keys.A, Keys.D))
					// player.powerslide();
				break;
			case Keys.PLUS:
				break;
			case Keys.MINUS:
				break;
			case Keys.TAB:
				break;
			case Keys.ENTER:
				break;
			case Keys.F12:
				player.setDebugging();
				break;
			case Keys.O:
				// player.changeCollisionGroup((short) 2);
				cable.getJoint().setMaxLength(Math.abs(player.getBody().getPosition().dst(amp.getBody().getPosition())));
				if (player.getBody().getPosition().y > amp.getBody().getPosition().y + 1) {
					player.pullAmp(true);
					amp.changeCollisionFilters(Units.ALL_FLAG, (short) 0);
					amp.changeCollisionGroup((short) 1);
					amp.setStateLocked(true);
				} else if (amp.getBody().getPosition().y > player.getBody().getPosition().y + 1) {
					amp.pullPlayer(true);
				}
				break;
			case Keys.P:
				if (cable.isConnected()) {
					cable.setConnected(false);
				} else if (player.getBody().getPosition().dst(amp.getBody().getPosition()) < 1) {
					cable.setConnected(true);
				}
				break;
			case Keys.K:
				// player.attack(false);
				player.attack(true);
				break;
			default:
				break;
		}

		keysDown.add(keycode);
		System.out.print("Key down: " + keycode + ". Keys still down: ");
		keysDown.forEach(k -> System.out.print(k + ", "));
		System.out.println();
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
			case Keys.A:
			case Keys.D:
			case Keys.LEFT:
			case Keys.RIGHT:
				player.setXVelocity(0);
				player.applyOpposingForce();
				break;
			case Keys.K:
				// player.attack(true);
				amp.kick(player.isFlipped() ? -1 : 1, 0, 0);
				break;
			case Keys.O:
				cable.getJoint().setMaxLength(5);
				Vector2 impulse = new Vector2(0, Units.JUMP / 2f);

				if (player.isPullingAmp()) {
					player.pullAmp(false);
					amp.setStateLocked(false);
					amp.changeCollisionFilters(Units.ALL_FLAG, (short) (Units.GROUND_FLAG | Units.ALL_FLAG));
					amp.changeCollisionGroup((short) 1);
					amp.getBody().applyLinearImpulse(impulse, amp.getBody().getWorldCenter(), true);
				} else if (amp.isPullingPlayer()) {
					amp.pullPlayer(false);
					player.getBody().applyLinearImpulse(impulse.scl(.25f), player.getBody().getWorldCenter(), true);
				}


//				if (player.isPullingAmp()) {
//					player.pullAmp(false);
//					amp.setStateLocked(false);
//					amp.changeCollisionFilters(Units.ALL_FLAG, (short) (Units.GROUND_FLAG | Units.ALL_FLAG));
//					amp.changeCollisionGroup((short) 1);
//
//					System.out.println("No longer pulling amp");
//				}
				// player.changeCollisionGroup((short) 1);
				// amp.getBody().applyAngularImpulse(5, true);
				break;
			default:
				break;
		}

		keysDown.forEach(k -> keysDown.removeValue(k, true));
		// keysDown.removeValue(keycode, true);
		System.out.print("Key up: " + keycode + ". Keys still down: ");
		keysDown.forEach(k -> System.out.print(k + ", "));

		System.out.println();
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}

	public boolean keyIsDown(int keycode) {
		return keysDown.contains(keycode, true);
	}

	public boolean keyIsDown(int... keycodes) {
		for (int key : keysDown) {
			if (IntStream.of(keycodes).parallel().filter(i -> i == key).findAny().isPresent()) {
				return true;
			}
		}
		return false;
	}

	public void setCable(Cable cable) {
		this.cable = cable;
	}

	public void setAmp(Amp amp) {
		this.amp = amp;
	}
}


