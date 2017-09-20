/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ray3k.mavisbroccli.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ray3k.mavisbroccli.Core;
import com.ray3k.mavisbroccli.EntityManager;
import com.ray3k.mavisbroccli.State;
import com.ray3k.mavisbroccli.entities.BroccoliEntity;

public class MenuState extends State {
    private Stage stage;
    private Skin skin;
    private Table root;
    private EntityManager entityManager;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;

    public MenuState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/ui/mavis-broccoli.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        Gdx.input.setInputProcessor(stage);
        
        createMenu();
        
        entityManager = new EntityManager();
        gameCamera = new OrthographicCamera();
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameViewport.apply();
        
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        BroccoliEntity broc = new BroccoliEntity(entityManager, getCore());
        broc.getAnimationState().setAnimation(0, "standing", true);
    }
    
    private void createMenu() {
        FileHandle fileHandle = Gdx.files.local(Core.DATA_PATH + "/data.json");
        JsonReader reader = new JsonReader();
        JsonValue val = reader.parse(fileHandle);
        
        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Table table = new Table();
        root.add(table).padBottom(50.0f).padTop(100.0f);
        
        Label label = new Label("Mavis Broccoli", skin, "title");
        label.setAlignment(Align.center);
        table.add(label).colspan(2);
        
        table.row();
        label = new Label("Teaches", skin, "subtitle");
        table.add(label).right();
        
        label = new Label("TYPING", skin, "title-bold");
        label.setAlignment(Align.center);
        table.add(label);
        
        root.defaults().space(30.0f).padLeft(25.0f);
        root.row();
        TextButton textButtton = new TextButton("Play", skin);
        root.add(textButtton);
        
        textButtton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/correct.wav", Sound.class).play(.25f);
                showTextDialog();
            }
        });
        
        root.row();
        textButtton = new TextButton("Quit", skin);
        root.add(textButtton).expandY().top();
        
        textButtton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/correct.wav", Sound.class).play(.25f);
                Gdx.app.exit();
            }
        });
    }
    
    private void showTextDialog() {
        final ButtonGroup buttonGroup = new ButtonGroup();
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                super.result(object);
                ((GameState) getCore().getStateManager().getState("game")).setTextFile((FileHandle) buttonGroup.getChecked().getUserObject());
                showFoodDialog();
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/correct.wav", Sound.class).play(.25f);
            }
        };
        
        Label label = new Label("Choose difficulty.", skin);
        dialog.getContentTable().add(label);
        
        dialog.getContentTable().row();
        ImageTextButton imageTextButton = new ImageTextButton("Easy", skin);
        buttonGroup.add(imageTextButton);
        dialog.getContentTable().add(imageTextButton);
        imageTextButton.setUserObject(Gdx.files.internal(Core.DATA_PATH + "/text/easy.txt"));
        
        dialog.getContentTable().row();
        imageTextButton = new ImageTextButton("Medium", skin);
        buttonGroup.add(imageTextButton);
        dialog.getContentTable().add(imageTextButton);
        imageTextButton.setUserObject(Gdx.files.internal(Core.DATA_PATH + "/text/medium.txt"));
        
        dialog.getContentTable().row();
        imageTextButton = new ImageTextButton("Hard", skin);
        buttonGroup.add(imageTextButton);
        dialog.getContentTable().add(imageTextButton);
        imageTextButton.setUserObject(Gdx.files.internal(Core.DATA_PATH + "/text/hard.txt"));
        
        dialog.button("OK");
        
        dialog.show(stage);
    }
    
    private void showFoodDialog() {
        final ButtonGroup buttonGroup = new ButtonGroup();
        Dialog dialog = new Dialog("", skin) {
            @Override
            protected void result(Object object) {
                super.result(object);
                ((GameState) getCore().getStateManager().getState("game")).setFood((GameState.Food) buttonGroup.getChecked().getUserObject());
                getCore().getStateManager().loadState("game");
                getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/correct.wav", Sound.class).play(.25f);
            }
        };
        
        Label label = new Label("Choose a dish.", skin);
        dialog.getContentTable().add(label);
        
        dialog.getContentTable().row();
        ImageTextButton imageTextButton = new ImageTextButton("Delicious Salad", skin, "salad");
        buttonGroup.add(imageTextButton);
        dialog.getContentTable().add(imageTextButton);
        imageTextButton.setUserObject(GameState.Food.SALAD);
        
        dialog.getContentTable().row();
        imageTextButton = new ImageTextButton("Tasty Breakfast", skin, "breakfast");
        buttonGroup.add(imageTextButton);
        dialog.getContentTable().add(imageTextButton);
        imageTextButton.setUserObject(GameState.Food.BREAKFAST);
        
        dialog.getContentTable().row();
        imageTextButton = new ImageTextButton("Exquisite Robot Fuel", skin, "robot");
        buttonGroup.add(imageTextButton);
        dialog.getContentTable().add(imageTextButton);
        imageTextButton.setUserObject(GameState.Food.ROBOT);
        
        dialog.button("OK");
        
        dialog.show(stage);
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
        stage.draw();
    }

    @Override
    public void act(float delta) {
        stage.act(delta);
        entityManager.act(delta);
    }

    @Override
    public void dispose() {
        
    }

    @Override
    public void stop() {
        stage.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        gameViewport.update(width, height);
        gameCamera.position.set(width / 2, height / 2.0f, 0.0f);
    }
}