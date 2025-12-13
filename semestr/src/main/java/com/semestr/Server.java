package com.semestr;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 5555;
    //имя файла логов, появится в корне папки проекта
    private static final String LOG_FILE = "server_log.txt";
    //список подключённых игроков
    private List<ClientHandler> players = new ArrayList<>();
    //сколько игроков нажали "READY"
    private int readyCount = 0;
    //более красиввый вывод времени ([14:20:14])
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static void main(String[] args) {
        new Server().start();
    }
    public void start() {
        log("====================================================");
        log("Сервер запущен на порту " + PORT);
        log("Логи записываются в файл: " + LOG_FILE);
        log("Ожидание игроков...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //принимаем ровно 2 подключения
            while (players.size() < 2) {
                Socket socket = serverSocket.accept();
                int newId = players.size() + 1;
                ClientHandler player = new ClientHandler(socket, newId);
                players.add(player);
                new Thread(player).start(); //запуск потока для игрока
                log ("Подключился новый клиент (ID " + newId + ")");
            }
            log("Комната заполнена. Ожидание готовности (READY)...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //основной метод логирования, выводит текст и записывает в файл
    //synchronized гарантирует, что записи не перемешаются при одновременных событиях
    private synchronized void log(String message) {
        String time = LocalTime.now().format(timeFormatter);
        String formatterMessage = "[" + time + "]" + message;
        //вывод в консоль (IDE)
        System.out.println(formatterMessage);
        //запись в файл
        //FileWriter(LOG_FILE, true) -> true означает режим "добавления" (append)
        try (PrintWriter  writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(formatterMessage);
        } catch (IOException e) {
            System.err.println("Ошибка записи лога в файл: " + e.getMessage());
        }
    }
    //метод рассылки сообщений
    private synchronized void broadcast(String msg, ClientHandler sender) {
        for (ClientHandler p : players) {
            if (p != sender) {
                p.sendMessage(msg);
            }
        }
    }
    //метод вызывается, когда игрок присылает "READY"
    private synchronized void onPlayerReady(ClientHandler player) {
        readyCount++;
        log("Игрок " + player.nickname + " готов к битве! (" + readyCount + "/2)");
        if (readyCount == 2) {
            log("Оба игрока готовы. Игра начинается!");
            log("Игрок " + players.get(0).nickname + " ходит первым.");
            //первый игрок ходит первым
            players.get(0).sendMessage("GAME_START YOUR_TURN");
            //второй игрок ходит вторым
            players.get(1).sendMessage("GAME_START OPPONENT_TURN");
        }
    }
    //Логика окончания игры
    private synchronized void endGame(ClientHandler loser) {
        //если проигравший прислал WINNER, значит победил другой
        ClientHandler winner = (players.get(0) == loser) ? players.get(1) : players.get(0);
        log("------------------------------------------");
        log("ИГРА ОКОНЧЕНА!");
        log("Победитель: " + winner.nickname.toUpperCase());
        log("Проигравший: " + loser.nickname);
        log("------------------------------------------");
    }
    //внутренний класс, который общается одним конкретным клиентом
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        public int id;
        public String nickname = "Unknown";
        public ClientHandler(Socket socket, int id) {
            this.socket = socket;
            this.id = id;
        }
        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    //логирование ника
                    if (line.startsWith("NICK")) {
                        this.nickname = line.substring(5).trim();
                        log("Игрок #" + id + " представился как: " + nickname);
                        broadcast(line, this);
                    //логирование готовности
                    } else if (line.equals("READY")) {
                        onPlayerReady(this);
                    //логирование выстрелов
                    } else if (line.startsWith("SHOOT")) {
                        String[] parts = line.split(" ");
                        log(this.nickname + " стреляет в клетку [" + parts[1] + ", " + parts[2] + "]");
                        broadcast(line, this);
                    //логирование попаданий\промахов
                    } else if (line.startsWith("RESULT")) {
                        String[] parts = line.split(" ");
                        String resultType = parts[1];
                        String x = parts[2];
                        String y = parts[3];
                        if (resultType.equals("HIT")) {
                            log("  -> ПОПАДАНИЕ по координатам [" + x + ", " + y + "] y " + this.nickname);
                        } else {
                            log("  -> ПРОМАХ по координатам [" + x + ", " + y + "] y " + this.nickname);
                        }
                        broadcast(line, this);
                    //логирование победы
                    } else if (line.equals("WINNER")) {
                        broadcast(line, this);
                        endGame(this);
                    } else {
                        broadcast(line, this);
                    }
                }
                } catch (IOException e) {
                    log("Игрок " + nickname + " (ID " + id + ") отключился.");
            }
        }
        public void sendMessage(String msg) {
            out.println(msg);
        }
    }
}