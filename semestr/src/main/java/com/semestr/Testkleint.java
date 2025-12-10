package com.semestr;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Testkleint {
    public static void main(String[] args) {
        try {
            System.out.println("Подключаемся к серверу...");
            //стучимся на localhost (свой компьютер) порт 5555
            Socket socket = new Socket("localhost", 5555);
            //настройка потоков
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            //сканер, чтобы можно было писать текст в консоли
            Scanner console = new Scanner(System.in);
            //читаем приветствие от сервера
            System.out.println("Сервер ответил: " + in.readLine());
            System.out.println("Напишите что-нибудь (или 'exit' для выхода):");
            while (true) {
                String myMessage = console.nextLine();
                if (myMessage.equals("exit")) break;
                //отправляемся на сервер
                out.println(myMessage);
                //ожидание ответа (эхо)
                String response = in.readLine();
                System.out.println("Сервер ответил: " + response);
            }
            socket.close();
        } catch (IOException e) {
            System.out.println("Не удалось подключиться. Сервер запущен?");
            e.printStackTrace();
        }
    }
}