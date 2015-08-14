package com.bytebreak.physnetest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.bytebreak.physnetest.MyGame;

/**
 * Created by mwingfield on 8/13/15.
 */
public class Menu implements Screen {
    private MyGame game;
    private Stage stage = new Stage();

    private Table menu;
    private TextButton chat;
    private TextButton chatServer;

    public Menu(final MyGame game){
        this.game = game;

        Skin skin = game.assetManager.get("skins/menu-skin.json", Skin.class);

        chat = new TextButton("Chat", skin, "simple");
        chat.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new Chat(game));
            }
        });

        chatServer = new TextButton("Chat Server", skin, "simple");
        chatServer.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ChatServer(game));
            }
        });

        menu = new Table();
        menu.add(chat).height(60).padBottom(5).row();
        menu.add(chatServer).height(60).padBottom(5).row();
//        menu.add(highscoreBtn).height(60).padBottom(5).row();
//        menu.add(optionsBtn).height(60).padBottom(5).row();
//        menu.add(quitBtn).height(60).padBottom(5).row();
        menu.setFillParent(true);

        stage.addActor(menu);

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.39f, 0.58f, 0.92f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
