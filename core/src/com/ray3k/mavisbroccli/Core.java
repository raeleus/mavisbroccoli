package com.ray3k.mavisbroccli;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.ray3k.mavisbroccli.SkeletonDataLoader.SkeletonDataLoaderParameter;
import com.ray3k.mavisbroccli.states.GameOverState;
import com.ray3k.mavisbroccli.states.GameState;
import com.ray3k.mavisbroccli.states.LoadingState;
import com.ray3k.mavisbroccli.states.MenuState;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

public class Core extends ApplicationAdapter {
    public final static String VERSION = "1";
    public final static String DATA_PATH = "mavis_broccoli_data";
    private final static long MS_PER_UPDATE = 10;
    private AssetManager assetManager;
    private StateManager stateManager;
    private SpriteBatch spriteBatch;
    private PixmapPacker pixmapPacker;
    private long previous;
    private long lag;
    private TextureAtlas atlas;
    private SkeletonRenderer skeletonRenderer;
    private ObjectMap<String, Array<String>> imagePacks;

    @Override
    public void create() {
        try {
            initManagers();

            loadAssets();

            previous = TimeUtils.millis();
            lag = 0;

            stateManager.loadState("loading");
        } catch (Exception e) {
            e.printStackTrace();
            
            FileWriter fw = null;
            try {
                fw = new FileWriter(Gdx.files.local("java-stacktrace.txt").file(), true);
                PrintWriter pw = new PrintWriter(fw);
                e.printStackTrace(pw);
                pw.close();
                fw.close();
                int choice = JOptionPane.showConfirmDialog(null, "Exception occurred. See error log?", "Game Exception!", JOptionPane.YES_NO_OPTION);
                if (choice == 0) {
                    FileHandle startDirectory = Gdx.files.local("java-stacktrace.txt");
                    if (startDirectory.exists()) {
                        File file = startDirectory.file();
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(file);
                    } else {
                        throw new IOException("Directory doesn't exist: " + startDirectory.path());
                    }
                }
                Gdx.app.exit();
            } catch (Exception ex) {
                
            }
        }
    }
    
    public void initManagers() {
        assetManager = new AssetManager(new LocalFileHandleResolver(), true);
        assetManager.setLoader(SkeletonData.class, new SkeletonDataLoader(new LocalFileHandleResolver()));
        
        stateManager = new StateManager(this);
        stateManager.addState("loading", new LoadingState("menu", this));
        stateManager.addState("menu", new MenuState(this));
        stateManager.addState("game", new GameState(this));
        stateManager.addState("game-over", new GameOverState(this));
        
        spriteBatch = new SpriteBatch();
        
        pixmapPacker = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 5, true, new PixmapPacker.GuillotineStrategy());
        
        skeletonRenderer = new SkeletonRenderer();
        
        imagePacks = new ObjectMap<String, Array<String>>();
        for (String name : new String[] {"backgrounds"}) {
            imagePacks.put(DATA_PATH + "/" + name, new Array<String>());
        }
    }
    
    @Override
    public void render() {
        try {
            long current = TimeUtils.millis();
            long elapsed = current - previous;
            previous = current;
            lag += elapsed;

            while (lag >= MS_PER_UPDATE) {
                stateManager.act(MS_PER_UPDATE / 1000.0f);
                lag -= MS_PER_UPDATE;
            }

            stateManager.draw(spriteBatch, lag / MS_PER_UPDATE);
        } catch (Exception e) {
            e.printStackTrace();
            
            FileWriter fw = null;
            try {
                fw = new FileWriter(Gdx.files.local("java-stacktrace.txt").file(), true);
                PrintWriter pw = new PrintWriter(fw);
                e.printStackTrace(pw);
                pw.close();
                fw.close();
                int choice = JOptionPane.showConfirmDialog(null, "Exception occurred. See error log?", "Game Exception!", JOptionPane.YES_NO_OPTION);
                if (choice == 0) {
                    FileHandle startDirectory = Gdx.files.local("java-stacktrace.txt");
                    if (startDirectory.exists()) {
                        File file = startDirectory.file();
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(file);
                    } else {
                        throw new IOException("Directory doesn't exist: " + startDirectory.path());
                    }
                }
                Gdx.app.exit();
            } catch (Exception ex) {
                
            }
        }
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        stateManager.dispose();
        pixmapPacker.dispose();
        if (atlas != null) {
            atlas.dispose();
        }
    }
    
    public void loadAssets() {
        assetManager.clear();
        SkeletonDataLoaderParameter parameter = new SkeletonDataLoaderParameter(DATA_PATH + "/spine/mavis-broccoli.atlas");
        assetManager.load(DATA_PATH + "/spine/bacon.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/breakfast-plate.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/broccoli.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/cucumber.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/egg.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/pipe.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/robot-bits.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/robot-plate.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/salad-plate.json", SkeletonData.class, parameter);
        assetManager.load(DATA_PATH + "/spine/tomato.json", SkeletonData.class, parameter);
        
        assetManager.load(DATA_PATH + "/ui/mavis-broccoli.json", Skin.class);

        assetManager.load(DATA_PATH + "/gfx/white.png", Pixmap.class);
        
        assetManager.load(DATA_PATH + "/sfx/chop.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/correct.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/drop.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/incorrect.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/land.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/typing character.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/pocket.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/crack.wav", Sound.class);
        assetManager.load(DATA_PATH + "/sfx/sizzle.wav", Sound.class);
        
        for (String directory : imagePacks.keys()) {
            FileHandle folder = Gdx.files.local(directory);
            for (FileHandle file : folder.list()) {
                assetManager.load(file.path(), Pixmap.class);
                imagePacks.get(directory).add(file.nameWithoutExtension());
            }
        }
    }

    @Override
    public void resume() {
        
    }

    @Override
    public void pause() {
        
    }

    @Override
    public void resize(int width, int height) {
        stateManager.resize(width, height);
    }
    
    public AssetManager getAssetManager() {
        return assetManager;
    }

    public StateManager getStateManager() {
        return stateManager;
    }

    public PixmapPacker getPixmapPacker() {
        return pixmapPacker;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public SkeletonRenderer getSkeletonRenderer() {
        return skeletonRenderer;
    }
    
    public ObjectMap<String, Array<String>> getImagePacks() {
        return imagePacks;
    }
}
