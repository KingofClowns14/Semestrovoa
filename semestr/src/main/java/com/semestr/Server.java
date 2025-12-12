package com.semestr;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 5555;
    //список подключённых игроков
    private List<ClientHandler> players = new ArrayList<>();
    //сколько игроков нажали "READY"
    private int readyCount = 0;
    public static void main(String[] args) {
        new Server().start();
    }
    public void start() {
        System.out.println("Сервер запущен...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //принимаем ровно 2 подключения
            while (players.size() < 2) {
                Socket socket = serverSocket.accept();
                ClientHandler player = new ClientHandler(socket, players.size() + 1);
                players.add(player);
                new Thread(player).start(); //запуск потока для игрока
                System.out.println("Игрок подключился.");
            }
        } catch (IOException e) {
            e.printStackTrace();
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
    private synchronized void onPlayerReady() {
        readyCount++;
        if (readyCount == 2) {
            System.out.println("Оба готовы! Начинаем игру.");
            //первый игрок ходит первым
            players.get(0).sendMessage("GAME_START YOUR_TURN");
            //второй игрок ходит вторым
            players.get(1).sendMessage("GAME_START OPPONENT_TURN");
        }
    }
    //внутренний класс, который общается одним конкретным клиентом
    private class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        public int id;
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
                    //логика обработки сообщений
                    if (line.startsWith("NICK")) {
                    } else if (line.equals("READY")) {
                        //сообщение главному серверу, что этот игрок готов
                        onPlayerReady();
                    } else {
                        broadcast(line, this);
                    }
                }
            } catch (IOException e) {
                System.out.println("Игрок отключился.");
            }
        }
        public void sendMessage(String msg) {
            out.println(msg);
        }
    }
}