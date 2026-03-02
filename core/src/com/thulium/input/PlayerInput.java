package com.thulium.input;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.utils.CharArray;
import com.thulium.player.Player;
import com.thulium.screen.GameScreen;
import com.thulium.screen.GameScreen.GameState;
import com.thulium.util.Units;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class PlayerInput implements InputProcessor, ControllerListener {
    public static final int UNKNOWN = Keys.UNKNOWN;
    public static final int PAUSE = Keys.ESCAPE;
    public static final int WALK_LEFT = Keys.A;
    public static final int WALK_RIGHT = Keys.D;
    public static final int UP = Keys.W;
    public static final int DOWN = Keys.S;
    public static final int POWERSLIDE = Keys.SHIFT_RIGHT;
    public static final int DEBUG = Keys.F12;
    public static final int ATTACK = Keys.K;
    private final Map<Integer, Integer> controls = new HashMap<>();
    private final Map<Integer, String> keysDown = new HashMap<>();
    private final CharArray chars = new CharArray();

    // Controller variables
    private final Map<Integer, Integer> buttonMap = new HashMap<>();
    private final Player player;
    private float lastAxisValue;

    public PlayerInput(Player player) {
        this.player = player;

        // Map controls based on settings. TODO: Save default controls to settings file, load from settings
        controls.putIfAbsent(Keys.ESCAPE, PAUSE);
        controls.putIfAbsent(Keys.A, WALK_LEFT);
        controls.putIfAbsent(Keys.LEFT, WALK_LEFT);
        controls.putIfAbsent(Keys.D, WALK_RIGHT);
        controls.putIfAbsent(Keys.RIGHT, WALK_RIGHT);
        controls.putIfAbsent(Keys.W, UP);
        controls.putIfAbsent(Keys.UP, UP);
        controls.putIfAbsent(Keys.S, DOWN);
        controls.putIfAbsent(Keys.DOWN, DOWN);
        controls.putIfAbsent(Keys.SPACE, UP);
        controls.putIfAbsent(Keys.SHIFT_RIGHT, POWERSLIDE);
        controls.putIfAbsent(Keys.SHIFT_LEFT, POWERSLIDE);
        controls.putIfAbsent(Keys.K, ATTACK);
        controls.putIfAbsent(Keys.F12, DEBUG);

        // Map controller inputs
        // Mapped in order of 0-n in controller's button range
        int[] keys = {UP, UNKNOWN, ATTACK, UNKNOWN, DEBUG, UNKNOWN, PAUSE, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UP, DOWN, WALK_LEFT, WALK_RIGHT};
        IntStream.range(0, keys.length).forEach(i -> buttonMap.put(i, keys[i]));
    }

    @Override
    public boolean keyDown(int keycode) {
        if (player.getHP() <= 0)    // TODO: Consider handling differently
            return false;

        switch (controls.getOrDefault(keycode, Keys.UNKNOWN)) {
            case WALK_LEFT:
                player.setXVelocity(-Units.MAX_VELOCITY);
                player.setFlipState(true);
                break;
            case WALK_RIGHT:
                player.setXVelocity(Units.MAX_VELOCITY);
                player.setFlipState(false);
                break;
            case UP:
                player.jump();
                break;
            case POWERSLIDE: // TODO: Rename, or assign different button to ledge grab/powerslide
                if (player.canGrabLedge())
                    player.grabLedge();
                else if (keyIsDown(Keys.A, Keys.D)) player.powerslide();
                break;
            case PAUSE:
                GameScreen.state =  GameScreen.state.equals(GameState.Running) ? GameState.Paused : GameState.Running;
                break;
            case DEBUG:
                System.out.println("Toggling debug");
                player.toggleDebug();
                break;
            case ATTACK:
                player.charge();
                break;
            default:
                break;
        }

        keysDown.put(keycode, Keys.toString(keycode));
        chars.append("Key down: ").append(keysDown.get(keycode)).append(". All keys currently down: ");
        keysDown.values().forEach(chars::append);
        // printKeys(chars);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (player.getHP() <= 0)    // TODO: Consider handling differently
            return false;

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
            case ATTACK:
                player.attack();
                break;
            case Keys.L:
                player.push();
                break;
            default:
                break;
        }

        chars.append("Key up: ").append(keysDown.get(keycode)).append(". Keys still down: ");
        keysDown.remove(keycode);
        keysDown.values().forEach(chars::append);
        // printKeys(chars);
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

    public Map<Integer, Integer> getbuttonMap() {
        return buttonMap;
    }

    public boolean keyIsDown(int... keycodes) {
        return !Collections.disjoint(Collections.singletonList(keycodes), Collections.singletonList(keysDown.keySet()));
    }

    /*
     *  Controller input directly to PlayerInput class
     */
    @Override
    public void connected(Controller controller) {
        System.out.println("Controller " + controller.getPlayerIndex() + " connected.");
    }

    @Override
    public void disconnected(Controller controller) {
        System.out.println("Controller " + controller.getPlayerIndex() + " disconnected.");
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        System.out.println("Pressing button " + buttonCode + " mapped to " + buttonMap.get(buttonCode));
        return keyDown(buttonMap.get(buttonCode));
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        return keyUp(buttonMap.get(buttonCode));
    }

    /**
     * @param controller The controller sending the input
     * @param axisCode   Left/right (0), up/down (1)
     * @param value      A -1 (left/down) to 1 (right/up) range indicating how far the axis has moved
     * @return Whether the value is greater than zero and the corresponding input process returns true.
     */
    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        System.out.println(value + " on axis " + axisCode + ", last axis value " + lastAxisValue);
        boolean moved = false;
        switch (axisCode) {
            case 0:    // Left-right
                if (Math.abs(value) > .1f)    // Joystick moved
                    moved = value > 0 ? keyDown(PlayerInput.WALK_RIGHT) : keyDown(PlayerInput.WALK_LEFT);
                else
                    moved = lastAxisValue > 0 ? keyUp(PlayerInput.WALK_RIGHT) : keyUp(PlayerInput.WALK_LEFT);    // Joystick released
                break;
            case 1:    // Up-down
            case 2:    // Left joycon, left-right
            case 3:    // Left joycon, up-down
            case 4:    // Left trigger
            case 5:    // Right trigger
                return false;
            default:
                return false;
        }
        lastAxisValue = value;
        return moved;
    }

    public void printKeys(CharArray chars) {
        System.out.println(chars.toStringAndClear());
    }
}