package com.thulium.player;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.thulium.world.GameWorld;

import java.util.HashMap;
import java.util.Map;

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
		for (int i = 0; i < keys.length; i++)
			buttonMap.put(i, keys[i]);
	}

	@Override
	public void connected(Controller controller) {
		// TODO Auto-generated method stub
		if (controller.getPlayerIndex() < 1)
		//	world.addPlayer();

		System.out.print("Controller ");
		System.out.println(controller.getPlayerIndex() + " connected.");
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

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		if (Math.abs(value) < .1f)
			return input.keyUp( Keys.A);

		System.out.println(value);
		switch (axisCode) {
			case 0:
				return value > 0 ? input.keyDown(Keys.D) : input.keyDown(Keys.A);
			case 1:
				return value > 0 ? input.keyDown(Keys.DOWN) : input.keyDown(Keys.UP);
		}
		// 0 - LR. 1 - UP/DOWN
		return false;
	}

}
