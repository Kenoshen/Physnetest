package com.bytebreak.physnetest;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Application
{
    private static final HTMLLogger log = HTMLLogger.getLogger(Application.class, LogGroup.System);
    public static void main(String[] arg)
    {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 800;
        config.height = 450;
        System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
        Game game = new Game();
        com.badlogic.gdx.Application app = new LwjglApplication(game, config);
        game.app = app;
        GlobalClipboard.instance().setClipboard(app.getClipboard());
    }
}
