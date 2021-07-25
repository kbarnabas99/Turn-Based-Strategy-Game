package com.codecool.dungeoncrawl.logic.maps;

import com.codecool.dungeoncrawl.logic.Player;
import com.codecool.dungeoncrawl.logic.drawable.cells.Cell;
import com.codecool.dungeoncrawl.logic.drawable.cells.CellType;
import com.codecool.dungeoncrawl.logic.drawable.troops.Troop;

import java.util.ArrayList;
import java.util.List;

public class GameMap {
    private final int width;
    private final int height;
    private final Cell[][] cells;

    private List<Player> players;

    private Troop selectedTroop;

    public GameMap(int width, int height, CellType defaultCellType) {
        this.width = width;
        this.height = height;
        cells = new Cell[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(this, x, y, defaultCellType);
            }
        }
        players = new ArrayList<>();
        players.add(new Player("player 1"));
        players.add(new Player("player 2"));
    }

    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Troop getSelectedTroop() {
        return selectedTroop;
    }

    public void setSelectedTroop(Troop selectedTroop, Player actualPlayerTurn) {
        if (selectedTroop != null) {
            if (selectedTroop.getPlayer().equals(actualPlayerTurn))
                this.selectedTroop = selectedTroop;
        }
    }

    public Player getPlayer(int playerNumber) {
        return players.get(playerNumber);
    }
}
