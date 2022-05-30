package com.thulium.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;

import java.util.HashMap;
import java.util.Map;

public class PlayerControllerInput implements ControllerListener {
	private Map<Integer, Integer> buttonMap;
	private PlayerInput input;

	public PlayerControllerInput(PlayerInput input) {
		this.input = input;
		buttonMap = new HashMap<>();
		int[] keys = {Keys.W, Keys.P, Keys.K, Keys.NUM_1, Keys.NUM_1, Keys.NUM_1, Keys.NUM_1, Keys.NUM_1, Keys.NUM_1,
				Keys.NUM_1, Keys.O, Keys.UP, Keys.DOWN, Keys.LEFT, Keys.RIGHT};
		for (int i = 0; i < keys.length; i++)
			buttonMap.put(i, keys[i]);
	}

	@Override
	public void connected(Controller controller) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return false;
	}

}
