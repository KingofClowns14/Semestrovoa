package com.semestr;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 5555;
    public static void main(String[] args) {
        System.out.println("Запускаем сервер на порту " + PORT + "...");
        //try-with-resources автоматически закроет сокет, если сервер упадет
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер ждет подключений.");
            //бесконечный цикл, сервер всегда работает
            while (true) {
                //ожидание подключения (программа замирает на этой строке)
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новый клиент подключился!");
                //как только кто-то подключился, создаем для него персонального "менеджера"
                ClientHandler handler = new ClientHandler(clientSocket);
                //запускаем "менеджера" в отдельном потоке
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //внутренний класс, который общается одним конкретным клиентом
    private static class ClientHandler implements Runnable {
        private Socket socket;
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            try {
                //настраиваем каналы для общения
                //читаем от клиента
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //пишем клиенту (autoFlush = true, чтобы сообщение улетало сразу)
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("Привет! Ты подключился к Эхо-Серверу.");
                String message;
                //читаем сообщения, пока клиент не отключится
                while ((message = in.readLine()) != null) {
                    System.out.println("Клиент пишет: " + message);
                    //отправляем ответ обратно (эхо)
                    out.println("Эхо: " + message);
                }
            } catch (IOException e) {
                System.out.println("Клиент отключился.");
            }
        }
    }
}