package com.thulium.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

public class PauseMenu {
    private Stage stage;
    private boolean isShowing;

    private Skin skin;

    public PauseMenu(Skin skin) {
        this.skin = skin;
        stage = new Stage();

        Gdx.input.setInputProcessor(stage);
    }

    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void show(PauseComponents menu) {

    }

    public void show() {
        isShowing = !isShowing;
        show(new Pause());
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void dispose() {
        stage.dispose();
    }

    /**
     *
     */
    private abstract class PauseComponents {

        public abstract String getTitle();
        public abstract Array<Actor> getComponents();
    }

    private class Pause extends PauseComponents {

        @Override
        public String getTitle() {
            return "-PAUSED-";
        }

        @Override
        public Array<Actor> getComponents() {
            Array<Actor> actors = new Array<>();

            Table table = new Table(skin);
            for (String s : new String[] {"Inventory", "Game Options", "Sound & Music Volume", "Controls"}) {
                Label l = new Label(s, skin);
                table.add(l).row();
            }

            actors.add(table);
            return actors;
        }
    }
}
