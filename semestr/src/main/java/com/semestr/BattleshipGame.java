package com.semestr;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

 public class BattleshipGame extends Application{
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT=5555;
    //GUI
    private Stage window;
    private Label infoLabel = new Label("Ожидание подключения........");
    //NETWORK
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    public static void main(String[] args){
        launch(args);
    }
    @Override
    public void start(Stage stage){
        this.window = stage;
        showLoginScreen();
    }
    //Экран Входа
    private void showLoginScreen(){
        window.setTitle("Морской Бой - Вход");
        TextField nickField = new TextField();
        nickField.setPromptText("Введите Ник");
        Button btnConnect = new Button("Подключиться");
        Label statusLabel = new Label("");
        statusLabel.setTextFill(Color.RED);
        //Действие при нажатии на кнопку
        btnConnect.setOnAction(e ->{
            String name = nickField.getText().trim();
            if (name.isEmpty()){
                statusLabel.setText("Имя не может быть пустым!");
                return;
            }
            this.nickname = name;
            statusLabel.setText("Подключение ");
            //Запускаем подключение в отдельном потоке,чтобы окно не зависло
            new Thread(()-> connectToServer(statusLabel)).start();
        });
        VBox layout = new VBox(10,new Label("Введите ник"),nickField,btnConnect,statusLabel);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new javafx.geometry.Insets(20));
        window.setScene(new Scene(layout,300,200));
        window.show();
    }
    //CONNECT
     private void connectToServer(Label statusLabel) {
        try {
            //Создание сокета
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Читаем приветствие от сервера 
            String serverMsg = in.readLine(); 
            System.out.println("Сервер говорит: " + serverMsg);
            //Отправляем серверу наш ник
            out.println("NICK " + nickname);
            //Переход к игре
            Platform.runLater(() -> {
                showGameScreen();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
        }
    }

    //GUI GAME
    private void showGameScreen() {
        window.setTitle("Игрок: " + nickname);
        //Создаем две доски
        Board myBoard = new Board(); //Левая доска
        Board enemyBoard = new Board(); //Правая доска
        //Подписываем их
        VBox leftBox = new VBox(10, new Label("МОЕ ПОЛЕ"), myBoard);
        VBox rightBox = new VBox(10, new Label("ПОЛЕ ВРАГА"), enemyBoard);
        leftBox.setAlignment(Pos.CENTER);
        rightBox.setAlignment(Pos.CENTER);
        //Ставим их рядом
        HBox boardsLayout = new HBox(30, leftBox, rightBox);
        boardsLayout.setAlignment(Pos.CENTER);
        //Общая компоновка
        VBox mainLayout = new VBox(20, infoLabel, boardsLayout);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new javafx.geometry.Insets(20));
        window.setScene(new Scene(mainLayout, 850, 500));
        window.centerOnScreen();
    }

    @Override
    public void stop() throws Exception {
        //Закрываем сокет при закрытии окна
        if (socket != null) socket.close();
        super.stop();
    }
 }  