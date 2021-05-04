package com.thulium.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.thulium.util.Units;

public class PlayerInput implements InputProcessor {
	private Player player;

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
		case Keys.P:
			break;
		case Keys.K:
			player.attack(false);
			break;
		default:
			return false;
		}
		
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
			// player.getBody().setLinearVelocity(0, player.getBody().getLinearVelocity().y);
			player.applyOpposingForce();
			return true;
		case Keys.K:
			player.attack(true);
			break;
		}

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
}
