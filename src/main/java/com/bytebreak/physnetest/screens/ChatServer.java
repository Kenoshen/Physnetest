package com.bytebreak.physnetest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.bytebreak.physnetest.MyGame;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by mwingfield on 8/13/15.
 */
public class ChatServer implements Screen {
    private static final int SERVER_PORT = 9222;

    private MyGame game;
    private Stage stage = new Stage();

    private Table messageList;
    private int maxMessagesSize = 20;
    private Queue<TextMessage> messages;
    private BitmapFont defaultFont;

    private ServerSocket serverSocket;

    private List<RegisteredClient> clients = new ArrayList<>();

    public ChatServer(final MyGame game) {
        this.game = game;

        messageList = new Table();
        messageList.setFillParent(true);
        stage.addActor(messageList);

        messages = new ArrayBlockingQueue<>(maxMessagesSize + 1);

        defaultFont = new BitmapFont();
    }

    @Override
    public void show() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", SERVER_PORT + 1), 20);
            server.createContext("/register", new HttpHandler(){
                @Override
                public void handle(HttpExchange httpExchange) throws IOException {
                    Gdx.app.log("got register http request", httpExchange.toString());
                    RegisteredClient newClient = new RegisteredClient(httpExchange.getRemoteAddress().getHostName(), Integer.parseInt(httpExchange.getRequestURI().getQuery()));
                    boolean shouldAdd = true;
                    for (RegisteredClient client : clients){
                        if (client.toString().equalsIgnoreCase(newClient.toString())){
                            shouldAdd = false;
                            break;
                        }
                    }
                    if (shouldAdd){
                        clients.add(newClient);
                    }
                    Gdx.app.log("Clients", clients.toString());
                }
            });
            server.createContext("/ping", new HttpHandler(){
                @Override
                public void handle(HttpExchange httpExchange) throws IOException {
                    Gdx.app.log("got ping http request", httpExchange.toString());
                }
            });
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now we create a thread that will listen for incoming socket connections
        new Thread(new Runnable() {

            @Override
            public void run() {
                ServerSocketHints serverSocketHint = new ServerSocketHints();
                // 0 means no timeout.  Probably not the greatest idea in production!
                serverSocketHint.acceptTimeout = 0;

                // Create the socket server using TCP protocol and listening on 9021
                // Only one app can listen to a port at a time, keep in mind many ports are reserved
                // especially in the lower numbers ( like 21, 80, etc )
                serverSocket = Gdx.net.newServerSocket(Net.Protocol.TCP, SERVER_PORT, serverSocketHint);

                // Loop forever
                while (true) {
                    // Create a socket
                    Gdx.app.log("listening", "" + SERVER_PORT);
                    Socket socket = serverSocket.accept(null);

                    // Read data from the socket into a BufferedReader
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    try {
                        String msg = buffer.readLine();
                        Gdx.app.log("Got something", msg);
                        // Read to the next newline (\n) and display that text on labelMessage
                        TextMessage txtMsg = new TextMessage(msg);
                        receiveMessage(txtMsg);
                        sendMessage(txtMsg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start(); // And, start the thread running
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

    public void sendMessage(TextMessage message){
        for (RegisteredClient client : clients) {
            try {

                SocketHints socketHints = new SocketHints();
                socketHints.connectTimeout = 4000; // in milliseconds
                Socket socket = Gdx.net.newClientSocket(Net.Protocol.TCP, client.ip, client.port, socketHints);
                try {
                    Gdx.app.log("Message", message.toString());
                    socket.getOutputStream().write(message.toString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e){
                e.printStackTrace();
                client.shouldBeRemoved = true;
            }
        }
    }

    public void receiveMessage(TextMessage message){
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

    public void cleanUpClients(){
        for (int i = 0; i < clients.size(); i++){
            if (clients.get(i).shouldBeRemoved){
                clients.remove(i);
                i--;
            }
        }
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

    public class RegisteredClient {
        public String ip = "";
        public int port = 0;
        public boolean shouldBeRemoved = false;

        public RegisteredClient(String ip, int port){
            this.ip = ip;
            this.port = port;
        }

        public String toString(){
            return ip + ":" + port;
        }
    }
}
