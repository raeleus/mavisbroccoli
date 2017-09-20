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
package com.ray3k.mavisbroccli.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.ray3k.mavisbroccli.Entity;
import com.ray3k.mavisbroccli.states.GameState;

public class AnimationEntity extends Entity {

    private Skeleton skeleton;
    private AnimationState animationState;
    private GameState gameState;
    private static final Vector2 temp = new Vector2();
    private int proceedCounter;
    
    public AnimationEntity(final GameState gameState, SkeletonData skeletonData) {
        super(gameState.getEntityManager(), gameState.getCore());
        this.gameState = gameState;
        skeleton = new Skeleton(skeletonData);
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationStateData.setDefaultMix(.25f);
        animationState = new AnimationState(animationStateData);
        animationState.setAnimation(0, "animation", false);
        animationState.addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                if (proceedCounter > 0) {
                    proceedCounter--;
                    if (event.getData().getName().equals("drip")) {
                        gameState.playDropSound();
                    } else if (event.getData().getName().equals("drop")) {
                        gameState.playLandSound();
                    } else if (event.getData().getName().equals("pocket")) {
                        gameState.playPocketSound();
                    } else if (event.getData().getName().equals("chop")) {
                        gameState.playChopSound();
                    } else if (event.getData().getName().equals("crack")) {
                        gameState.playCrackSound();
                    } else if (event.getData().getName().equals("sizzle")) {
                        gameState.playSizzleSound();
                    } else if (event.getData().getName().equals("correct")) {
                        gameState.playCorrectSound();
                    }
                }
            }
            
            @Override
            public void complete(AnimationState.TrackEntry entry) {
                gameState.createAnimation();
                AnimationEntity.this.dispose();
                proceedCounter++;
            }
        });
        
        proceedCounter = 0;
    }

    @Override
    public void create() {
    }

    @Override
    public void act(float delta) {
        skeleton.setPosition(getX(), getY());
        if (proceedCounter > 0) {
            animationState.update(delta);
        }
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
    }

    @Override
    public void act_end(float delta) {

    }

    @Override
    public void draw(SpriteBatch spriteBatch, float delta) {
        getCore().getSkeletonRenderer().draw(spriteBatch, skeleton);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void collision(Entity other) {
    }

    public void proceed() {
        proceedCounter++;
    }
}
