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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.SkeletonData;
import com.ray3k.mavisbroccli.Core;
import com.ray3k.mavisbroccli.EntityManager;
import com.ray3k.mavisbroccli.InputManager;
import com.ray3k.mavisbroccli.State;
import com.ray3k.mavisbroccli.entities.AnimationEntity;
import com.ray3k.mavisbroccli.entities.GameOverTimerEntity;

public class GameState extends State implements InputManager.KeyActionListener {
    private OrthographicCamera gameCamera;
    private Viewport gameViewport;
    private OrthographicCamera uiCamera;
    private Viewport uiViewport;
    private InputManager inputManager;
    private Skin skin;
    private Stage stage;
    private Table table;
    private EntityManager entityManager;
    private Food food;
    private FileHandle textFile;
    private Array<SkeletonData> animationQueue;
    private Array<String> lines;
    private String line;
    private int charIndex;
    private Label textLabel;
    private AnimationEntity animationEntity;
    private int characterCounter;
    private float time;
    private boolean complete;
    private boolean started;
    private TiledDrawable tiledDrawable;
    
    public static enum Food {
        ROBOT, BREAKFAST, SALAD
    }
    
    public GameState(Core core) {
        super(core);
    }
    
    @Override
    public void start() {
        inputManager = new InputManager(); 
        
        uiCamera = new OrthographicCamera();
        uiViewport = new ScreenViewport(uiCamera);
        uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiViewport.apply();
        
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        
        gameCamera = new OrthographicCamera();
        gameViewport = new ScreenViewport(gameCamera);
        gameViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gameViewport.apply();
        
        gameCamera.position.set(gameCamera.viewportWidth / 2, gameCamera.viewportHeight / 2, 0);
        
        skin = getCore().getAssetManager().get(Core.DATA_PATH + "/ui/mavis-broccoli.json", Skin.class);
        stage = new Stage(new ScreenViewport());
        
        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(inputManager);
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
        inputManager.addKeyActionListener(this);
        
        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        
        entityManager = new EntityManager();
        
        
        animationQueue = new Array<SkeletonData>();
        
        switch (food) {
            case SALAD:
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/tomato.json", SkeletonData.class));
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/cucumber.json", SkeletonData.class));
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/salad-plate.json", SkeletonData.class));
                break;
            case BREAKFAST:
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/bacon.json", SkeletonData.class));
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/bacon.json", SkeletonData.class));
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/egg.json", SkeletonData.class));
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/egg.json", SkeletonData.class));
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/breakfast-plate.json", SkeletonData.class));
                break;
            case ROBOT:
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/pipe.json", SkeletonData.class));
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/robot-bits.json", SkeletonData.class));
                animationQueue.add(getCore().getAssetManager().get(Core.DATA_PATH + "/spine/robot-plate.json", SkeletonData.class));
                break;
        }
        animationQueue.reverse();

        createAnimation();
        
        String text = textFile.readString();
        text = text.replace(' ', '_');
        lines = new Array<String>(text.split("\\R"));
        int index = MathUtils.random(lines.size - 1);
        line = lines.get(index);
        lines.removeIndex(index);
        charIndex = 0;
        
        createStageElements();
        updateTextLabel();
        characterCounter = 0;
        complete = false;
        started = false;
        time = 0;
        
        Array<String> names = getCore().getImagePacks().get(Core.DATA_PATH + "/backgrounds");
        tiledDrawable = new TiledDrawable(getCore().getAtlas().findRegion(names.random()));
    }
    
    private void updateTextLabel() {
        String lineEdited = "";
        if (charIndex == 0) {
            lineEdited = "[PINK]" + line.substring(charIndex, charIndex + 1) + "[]" + line.substring(charIndex + 1, line.length());
        } else {
            lineEdited = line.substring(MathUtils.clamp(charIndex - 13, 0, charIndex), charIndex) + "[PINK]" + line.substring(charIndex, charIndex + 1) + "[]" + line.substring(charIndex + 1, line.length());
        }
        
        textLabel.setText(lineEdited);
    }

    @Override
    public void keyTyped(char character) {
        started = true;
        if (!complete) {
            if (character == line.charAt(charIndex) || character == ' ' && line.charAt(charIndex) == '_') {
                characterCounter++;
                playTypingCharacterSound();

                if (character == ' ' || charIndex == line.length() - 1) {
                    animationEntity.proceed();
                }
                charIndex++;
                if (charIndex >= line.length()) {
                    if (lines.size > 0) {
                        int index = MathUtils.random(lines.size - 1);
                        line = lines.get(index);
                        lines.removeIndex(index);
                        charIndex = 0;
                        updateTextLabel();
                    }
                } else {
                    updateTextLabel();
                }
            } else {
                if ((int)character != 0) {
                    playIncorrectSound();
                }
            }
        }
    }
    
    public void createAnimation() {
        if (animationQueue.size > 0) {
            animationEntity = new AnimationEntity(this, animationQueue.pop());
            animationEntity.setPosition(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f);
        } else {
            new GameOverTimerEntity(this, 3.0f);
            textLabel.setText("");
            complete = true;
        }
    }
    
    private void createStageElements() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        textLabel = new Label("", skin, "cml");
        textLabel.getStyle().font.getData().markupEnabled = true;
        textLabel.setEllipsis(true);
        textLabel.setEllipsis("");
        root.add(textLabel).width(740.0f);
    }
    
    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        Gdx.gl.glClearColor(252 / 255.0f, 255 / 255.0f, 207 / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        gameCamera.update();
        spriteBatch.setProjectionMatrix(gameCamera.combined);
        spriteBatch.begin();
        tiledDrawable.draw(spriteBatch, 0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        entityManager.draw(spriteBatch, delta);
        spriteBatch.end();
        
        stage.draw();
    }

    @Override
    public void act(float delta) {
        entityManager.act(delta);
        
        stage.act(delta);
        
        if (!complete && started) {
            time += delta;
        }
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
        gameViewport.update(width, height);
        gameCamera.position.set(width / 2, height / 2.0f, 0.0f);
        
        uiViewport.update(width, height);
        uiCamera.position.set(uiCamera.viewportWidth / 2, uiCamera.viewportHeight / 2, 0);
        stage.getViewport().update(width, height, true);
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public InputManager getInputManager() {
        return inputManager;
    }
    
    public void playChopSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/chop.wav", Sound.class).play(.5f);
    }
    
    public void playCorrectSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/correct.wav", Sound.class).play(.5f);
    }
    
    public void playDropSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/drop.wav", Sound.class).play(.5f);
    }
    
    public void playIncorrectSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/incorrect.wav", Sound.class).play(.5f);
    }
    
    public void playLandSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/land.wav", Sound.class).play(.5f);
    }
    
    public void playTypingCharacterSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/typing character.wav", Sound.class).play(.5f);
    }
    
    public void playPocketSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/pocket.wav", Sound.class).play(.5f);
    }
    
    public void playCrackSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/crack.wav", Sound.class).play(.5f);
    }
    
    public void playSizzleSound() {
        getCore().getAssetManager().get(Core.DATA_PATH + "/sfx/sizzle.wav", Sound.class).play(.5f);
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public void setGameCamera(OrthographicCamera gameCamera) {
        this.gameCamera = gameCamera;
    }
    
    public Skin getSkin() {
        return skin;
    }

    public Stage getStage() {
        return stage;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public FileHandle getTextFile() {
        return textFile;
    }

    public void setTextFile(FileHandle textFile) {
        this.textFile = textFile;
    }

    public int getCharacterCounter() {
        return characterCounter;
    }

    public float getTime() {
        return time;
    }
}