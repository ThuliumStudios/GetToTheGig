package com.thulium.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;

public class PlayerControllerInput implements ControllerListener {
	private PlayerInput input;
	
	public PlayerControllerInput(PlayerInput input) {
		this.input = input;
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
		switch (buttonCode) {
			case 0:
				input.keyDown(Keys.W);
				break;
			case 1:
				input.keyDown(Keys.P);
				break;
			case 2:
				input.keyDown(Keys.K);
				break;
			case 13:
				input.keyDown(Keys.A);
				break;
			case 14:
				input.keyDown(Keys.D);
				break;
			case 10:
				input.keyDown(Keys.O);
				break;
		}
		System.out.println(buttonCode);
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
		switch (buttonCode) {
			case 10:
				input.keyUp(Keys.O);
				break;
			case 13:
				input.keyUp(Keys.A);
				break;
			case 14:
				input.keyUp(Keys.D);
				break;
		}
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		// TODO Auto-generated method stub
		return false;
	}

}
