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
	private final Map<Integer, Integer> controls = new HashMap<>();
	public static final int PAUSE = Keys.ESCAPE;
	public static final int WALK_LEFT = Keys.A;
	public static final int WALK_RIGHT = Keys.D;
	public static final int JUMP = Keys.W;
	public static final int FALL = Keys.S;
	public static final int POWERSLIDE = Keys.SHIFT_RIGHT;
	public static final int DEBUG = Keys.F12;
	public static final int ATTACK = Keys.K;

	private final Map<Integer, String> keysDown = new HashMap<>();
	private final StringBuilder string = new StringBuilder();

	private final Player player;

	public PlayerInput(Player player) {
		this.player = player;

		// Map controls based on settings. TODO: Save default controls to settings file, load from settings
		controls.putIfAbsent(Keys.ESCAPE, PAUSE);
		controls.putIfAbsent(Keys.A, WALK_LEFT);
		controls.putIfAbsent(Keys.LEFT, WALK_RIGHT);
		controls.putIfAbsent(Keys.D, WALK_RIGHT);
		controls.putIfAbsent(Keys.RIGHT, WALK_RIGHT);
		controls.putIfAbsent(Keys.W, JUMP);
		controls.putIfAbsent(Keys.UP, JUMP);
		controls.putIfAbsent(Keys.S, FALL);
		controls.putIfAbsent(Keys.DOWN, FALL);
		controls.putIfAbsent(Keys.SPACE, JUMP);
		controls.putIfAbsent(Keys.SHIFT_RIGHT, POWERSLIDE);
		controls.putIfAbsent(Keys.SHIFT_LEFT, POWERSLIDE);
		controls.putIfAbsent(Keys.K, ATTACK);
		controls.putIfAbsent(Keys.F12, DEBUG);
	}

	@Override
	public boolean keyDown(int keycode) {
		switch(controls.getOrDefault(keycode, Keys.UNKNOWN)) {
			case WALK_LEFT:		// Walk left
				player.setXVelocity(-Units.MAX_VELOCITY);
				player.setFlipState(true);
				break;
			case WALK_RIGHT:	// Walk right
				player.setXVelocity(Units.MAX_VELOCITY);
				player.setFlipState(false);
				break;
			case JUMP:			// Jump
				player.jump();
				break;
			case POWERSLIDE:	// Powerslide
				if (keyIsDown(Keys.A, Keys.D))
					player.powerslide();
				break;
            case PAUSE:			// Pause
				player.togglePause();
				break;
			case DEBUG:		// Debug Box2D shapes
				System.out.println("Toggling debug");
				player.toggleDebug();
				break;
			case ATTACK:		// Attack
				player.charge();
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
		switch (controls.getOrDefault(keycode, Keys.UNKNOWN)) {
			case WALK_LEFT:
				if (!keyIsDown(WALK_RIGHT)) {
					player.setXVelocity(0);
					player.applyOpposingForce();
				}
				break;
			case WALK_RIGHT:
				if (!keyIsDown(WALK_LEFT)) {
					player.setXVelocity(0);
					player.applyOpposingForce();
				}
				break;
			case ATTACK:	// Release attack
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
}