package com.thulium.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.thulium.world.GameWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class PlayerControllerInput implements ControllerListener {
	private Map<Integer, Integer> buttonMap;
	private PlayerInput input;
	private GameWorld world;

	public PlayerControllerInput(PlayerInput input, GameWorld world) {
		this.input = input;
		this.world = world;
		buttonMap = new HashMap<>();
		int[] keys = {Keys.W, Keys.P, Keys.K, Keys.NUM_1, Keys.NUM_1, Keys.NUM_1, Keys.ESCAPE, Keys.NUM_1, Keys.NUM_1,
				Keys.NUM_1, Keys.O, Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT};

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
		input.keyDown(buttonMap.get(buttonCode));
		System.out.println(buttonCode);
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
		input.keyUp(buttonMap.get(buttonCode));
		return false;
	}

	/**
	 *
	 * @param controller	The controller sending the input
	 * @param axisCode		Left/right (0) or up/down (1)
	 * @param value			A 0-1 range indicating how far the axis has moved
	 * @return 				Only true if the value is greater than zero and the corresponding input process returns true.
	 *						Returns key up if the axis is less than .1, representing a resting position
	 */
	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		if (Math.abs(value) < .1f)
			return input.keyUp( Keys.A);

		// System.out.println(value);
		switch (axisCode) {
			case 0:
				return value > 0 ? input.keyDown(Keys.D) : input.keyDown(Keys.A);
			case 1:
				return value > 0 ? input.keyDown(Keys.DOWN) : input.keyDown(Keys.UP);
		}
		return false;
	}

}
