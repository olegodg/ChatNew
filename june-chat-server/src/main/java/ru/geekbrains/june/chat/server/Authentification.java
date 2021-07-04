package ru.geekbrains.june.chat.server;

import jdk.nashorn.internal.runtime.Context;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Authentification {
    private static Connection connection;
    private static Statement statement;

   /* public static void main(String[] args) {
        try {
            connect();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            disconnect();
        }

    }*/

    public static void connect() throws SQLException {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:listClient.db");
            statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
    public static void createTable() throws SQLException {
        String sql = "Create table if not exists client (" + "ID integer primary key autoincriment not null, " +
                "login text not null, " + "password integer not null" + "nick text not null" + ");";
        statement.executeUpdate(sql);
    }
    public static void setNewClients(String login, String pass, String nick) throws SQLException {
        connect();
        int hash = pass.hashCode();
        String sql = String.format("insert into  (ID, login, password, nickname) VALUES ('%s', '%d', '%s')", login, hash, nick);
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /*
    поидее должно быть что-то такое
    public void changeNick (String oN, String nN) throws SQLException {
        connect();
        try {
            statement.execute("update listClient set nickname=" + nN+"where nickname ="+oN);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    Проверить к сожалению без плагина не получается пока, что да как работает, поэтому пока вслепую*/
}