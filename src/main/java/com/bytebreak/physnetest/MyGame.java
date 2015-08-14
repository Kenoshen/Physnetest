package com.bytebreak.physnetest;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.bytebreak.physnetest.screens.Menu;

/**
 * Created by mwingfield on 8/13/15.
 */
public class MyGame extends Game {
    public com.badlogic.gdx.Application app;
    public AssetManager assetManager;

    public MyGame(){
        super();
        assetManager = new AssetManager();
    }

    @Override
    public void create() {
        assetManager.load("fonts/chalk.fnt", BitmapFont.class);
        assetManager.load("packed/ui.atlas", TextureAtlas.class);
        assetManager.load("skins/menu-skin.json", Skin.class, new SkinLoader.SkinParameter("packed/ui.atlas"));
        assetManager.finishLoading();

        setScreen(new Menu(this));
    }

    @Override
    public void render(){
        super.render();
    }

    @Override
    public void dispose()
    {
        super.dispose();
    }
}
