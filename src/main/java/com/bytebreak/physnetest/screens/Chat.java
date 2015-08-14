package com.bytebreak.physnetest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.net.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.bytebreak.physnetest.MyGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by mwingfield on 8/13/15.
 */
public class Chat implements Screen {
    private static final int SERVER_PORT = 9222;

    private MyGame game;
    private Stage stage = new Stage();

    private Table messageList;
    private int maxMessagesSize = 20;
    private Queue<TextMessage> messages;
    private BitmapFont defaultFont;

    private String userName;
    private int listeningPort;
    private ServerSocket serverSocket;
    private final boolean listening = true;

    private String serverIp = "";

    public Chat(final MyGame game) {
        this.game = game;
        Skin skin = game.assetManager.get("skins/menu-skin.json", Skin.class);
        TextField textfield = new TextField("hello world", skin, "default");
        textfield.setMessageText("blah");
        textfield.setPosition(0, 0);
        textfield.setBlinkTime(0.75f);
        textfield.setSize(Gdx.graphics.getWidth(), 50);
        textfield.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char key) {
                if (key == '\r' || key == '\n') {
                    sendMessage(textField.getText());
                    textField.setText("");
                }
            }
        });
        stage.addActor(textfield);
        Gdx.input.setInputProcessor(stage);

        messageList = new Table();
        messageList.setPosition(0, textfield.getHeight());
        messageList.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - textfield.getHeight());
        stage.addActor(messageList);

        messages = new ArrayBlockingQueue<>(maxMessagesSize + 1);

        defaultFont = new BitmapFont();

        scanForServer();
    }

    @Override
    public void show() {
        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String text) {
                userName = text;
            }

            @Override
            public void canceled() {
                game.setScreen(new Menu(game));
            }
        }, "User Name", "", "type user name");

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
        if (serverSocket != null) {
            serverSocket.dispose();
        }
    }

    public void sendMessage(String message){
        TextMessage textMessage = new TextMessage(userName, message);

        SocketHints socketHints = new SocketHints();
        socketHints.connectTimeout = 4000; // in milliseconds
        try {
            Socket socket = Gdx.net.newClientSocket(Net.Protocol.TCP, serverIp, SERVER_PORT, socketHints); // todo: change to remote server
            try {
                Gdx.app.log("Message", textMessage.toString());
                socket.getOutputStream().write(textMessage.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            Gdx.app.log("error", "Server is down");
        }
    }

    public void receiveMessage(TextMessage message){
        if (userName.equalsIgnoreCase(message.user)){
            message.local = true;
        }
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = defaultFont;
        style.fontColor = (message.local ? Color.BLUE : Color.GRAY);
        Label label = new Label(message.user + ": " + message.message, style);
        message.label = label;

        messageList.add(label).row();

        messages.add(message);
        if (messages.size() > maxMessagesSize) {
            messageList.removeActor(messages.remove().label);
        }
    }

    public void tryServerIp(final String ip, final HttpRequestBuilder requestBuilder, final int i){
        final String ipAddr = ip + i;
        final Net.HttpRequest httpRequest = requestBuilder.newRequest().method(Net.HttpMethods.GET).url("http://" + ip + i + ":" + (SERVER_PORT + 1) + "/ping").build();
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Gdx.app.log("response: ", httpResponse.toString());
                serverIp = ipAddr;
                Gdx.app.log("Server", serverIp);
                scanForOpenPort();
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.log("failed", ipAddr);
                if (i > 40){
                    throw new RuntimeException("Couldn't find server on LAN");
                }
                tryServerIp(ip, requestBuilder, i + 1);
            }

            @Override
            public void cancelled() {

            }
        });
    }

    public void scanForServer(){
        List<String> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for(NetworkInterface ni : Collections.list(interfaces)){
                for(InetAddress address : Collections.list(ni.getInetAddresses()))
                {
                    if(address instanceof Inet4Address){
                        addresses.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Print the contents of our array to a string.  Yeah, should have used StringBuilder
        String ipAddress = new String("");
        for(String str:addresses)
        {
            ipAddress = ipAddress + str + "\n";
        }

        String[] ipSplit = addresses.get(0).split("\\.");
        String ip = ipSplit[0] + "." + ipSplit[1] + "." + ipSplit[2] + ".";
        serverIp = null;
        tryServerIp(ip, new HttpRequestBuilder(), 0);

    }

    public void tryPort(ServerSocketHints serverSocketHint, int port) {
        try {
            serverSocket = Gdx.net.newServerSocket(Net.Protocol.TCP, port, serverSocketHint);
            listeningPort = port;

            Net.HttpRequest httpRequest = new HttpRequestBuilder().newRequest().method(Net.HttpMethods.GET).url("http://" + serverIp + ":" + (SERVER_PORT + 1) + "/register?" + listeningPort).build();
            Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(Net.HttpResponse httpResponse) {
                    Gdx.app.log("succeed", "registered");
                }

                @Override
                public void failed(Throwable t) {
                    Gdx.app.log("failed", "registration");
                }

                @Override
                public void cancelled() {

                }
            });
        } catch (Exception e){
            if (port < SERVER_PORT + 1000) {
                tryPort(serverSocketHint, port + 1);
            }
        }
    }

    public void scanForOpenPort(){
        ServerSocketHints serverSocketHint = new ServerSocketHints();
        // 0 means no timeout.  Probably not the greatest idea in production!
        serverSocketHint.acceptTimeout = 0;

        int testPort = SERVER_PORT + 2;
        tryPort(serverSocketHint, testPort);

        if (serverSocket == null){
            throw new RuntimeException("Couldn't find open port to hook socket up to");
        }
        // Now we create a thread that will listen for incoming socket connections
        new Thread(new Runnable() {

            @Override
            public void run() {
                // Loop forever
                while (listening) {
                    // Create a socket
                    Gdx.app.log("listening", "" + listeningPort);
                    Socket socket = serverSocket.accept(null);

                    // Read data from the socket into a BufferedReader
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    try {
                        String msg = buffer.readLine();
                        Gdx.app.log("Got something", msg);
                        // Read to the next newline (\n) and display that text on labelMessage
                        receiveMessage(new TextMessage(msg));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start(); // And, start the thread running
    }

    public class TextMessage{
        public long timestamp = 0;
        public String message = "";
        public String user = "";
        public boolean local = false;
        public Label label;

        public TextMessage(String user, String message){
            timestamp = System.currentTimeMillis();
            this.user = user;
            this.message = message;
            local = true;
        }

        public TextMessage(String parseableBody){
            String[] arr = parseableBody.split(";");
            timestamp = Long.parseLong(arr[0]);
            user = arr[1];
            message = arr[2];
        }

        public TextMessage(byte[] data){
            this(new String(data));
        }

        public String toString(){
            return timestamp + ";" + user + ";" + message + "\n";
        }
    }
}
