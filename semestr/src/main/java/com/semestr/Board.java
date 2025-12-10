package com.semestr;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Board extends Parent {
    private VBox rows = new VBox();
    //флаг: если враг, то не будет видно его кораблей при расстановке
    private boolean isEnemy;
    //конструктор теперь принимает обработчик мышки
    public Board(boolean isEnemy, EventHandler<? super MouseEvent> handler) {
        this.isEnemy = isEnemy;
        for (int y = 0; y < 10; y++) {
            HBox row = new HBox();
            for (int x = 0; x < 10; x++) {
                Cell c = new Cell(x, y);
                //передача кликов и движения мыши в главный класс
                c.setOnMouseClicked(handler);
                c.setOnMouseEntered(handler);
                c.setOnMouseExited(handler);
                row.getChildren().add(c);
            }
            rows.getChildren().add(row);
        }
        getChildren().add(rows);
    }
    //получение клетку по координатам с защитой от вылета за границы
    public Cell getCell(int x, int y) {
        if (x < 0 || x >= 10 || y < 0 || y >= 10) return null;
        return (Cell) ((HBox) rows.getChildren().get(y)).getChildren().get(x);
    }
    //проверка на существование и свободу клетки
    public boolean isValidPlacement(int x, int y, int size) {
        for (int i = 0; i< size; i++) {
            Cell c = getCell(x + i, y);
            //если вышло за карту или клетка занята, то нельзя
            if (c == null || c.hasShip) return false;
        }
        return true;
    }
    //поставить корабль
    public boolean placeShip(int x, int y, int size) {
        if (!isValidPlacement(x, y, size)) return false;
        for (int i = 0; i < size; i++) {
            Cell c = getCell(x + i, y);
            c.hasShip = true;
            if (!isEnemy) c.setFill(Color.GRAY); //покрас в серый
        }
        return true;
    }
    //подсветка при наведении
    public void highlight(int x, int y, int size) {
        boolean valid = isValidPlacement(x, y, size);
        for (int i = 0; i < size; i++) {
            Cell c = getCell(x + i, y);
            if (c != null && !c.hasShip) {
                c.setFill(valid ? Color.LIGHTGREEN : Color.SALMON);
            }
        }
    }
    //убрать подсветку
    public void clearColors() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                Cell c = getCell(x, y);
                if (c.hasShip) {
                    c.setFill(isEnemy ? Color.LIGHTBLUE : Color.GRAY);
                } else {
                    c.setFill(Color.LIGHTBLUE);
                }
                c.setStroke(Color.BLACK);
            }
        }
    }
    public class Cell extends Rectangle {
        public int x, y;
        public boolean hasShip = false;
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