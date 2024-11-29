package com.thulium.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.StringBuilder;
import com.thulium.player.Player;
import com.thulium.util.Units;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlayerInput implements InputProcessor {
	private final Map<Integer, String> keysDown = new HashMap<>();
	private final StringBuilder string = new StringBuilder();
	private String keyDownList;

	private final Player player;

	public PlayerInput(Player player) {
		this.player = player;
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Keys.LEFT:		// Walk left
			case Keys.A:
				player.setXVelocity(-Units.MAX_VELOCITY);
				player.setFlipState(true);
				break;
			case Keys.RIGHT:	// Walk right
			case Keys.D:
				player.setXVelocity(Units.MAX_VELOCITY);
				player.setFlipState(false);
				break;
			case Keys.UP:		// Jump
			case Keys.W:
			case Keys.SPACE:
				player.jump();
				break;
			case Keys.SHIFT_LEFT:// Powerslide
			case Keys.SHIFT_RIGHT:
				if (keyIsDown(Keys.A, Keys.D))
					player.powerslide();
				break;
            case Keys.ESCAPE:	// Pause
				player.setPaused();
				break;
			case Keys.F12:		// Debug Box2D shapes
				player.toggleDebug();
				break;
			case Keys.K:		// Attack
				player.charge();
				break;

			// Test cases
			case Keys.PLUS:
			case Keys.NUMPAD_ADD:
				player.damage(-1);
				break;
			case Keys.MINUS:
			case Keys.NUMPAD_SUBTRACT:
				player.damage(1);
				break;
			case Keys.NUM_0:
			case Keys.NUM_1:
			case Keys.NUM_2:
			case Keys.NUM_3:
			case Keys.NUM_4:
				player.setHP(keycode - 7);
				break;
			case Keys.NUMPAD_0:
			case Keys.NUMPAD_1:
			case Keys.NUMPAD_2:
			case Keys.NUMPAD_3:
			case Keys.NUMPAD_4:
				player.setHP(keycode - 144);
				break;
			default:
				break;
		}

		keysDown.put(keycode, Keys.toString(keycode));
		string.append("Key down: ").append(keysDown.get(keycode)).append(". All keys currently down: ");
		keysDown.values().forEach(string::append);
		System.out.println(string.toStringAndClear());
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
			case Keys.A:
			case Keys.LEFT:
				if (!keyIsDown(Keys.RIGHT)) {
					player.setXVelocity(0);
					player.applyOpposingForce();
				}
				break;
			case Keys.D:
			case Keys.RIGHT:
				if (!keyIsDown(Keys.LEFT)) {
					player.setXVelocity(0);
					player.applyOpposingForce();
				}
				break;
			case Keys.K:	// Release attack
				player.attack();
				break;
			case Keys.L:	// Push
				player.push();
				break;
			default:
				break;
		}

		string.append("Key up: ").append(keysDown.get(keycode)).append(". Keys still down: ");
		keysDown.remove(keycode);
		keysDown.values().forEach(string::append);
		System.out.println(string.toStringAndClear());
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
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
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
		// camera.zoom = MathUtils.clamp(camera.zoom + amountY * .1f, .1f, 10f);
		return false;
	}

	public boolean keyIsDown(int... keycodes) {
		return !Collections.disjoint(Collections.singletonList(keycodes), Collections.singletonList(keysDown.keySet()));
	}

	/**
	 *	Inner controller mapping
	 */

	/**
	 * 	Inner keyboard mapping
	 */
}


