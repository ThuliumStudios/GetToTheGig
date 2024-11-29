package com.thulium.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.math.MathUtils;
import com.thulium.world.GameWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class PlayerControllerInput implements ControllerListener {
	private final Map<Integer, Integer> buttonMap = new HashMap<>();
	private final PlayerInput input;

	private float lastAxisValue;

	public PlayerControllerInput(PlayerInput input, GameWorld world) {
		this.input = input;

		// Mapped in order of 0-n in controller's button range
		int[] keys = {PlayerInput.JUMP, Keys.UNKNOWN, PlayerInput.ATTACK, Keys.UNKNOWN, PlayerInput.DEBUG,
				Keys.UNKNOWN, PlayerInput.PAUSE, Keys.UNKNOWN, Keys.UNKNOWN, Keys.UNKNOWN,
				Keys.UNKNOWN, PlayerInput.JUMP, PlayerInput.JUMP, PlayerInput.WALK_LEFT, PlayerInput.WALK_RIGHT};
		IntStream.range(0, keys.length).forEach(i -> buttonMap.put(i, keys[i]));
	}

	@Override
	public void connected(Controller controller) {
		// TODO Auto-generated method stub
		if (controller.getPlayerIndex() < 1)
			System.out.print("Controller ");
		//	world.addPlayer();

		System.out.println("Controller " + controller.getPlayerIndex() + " connected.");
		System.out.println();
	}

	@Override
	public void disconnected(Controller controller) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		System.out.println("Pressing button " + buttonCode + " mapped to " + buttonMap.get(buttonCode));
		return input.keyDown(buttonMap.get(buttonCode));
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
		return input.keyUp(buttonMap.get(buttonCode));
	}

	/**
	 *
	 * @param controller	The controller sending the input
	 * @param axisCode		Left/right (0), up/down (1)
	 * @param value			A -1 (left/down) to 1 (right/up) range indicating how far the axis has moved
	 * @return 				Only true if the value is greater than zero and the corresponding input process returns true.
	 *						Returns key up if the axis is less than .1, representing a resting position
	 */
	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		System.out.println(value + " on axis " + axisCode + ", last axis value " + lastAxisValue);
		boolean moved = false;
		switch (axisCode) {
			case 0:	// Left-right
				if (Math.abs(value) > .1f)	// Joystick moved
					moved =  value > 0 ? input.keyDown(PlayerInput.WALK_RIGHT) : input.keyDown(PlayerInput.WALK_LEFT);
				else
					moved =  lastAxisValue > 0 ? input.keyUp(PlayerInput.WALK_RIGHT) : input.keyUp(PlayerInput.WALK_LEFT);	// Joystick released
				break;
			case 1:	// Up-down
				if (Math.abs(value) > .1f)	// Joystick moved
					moved =  value > 0 ? input.keyDown(PlayerInput.FALL) : input.keyDown(PlayerInput.JUMP);
				else
					moved = lastAxisValue > 0 ? input.keyUp(PlayerInput.FALL) : input.keyUp(PlayerInput.JUMP);	// Joystick released
				break;
			default:
				return false;
		}
		lastAxisValue = value;
		return moved;
	}

}
