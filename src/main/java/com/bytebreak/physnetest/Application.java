package com.bytebreak.physnetest;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Application
{
    public static void main(String[] arg)
    {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 800;
        config.height = 450;
        System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
        MyGame game = new MyGame();
        com.badlogic.gdx.Application app = new LwjglApplication(game, config);
        game.app = app;
    }
}
