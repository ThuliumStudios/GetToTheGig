package com.thulium.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.thulium.entity.Amp;
import com.thulium.entity.Cable;
import com.thulium.util.Units;

import java.util.stream.IntStream;

public class PlayerInput implements InputProcessor {
	private Array<Integer> keysDown = new Array<>();
	private Player player;
	private Cable cable;
	private Amp amp;
	private OrthographicCamera camera;

	public PlayerInput(Player player, OrthographicCamera camera) {
		this.player = player;
		this.camera = camera;
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO: Push/pull amp
		switch (keycode) {
			case Keys.LEFT:
			case Keys.A:
				if (!player.isPositionLocked())
					player.setXVelocity(-Units.MAX_VELOCITY);
				player.setFlipState(true);
				break;
			case Keys.RIGHT:
			case Keys.D:
				if (!player.isPositionLocked())
					player.setXVelocity(Units.MAX_VELOCITY);
				player.setFlipState(false);
				break;
			case Keys.UP:
				break;
			case Keys.W:
			case Keys.SPACE:
				if (player.getAnimationName().equals("rare")) {

				} else {
					player.jump();
				}
				break;
			case Keys.SHIFT_LEFT:
			case Keys.SHIFT_RIGHT:
				if (keyIsDown(Keys.A, Keys.D))
					player.powerslide();
				break;
			case Keys.PLUS:
			case Keys.NUMPAD_ADD:
				player.damage(-1);
				break;
			case Keys.MINUS:
			case Keys.NUMPAD_SUBTRACT:
				player.damage(1);
				break;
			case Keys.TAB:
				break;
			case Keys.ENTER:
				break;
			case Keys.ESCAPE:
				player.setPaused();
				break;
			case Keys.F12:
				player.setDebugging();
				break;
			case Keys.O:
				cable.getJoint().setMaxLength(Math.abs(player.getBody().getPosition().dst(amp.getBody().getPosition())));
				if (player.getBody().getPosition().y > amp.getBody().getPosition().y + 1 && player.canPullAmp()) {
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
				player.attack(false);
				// player.attack(true);
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

		keysDown.add(keycode);
//		System.out.print("Key down: " + keycode + ". Keys still down: ");
//		keysDown.forEach(k -> System.out.print(k + ", "));
//		System.out.println();
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
				float force = player.getChargeTime();
				player.attack(true);
				if (player.getBody().getPosition().dst(amp.getBody().getPosition()) < 1.5f) {
					int sign = player.isFlipped() ? -1 : 1;
					float chargeMul = MathUtils.clamp(force, 0, Units.MAX_CHARGE) / Units.MAX_CHARGE;
					float xMul = sign * chargeMul;
					float yMul = chargeMul;
					if (keyIsDown(Keys.W, Keys.UP)) {
						yMul *= 1.25f * chargeMul;
						xMul = 0;
					}
					xMul *= (1 / force);
					amp.kick(xMul, yMul);
				}
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
			case Keys.L:
				player.push();
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
		camera.zoom = MathUtils.clamp(camera.zoom += -amountY * .1f, .1f, 10f);
		return false;
	}

	public boolean keyIsDown(int... keycodes) {
		for (int key : keysDown) {
			if (IntStream.of(keycodes).filter(i -> i == key).findAny().isPresent()) {
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


