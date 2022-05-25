package com.thulium.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.thulium.entity.Amp;
import com.thulium.entity.Cable;
import com.thulium.util.Units;

import java.util.Arrays;
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
					player.powerslide();
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
				cable.getJoint().setMaxLength(Math.abs(player.getBody().getPosition().dst(amp.getBody().getPosition())));
				player.pullAmp(true);
				break;
			case Keys.P:
				break;
			case Keys.K:
				player.attack(false);
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
				player.attack(true);
				break;
			case Keys.O:
				cable.getJoint().setMaxLength(5);
				player.pullAmp(false);
				// amp.getBody().applyAngularImpulse(5, true);
				Vector2 impulse = new Vector2(0, 2);
				amp.getBody().applyLinearImpulse(impulse, amp.getBody().getWorldCenter(), true);
				break;
			default:
				break;
		}

		keysDown.removeValue(keycode, true);
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
				System.out.println("Key is down: " + key);
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


