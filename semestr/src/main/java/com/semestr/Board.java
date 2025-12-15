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
    private boolean isEnemy;
    // счёт сколько осталось кораблей
    public int shipsAliveParts = 0;
    // в каждую клетку добавляется обработчик мыши
    public Board(boolean isEnemy, EventHandler<? super MouseEvent> handler) {
        this.isEnemy = isEnemy;
        for (int y = 0; y < 10; y++) {
            HBox row = new HBox();
            for (int x = 0; x < 10; x++) {
                Cell c = new Cell(x, y);
                c.setOnMouseClicked(handler);
                if (!isEnemy) {
                    c.setOnMouseEntered(handler);
                    c.setOnMouseExited(handler);
                }
                row.getChildren().add(c);
            }
            rows.getChildren().add(row);
        }
        getChildren().add(rows);
    }

    // получение клетку по координатам с защитой от вылета за границы
    public Cell getCell(int x, int y) {
        if (!isValidPoint(x, y))
            return null;
        return (Cell) ((HBox) rows.getChildren().get(y)).getChildren().get(x);
    }

    // вспомогательный метод, проверяет, находится ли координата внутри поля 10х10
    private boolean isValidPoint(int x, int y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }

    // метод для покраски клеток
    public void paintCell(int x, int y, Color color) {
        Cell c = getCell(x, y);
        if (c != null)
            c.setFill(color);
    }

    // проверка, можно ли ставить корабль
    public boolean isValidPlacement(int x, int y, int size, boolean vertical) {
        for (int i = 0; i < size; i++) {
            // вычисление координаты текущего корабля
            int cx = vertical ? x : x + i;
            int cy = vertical ? y + i : y;
            // проверка границ
            if (!isValidPoint(cx, cy))
                return false;
            // проверка занятости клетки
            Cell cell = getCell(cx, cy);
            if (cell.hasShip)
                return false;
            // проверка соседей и квадрата 3х3 вокруг точки (cx, cy)
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int nx = cx + dx;
                    int ny = cy + dy;
                    // если сосед существует и это не мы сами
                    if (isValidPoint(nx, ny)) {
                        if (getCell(nx, ny).hasShip)
                            return false;
                    }
                }
            }
        }
        return true;
    }

    // поставить корабль
    public boolean placeShip(int x, int y, int size, boolean vertical) {
        if (!isValidPlacement(x, y, size, vertical))
            return false;
        for (int i = 0; i < size; i++) {
            int cx = vertical ? x : x + i;
            int cy = vertical ? y + i : y;
            Cell cell = getCell(cx, cy);
            cell.hasShip = true;
            if (!isEnemy)
                cell.setFill(Color.GRAY); // покрас в серый
            shipsAliveParts++;
        }
        return true;
    }

    // подсветка нахождения корабля с учётом vertical
    public void highlight(int x, int y, int size, boolean vertical) {
        boolean valid = isValidPlacement(x, y, size, vertical);
        for (int i = 0; i < size; i++) {
            int cx = vertical ? x : x + i;
            int cy = vertical ? y + i : y;
            if (isValidPoint(cx, cy)) {
                Cell cell = getCell(cx, cy);
                if (!cell.hasShip) {
                    cell.setFill(valid ? Color.LIGHTGREEN : Color.SALMON);
                }
            }
        }
    }

    public void clearColors() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                Cell c = getCell(x, y);
                // не сбрасывается цвет, если попасть в клетку в которую уже стреляли
                if (!c.wasShot) {
                    c.setFill(c.hasShip && !isEnemy ? Color.GRAY : Color.LIGHTBLUE);
                }
            }
        }
    }

    public class Cell extends Rectangle {
        public int x, y;
        public boolean hasShip = false;
        public boolean wasShot = false;

        public Cell(int x, int y) {
            super(30, 30); // размер клетки 30x30 пикселей
            this.x = x;
            this.y = y;
            // настройка внешнего вида
            setFill(Color.LIGHTBLUE); // цвет воды
            setStroke(Color.BLACK); // чёрная рамка
        }
    }
}