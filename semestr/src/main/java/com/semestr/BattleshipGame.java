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
        nickField.setMaxWidth(200);
        Button btn = new Button("Играть");
        VBox root = new VBox(10, new Label("НИК"), nickField, btn);
        root.setAlignment(Pos.CENTER);
        // Действие при нажатии на кнопку
        btn.setOnAction(e -> {
            nickname = nickField.getText();
            if (!nickname.isEmpty())
                new Thread(this::connect).start();
        });
        window.setScene(new Scene(root, 300, 200));
        window.show();
    }

    // CONNECT
    private void connect() {
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
            e.printStackTrace();
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
        String[] parts = msg.split(" ");
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
            // SHOOT
            case "SHOOT":
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                Board.Cell cell = myBoard.getCell(x, y);
                cell.wasShot = true;
                if (cell.hasShip) {
                    // HIT
                    cell.setFill(Color.RED);
                    myBoard.shipsAliveParts--;
                    if (myBoard.shipsAliveParts <= 0) {
                        out.println("WINNER");// Сообщаем о поражении
                        infoLabel.setText("ВЫ ПРОИГРАЛИ :(");
                        infoLabel.setTextFill(Color.RED);
                        myBoard.setDisable(true);
                    } else {
                        out.println("RESULT HIT " + x + " " + y);
                        infoLabel.setText("В НАС ПОПАЛИ");
                    }
                } else {
                    // MISS
                    cell.setFill(Color.BLACK);
                    out.println("RESULT MISS" + x + " " + y);
                    myTurn = true;// Передача хода
                    infoLabel.setText("ВРАГ ПРОМАЗАЛ. ВАШ ХОД");
                    infoLabel.setTextFill(Color.GREEN);
                }
                break;
            // Получение ответа от сервера о попадании или промахе
            case "RESULT":
                int tx = Integer.parseInt(parts[2]);
                int ty = Integer.parseInt(parts[3]);
                if (parts[1].equals("HIT")) {
                    enemyBoard.paintCell(tx, ty, Color.RED);
                    infoLabel.setText("ЕСТЬ ПОПАДАНИИЕ!");
                    myTurn = true;
                } else {
                    enemyBoard.paintCell(tx, ty, Color.BLACK);
                    infoLabel.setText("МИМО....... ХОД ПЕРЕШЕЛ СОПЕРНИКУ");
                    infoLabel.setTextFill(Color.RED);
                    myTurn = false;
                }
                break;
            case "WINNER":
                infoLabel.setText("ПОБЕДА!!!!");
                infoLabel.setTextFill(Color.GOLD);
                myBoard.setDisable(true);
                enemyBoard.setDisable(true);
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
            if (setupPhase || !myTurn)
                return;
            Board.Cell c = (Board.Cell) e.getSource();
            if (c.wasShot)
                return;
            // Временно блокируем, пока ждем ответа
            myTurn = false;
            c.wasShot = true;
            c.setFill(Color.LIGHTGRAY);// Временный цвет
            out.println("SHOOT " + c.x + " " + c.y);
            infoLabel.setText("МИМО........");
            infoLabel.setTextFill(Color.RED);
        });
        VBox left = new VBox(5, new Label("ВЫ"), myBoard);
        VBox right = new VBox(5, new Label("ВРАГ"), enemyBoard);
        HBox boards = new HBox(30, left, right);
        boards.setAlignment(Pos.CENTER);
        infoLabel.setText("Поставьте корабль длиной " + shipsToPlace[0]);
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
            myBoard.highlight(cell.x, cell.y, shipsToPlace[currentShipIndex]);
        } else if (e.getEventType() == MouseEvent.MOUSE_EXITED)// Убрали мышку -> Очистка
        {
            myBoard.clearColors();
        } else if (e.getButton() == MouseButton.PRIMARY && e.getEventType() == MouseEvent.MOUSE_CLICKED)// Кликнули ЛКМ
                                                                                                        // -> Установка
        {
            // Пробуем поставить
            if (myBoard.placeShip(cell.x, cell.y, shipsToPlace[currentShipIndex])) {
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