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
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ray3k.mavisbroccli.Core;
import com.ray3k.mavisbroccli.EntityManager;
import com.ray3k.mavisbroccli.State;
import com.ray3k.mavisbroccli.entities.BroccoliEntity;

public class GameOverState extends State {
    private Stage stage;
    private Skin skin;
    private EntityManager entityManager;
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;

    public GameOverState(Core core) {
        super(core);
    }

    @Override
    public void start() {
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/ui/mavis-broccoli.json", Skin.class);
        
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        
        refreshTable();
        
        entityManager = new EntityManager();
        gameCamera = new OrthographicCamera();
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameViewport.apply();
        
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        new BroccoliEntity(entityManager, getCore());
    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1);
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
        
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            getCore().getStateManager().loadState("menu");
        }
        
        entityManager.act(delta);
    }

    @Override
    public void stop() {
        stage.dispose();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
        gameCamera.position.set(width / 2, height / 2.0f, 0.0f);
        
        stage.getViewport().update(width, height, true);
    }
    
    private void refreshTable() {
        stage.clear();
        
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        Label label = new Label("Congratulations!", skin, "title");
        root.add(label).colspan(2);
        
        GameState gameState = (GameState) getCore().getStateManager().getState("game");
        int wpm = MathUtils.floor(gameState.getCharacterCounter() / gameState.getTime() * 60 / 5);
        
        root.row();
        label = new Label(Integer.toString(wpm) + " Words Per Minute", skin);
        label.setAlignment(Align.center);
        root.add(label).pad(20.0f);
        
        root.row();
        label = new Label("Press space\nto return to menu!", skin);
        label.setAlignment(Align.center);
        root.add(label);
    }
}
