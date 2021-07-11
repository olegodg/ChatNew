package ru.geekbrains.june.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private String username;
    private DataInputStream in;
    private DataOutputStream out;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());//улучшенный входной поток данных
            this.out = new DataOutputStream(socket.getOutputStream());//улучшенный исходящий поток данных
            server.getClientsExecutorService().execute(() -> logic());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {//отправка сообщений
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logic() {
        try {
            while (!consumeAuthorizeMessage(in.readUTF()));
            while (consumeRegularMessage(in.readUTF()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Клиент " + username + " отключился");
            server.unsubscribe(this);
            closeConnection();
        }
    }

    private boolean consumeRegularMessage(String inputMessage) {
        if (inputMessage.startsWith("/")) {
            if (inputMessage.equals("/exit")) {
                sendMessage("/exit");
                return false;
            }
            if (inputMessage.startsWith("/w ")) {
                String[] tokens = inputMessage.split("\\s+", 3);
                server.sendPersonalMessage(this, tokens[1], tokens[2]);
            }
            return true;
        }
        server.broadcastMessage(username + ": " + inputMessage);
        return true;
    }

    private boolean consumeAuthorizeMessage(String message) {//варианты сообщений при авторизации
        if (message.startsWith("/auth ")) { // /auth bob
            String[] tokens = message.split("\\s+");
            if (tokens.length == 1) {
                sendMessage("SERVER: Вы не указали имя пользователя");
                return false;
            }
            if (tokens.length > 2) {
                sendMessage("SERVER: Имя пользователя не может состоять из нескольких слов");
                return false;
            }
            String selectedUsername = tokens[1];
            if (server.isUsernameUsed(selectedUsername)) {
                sendMessage("SERVER: Данное имя пользователя уже занято");
                return false;
            }
            username = selectedUsername;
            sendMessage("/authok");
            server.subscribe(this);
            return true;
        } else {
            sendMessage("SERVER: Вам необходимо авторизоваться");
            return false;
        }
    }

    private void closeConnection() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void userNameOnForm(){

    }
}
