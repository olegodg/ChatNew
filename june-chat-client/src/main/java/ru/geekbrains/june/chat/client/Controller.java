package ru.geekbrains.june.chat.client;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import ru.geekbrains.june.chat.server.Authentification;
import java.io.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller {
    @FXML
    TextArea chatArea;
    @FXML
    TextField messageField, usernameField;
    @FXML
    HBox authPanel, msgPanel;
    @FXML
    HBox registredPanel;
    @FXML
    TextField Login;
    @FXML
    PasswordField regPassword;
    @FXML
    TextField Nick;
    @FXML
    ListView<String> clientsListView;
    @FXML
    Label userName;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;

    public void setAuthorized(boolean authorized) {
        userName.setText(name);
        registredPanel.setVisible(true);
        registredPanel.setManaged(true);
        msgPanel.setVisible(authorized);
        msgPanel.setManaged(authorized);
        authPanel.setVisible(!authorized);
        authPanel.setManaged(!authorized);
        clientsListView.setVisible(authorized);
        clientsListView.setManaged(authorized);
    }

    public void registration() throws SQLException {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        Authentification.setNewClients(Login.getText(), regPassword.getText(), Nick.getText());
        Login.clear();
        regPassword.clear();
        Nick.clear();
    }

    public void sendMessage() {
        try {
            out.writeUTF(messageField.getText());//берем текст из текстового поля для отправки
            messageField.clear();//очистка текстового поля
            messageField.requestFocus();//фокусировка на поле для отправки
        } catch (IOException e) {
            showError("Невозможно отправить сообщение на сервер");//вывод сообщения об ошибке
        }
    }

    public void sendCloseRequest() {//запрос на выход из чата
        try {
            if (out != null) {
                out.writeUTF("/exit");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth() {//аутентификация пользователей
        connect();
        try {
            out.writeUTF("/auth " + usernameField.getText());
            name = usernameField.getText();
            usernameField.clear();
        } catch (IOException e) {
            showError("Невозможно отправить запрос авторизации на сервер");
        }
    }

    public void connect() {
        if (socket != null && !socket.isClosed()) {//если порт закрыт или пуст, то лавочку прикрываем
            return;
        }
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> logic()).start();
        } catch (IOException e) {
            showError("Невозможно подключиться к серверу");
        }
    }

    private void logic() {
        try {
            while (true) {
                String inputMessage = in.readUTF();
                if (inputMessage.equals("/exit")) {
                    closeConnection();
                }
                if (inputMessage.equals("/authok")) {
                    setAuthorized(true);
                    break;
                }
                chatArea.appendText(inputMessage + "\n");
            }
            while (true) {
                String inputMessage = in.readUTF();
                if (inputMessage.startsWith("/")) {
                    if (inputMessage.equals("/exit")) {
                        break;
                    }
                    // /clients_list bob john
                    if (inputMessage.startsWith("/clients_list ")) {
                        Platform.runLater(() -> {
                            String[] tokens = inputMessage.split("\\s+");
                            clientsListView.getItems().clear();
                            for (int i = 1; i < tokens.length; i++) {
                                clientsListView.getItems().add(tokens[i]);
                            }
                        });
                    }
                    continue;
                }
                chatArea.appendText(inputMessage + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        setAuthorized(false);
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

    public void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }

    public void clientsListDoubleClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            String selectedUser = clientsListView.getSelectionModel().getSelectedItem();
            messageField.setText("/w " + selectedUser + " ");
            messageField.requestFocus();
            messageField.selectEnd();
        }
    }
    //Логика вроде правильная
    public void historyChat() throws IOException {
        try {
            File history = new File("history.txt");
            if (!history.exists()) {
                history.createNewFile();
            }
            PrintWriter fileWriter = new PrintWriter(new FileWriter(history, false));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(chatArea.getText());
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showHistory() throws IOException {
        File history = new File("history.txt");
        List<String> historyList = new ArrayList<>();
        FileInputStream inStrHistory = new FileInputStream(history);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inStrHistory));
        while ((bufferedReader.readLine()) != null) {
            historyList.add(bufferedReader.readLine());
        }
        if (historyList.size() > 100) {
            for (int i = historyList.size() - 100; i <= (historyList.size() - 1); i++) {
                chatArea.appendText(historyList.get(i) + "\n");
            }

        } else {
            for (int i = 0; i < 100; i++) {
                System.out.println(historyList.get(i));
            }
        }
    }
}

