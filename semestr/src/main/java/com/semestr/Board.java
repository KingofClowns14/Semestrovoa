package com.semestr;

import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

//наследование от Parent, чтобы этот объект можно было добавить в окно
public class Board extends Parent {
    //VBox будет хранить 10 горизонтальных строк
    private VBox rows = new VBox();
    public Board() {
        //цикл по строкам у
        for (int y = 0; y < 10; y++) {
            HBox row = new HBox(); //создание одной строки
            //цикл по столбцам х
            for (int x = 0; x < 10; x++) {
                Cell c = new Cell(x, y);
                row.getChildren().add(c); //добавление клетки в строку
            }
            rows.getChildren().add(row); //добавление готовой строки в столбец
        }
        //добавление всей конструкции в сам объект Board
        getChildren().add(rows);
    }
    //внутренний класс Клетка
    public class Cell extends Rectangle {
        public int x, y;
        public Cell(int x, int y) {
            super(30, 30); //размер клетки 30x30 пикселей
            this.x = x;
            this.y = y;
            //настройка внешнего вида
            setFill(Color.LIGHTBLUE); //цвет воды
            setStroke(Color.BLACK);   //чёрная рамка
        }
    }
}