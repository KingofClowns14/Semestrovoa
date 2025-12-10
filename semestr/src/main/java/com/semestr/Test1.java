package com.semestr;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Test1 extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        //создание доски
        Board testBoard = new Board();
        //кладем доску в контейнер
        StackPane root = new StackPane(testBoard);
        //cоздание сцены
        Scene scene = new Scene(root, 400, 400);
        //настраиваем и показываем окно
        primaryStage.setTitle("Шаг 1: Тест Доски");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}