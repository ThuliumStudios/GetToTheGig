package com.thulium.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.thulium.item.Item;

import java.lang.reflect.GenericArrayType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class PauseMenu {
    private final Stage stage;

    // Consistent UI components
    private final Label rightLabel;

    // Tables
    private final Table leftTable;
    private final Table rightTable;

    private static final String[] options = {"*Inventory", "*Game Options", "*Audio", "*Controls"};
    private final Map<String, Table> tables = new HashMap<>();

    public PauseMenu(Skin skin) {
        stage = new Stage();
        int i = 0;  // To iterate over table options

        // Create Inventory table
        Table inventoryTable = new Table(skin);

        Stack gear = new Stack();
        Map<String, Image> gearMap = new HashMap<>();
        gear.add(new Image(skin, "gear"));
        VerticalGroup itemsGroup = new VerticalGroup();
        itemsGroup.align(Align.left).grow();
        Arrays.asList("Amp", "Axe", "Wheels", "Bass", "Mic", "Drums", "Drumsticks", "Keys").forEach(item -> { // In stack order
            CheckBox checkbox = new CheckBox(item, skin);
            checkbox.setName(item.toLowerCase());
            gearMap.put(item.toLowerCase(), new Image(skin, item.toLowerCase()));
            gearMap.get(item.toLowerCase()).setColor(1, 1, 1, 0);
            gear.add(gearMap.get(item.toLowerCase()));
            checkbox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent changeEvent, Actor actor) {
                    if (((CheckBox) actor).isChecked()) {
                        gearMap.get(actor.getName()).setColor(1, 1, 1, 1);
                    } else {
                        gearMap.get(actor.getName()).setColor(1, 1, 1, 0);
                    }
                }
            });
            checkbox.align(Align.left);
            itemsGroup.addActor(checkbox);
        });

        inventoryTable.defaults().width(stage.getWidth() / 4f).growY().uniform();
        inventoryTable.add(gear).height(Value.percentWidth(1));
        inventoryTable.add(itemsGroup);
        inventoryTable.pack();
        tables.put(options[i++], inventoryTable);


        // Create Game Options table
        Table optionsTable = new Table(skin);
        tables.put(options[i++], optionsTable);


        // Create Sound & Music Volume table
        Table audioTable = new Table(skin);
        Table masterAudio = new Table(skin);
        Table sfxTable = new Table(skin);
        Table musicTable = new Table(skin);
        // Slider sfxSlider = new Slider(0, 100, 1, false, skin);
        // Slider musicSlider = new Slider(0, 100, 1, false, skin);
        audioTable.add(musicTable, sfxTable);
        audioTable.add(masterAudio).colspan(2);
        tables.put(options[i++], audioTable);


        // Create Controls table
        Table controlsTable = new Table(skin);
        tables.put(options[i++], controlsTable);


        // Create default left and right tables
        Table contentTable = new Table(skin);
        leftTable = new Table(skin);
        rightTable = new Table(skin);
        rightTable.defaults().pad(8);
        contentTable.defaults().fill();

        // Initialize right table
        rightLabel = new Label("*INVENTORY*", skin.get("title", Label.LabelStyle.class));
        rightLabel.setAlignment(Align.center);
        rightTable.add(rightLabel).height(stage.getHeight() / 8f).expand().growX().top().row();
        rightTable.add(contentTable).fill();


        // Add left table components
        leftTable.defaults().pad(8).left();
        Label pauseLabel = new Label("-PAUSED-", skin.get("title", Label.LabelStyle.class));
        pauseLabel.setAlignment(Align.center);
        leftTable.add(pauseLabel).height(stage.getHeight() / 8f).expand().growX().top().row();

        // TEST - Add left lables to vertical group
        VerticalGroup leftLabels = new VerticalGroup();
        Arrays.asList(options).forEach(str -> {
            Label l = new Label(str, skin.get("black", Label.LabelStyle.class));
            l.setName(str);
            l.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    rightLabel.setText(str.toUpperCase() + "*");
                    contentTable.clear();
                    contentTable.addActor(tables.get(event.getTarget().getName()));
                    contentTable.layout();
                    tables.get(event.getTarget().getName()).layout();
                    // contentTable.pack();
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    ((Label) event.getTarget()).setStyle(skin.get("white", Label.LabelStyle.class));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    ((Label) event.getTarget()).setStyle(skin.get("black", Label.LabelStyle.class));
                }
            });
            l.addListener(new FocusListener() {
                @Override
                public boolean handle(Event event) {
                    leftLabels.getChildren().forEach(l -> ((Label) l).setStyle(skin.get("black", Label.LabelStyle.class)));
                    ((Label) event.getTarget()).setStyle(skin.get("white", Label.LabelStyle.class));
                    return super.handle(event);
                }
            });
            // leftTable.add(l).row();
            leftLabels.addActor(l);
        });
        leftTable.add(leftLabels);
        // leftTable.add().pad(stage.getHeight() / 4, 0, 0, 0); // To add spacing at the bottom

        // Add default tables to stage
        Table table = new Table(skin);
        table.setBackground(skin.getDrawable("skin_background"));
        table.setFillParent(true);
        table.defaults().grow().uniform();
        table.add(leftTable, rightTable);
        stage.addActor(table);

        // TEST
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                // Hard-coded inputs
                switch (keycode) {
                    case Input.Keys.TAB:
                        stage.setKeyboardFocus(getNextActor(stage.getKeyboardFocus()));
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void renderDebug(boolean debug) {
        if (stage.isDebugAll() != debug)
            stage.setDebugAll(debug);
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public Stage getStage() {
        return stage;
    }

    public void dispose() {
        stage.dispose();
    }

    /**
     *  Selects next actor
     */
    private Actor getNextActor(Actor currentActor) {
        // Get the list of actors in the stage
        Array<Actor> actors = ((Group) leftTable.getChild(1)).getChildren();

        // Find the index of the current actor
        int currentIndex = actors.indexOf(currentActor, true);

        // Get the next actor in the list
        int nextIndex = (currentIndex + 1) % actors.size;

        Actor nextActor = actors.get(nextIndex);
        if (nextActor == null)
            nextActor = actors.first();

        System.out.println("Focused " + nextActor);
        return nextActor;
    }
}