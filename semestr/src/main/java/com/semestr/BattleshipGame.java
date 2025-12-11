package com.semestr;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BattleshipGame extends Application {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    // GUI
    private Stage window;
    private Label infoLabel = new Label("Подключение........");
    // NETWORK
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;
    // GAME
    private Board myBoard, enemyBoard;
    // SHIPS 5,4,3,2,1
    private final int[] shipsToPlace = { 5, 4, 3, 2, 1 };
    private int currentShipIndex = 0;// Номер корабля
    private boolean setupPhase = true;// Проверка фазы расстановки
    private boolean myTurn = false;// Проверка чей ход

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.window = stage;
        showLoginScreen();
    }

    // Экран Входа
    private void showLoginScreen() {
        window.setTitle("Морской Бой - Вход");
        TextField nickField = new TextField();
        Button btn = new Button("Играть");
        Label status = new Label("");
        status.setTextFill(Color.RED);
        // Действие при нажатии на кнопку
        btn.setOnAction(e -> {
            nickname = nickField.getText();
            if (!nickname.isEmpty())
                new Thread(() -> connect(status)).start();
        });
        VBox root = new VBox(10, new Label("Ник:"), nickField, btn, status);
        root.setAlignment(Pos.CENTER);
        window.setScene(new Scene(root, 300, 200));
        window.show();
    }

    // CONNECT
    private void connect(Label status) {
        try {
            // Создание сокета
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Отправляем серверу наш ник
            // Читаем эхо-приветствие, если сервер его шлет
            out.println("NICK " + nickname);
            // Переход к игре
            Platform.runLater(this::showGameScreen);
            listenForServer();
        } catch (Exception e) {
            Platform.runLater(() -> status.setText("Ошибка Сети"));
        }
    }

    // Listener
    private void listenForServer() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                String msg = line;
                Platform.runLater(() -> processMessage(msg));
            }
        } catch (IOException e) {
            System.out.println("Connection lost");
        }
    }

    // Executor
    private void processMessage(String msg) {
        System.out.println("Message from the server" + msg);
        String[] parts = msg.split("");
        String cmd = parts[0];
        switch (cmd) {
            case "GAME_START":
                if (parts[1].equals("YOUR_TURN")) {
                    myTurn = true;
                    infoLabel.setText("ВАШ ХОД! Атакуйте врага.");
                    infoLabel.setTextFill(Color.GREEN);
                } else {
                    myTurn = false;
                    infoLabel.setText("ХОД ПРОТИВНИКА. Ждите.");
                    infoLabel.setTextFill(Color.RED);
                }
                break;
        }
    }

    // GUI GAME
    private void showGameScreen() {
        window.setTitle("Игрок: " + nickname);
        // Создаем две доски
        myBoard = new Board(false, e -> handleMyBoardClick(e));
        // Доска врага (true = враг)
        enemyBoard = new Board(true, e -> {
        });
        VBox left = new VBox(5, new Label("ВЫ"), myBoard);
        VBox right = new VBox(5, new Label("ВРАГ"), enemyBoard);
        HBox boards = new HBox(30, left, right);
        boards.setAlignment(Pos.CENTER);
        infoLabel.setText("Поставьте корабль длиной" + shipsToPlace[0]);
        infoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: blue;");
        VBox layout = new VBox(20, infoLabel, boards);
        layout.setAlignment(Pos.CENTER);
        window.setScene(new Scene(layout, 800, 500));
    }

    // Placement
    private void handleMyBoardClick(MouseEvent e) {
        // Если расстановка закончена, ничего не делаем
        if (!setupPhase)
            return;
        Board.Cell cell = (Board.Cell) e.getSource();
        // Навели мышку -> Подсветка
        if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
            myBoard.clearColors(); // Сброс старой подсветки
            int size = shipsToPlace[currentShipIndex];
            myBoard.highlight(cell.x, cell.y, size);
        }
        // Убрали мышку -> Очистка
        if (e.getEventType() == MouseEvent.MOUSE_EXITED) {
            myBoard.clearColors();
        }
        // Кликнули ЛКМ -> Установка
        if (e.getButton() == MouseButton.PRIMARY && e.getEventType() == MouseEvent.MOUSE_CLICKED) {
            int size = shipsToPlace[currentShipIndex];
            // Пробуем поставить
            if (myBoard.placeShip(cell.x, cell.y, size)) {
                // Если успешно:
                currentShipIndex++; // Переходим к следующему кораблю
                // Проверяем, остались ли корабли
                if (currentShipIndex >= shipsToPlace.length) {
                    setupPhase = false;
                    myBoard.clearColors();
                    infoLabel.setText("Расстановка завершена! Ждем врага...");
                    out.println("READY"); // Отправляем серверу сигнал
                    System.out.println("Отправлено: READY");
                } else {
                    infoLabel.setText("Поставьте корабль длиной" + shipsToPlace[currentShipIndex]);
                }
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (socket != null)
            socket.close();
        super.stop();
    }
}