package com.codecool.dungeoncrawl.logic;

import com.codecool.dungeoncrawl.logic.drawable.cells.Cell;
import com.codecool.dungeoncrawl.logic.drawable.troops.Troop;
import com.codecool.dungeoncrawl.logic.maps.GameMap;
import com.codecool.dungeoncrawl.logic.maps.MapLoader;
import com.codecool.dungeoncrawl.logic.tiles.Tiles;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class GameLoop {

    Player actualTurnPlayer;
    List<String> movableCellTypes = Arrays.asList("ground_1", "bridge_1");

    GameMap map = MapLoader.loadMapFromCsv(1);
    Canvas canvas = new Canvas(
            map.getWidth() * Tiles.TILE_WIDTH,
            map.getHeight() * Tiles.TILE_WIDTH);
    GraphicsContext context = canvas.getGraphicsContext2D();
    VBox rightLogBox;

    public void start(Stage primaryStage) {

        canvas.setOnMouseClicked(this::setOnMouseClicked);

        Button passTurnButton = new Button("Pass Turn!");
        passTurnButton.setPrefSize(100, 20);
        passTurnButton.setOnMouseClicked(this::passTurn);

        Button clearLogBoxButton = new Button("Clear Right Log Box!");
        clearLogBoxButton.setPrefSize(200, 20);
        clearLogBoxButton.setOnMouseClicked(this::clearVBoxEvent);

        HBox hbox = new HBox();
        hbox.getChildren().add(passTurnButton);
        hbox.getChildren().add(clearLogBoxButton);

        rightLogBox = new VBox();
        rightLogBox.getChildren().add(new Hyperlink("Log output: "));

        ScrollPane scroll = new ScrollPane();
        scroll.setMinWidth(500);
        scroll.setContent(rightLogBox);

        BorderPane borderPane = new BorderPane();
        borderPane.setOnKeyPressed(this::setKeyEvents);
        borderPane.setCenter(canvas);
        borderPane.setBottom(hbox);
        borderPane.setRight(scroll);

        Scene scene = new Scene(borderPane);

        primaryStage.setScene(scene);
        primaryStage.show();

        actualTurnPlayer = map.getPlayer(0);

        refresh();
    }

    private void passTurn(MouseEvent mouseEvent) {
        if (actualTurnPlayer.equals(map.getPlayer(0)))
            actualTurnPlayer = map.getPlayer(1);
        else
            actualTurnPlayer = map.getPlayer(0);
        map.setSelectedTroopToNull();
    }


    private void setOnMouseClicked(MouseEvent mouseEvent) {
        // getting data about the place where mouse click occured
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        Cell cell = map.getCell((int) x / 32, (int) y / 32);
        Troop troop = cell.getTroop();

        if (troop != null)
            map.setSelectedTroop(troop, actualTurnPlayer);
        else
            map.setSelectedTroopToNull();

        Troop selectedTroop = map.getSelectedTroop();
        String tileName = cell.getTileName();

        // logging to vbox
        clearVBox();
        logToVBox("\n");
        logToVBox("Troop: " + (troop == null ? "null" : troop.toString()));
        logToVBox("Tile name: " + tileName);
        logToVBox("Actual round player: " + actualTurnPlayer);
        logToVBox("Selected troop: " + (selectedTroop == null ? "null" : selectedTroop.toString()));
    }

    private void setKeyEvents(KeyEvent keyEvent) {
        Troop selectedTroop = map.getSelectedTroop();
        if (selectedTroop != null) {
            // 0, 0 -> not move
            int xDirection = 0;
            int yDirection = 0;
            switch (keyEvent.getCode()) {
                case W -> {
                    xDirection = 0;
                    yDirection = -1;
                }
                case S -> {
                    xDirection = 0;
                    yDirection = 1;
                }
                case A -> {
                    xDirection = -1;
                    yDirection = 0;
                }
                case D -> {
                    xDirection = 1;
                    yDirection = 0;
                }
            }
            if (freeToMove(selectedTroop, xDirection, yDirection))
                selectedTroop.move(xDirection, yDirection);
            else if (canAttack(selectedTroop, xDirection, yDirection)) {
                selectedTroop.attack(xDirection, yDirection);
                if (selectedTroop.getHealth() < 1) {
                    logToVBox("Selected troop died!");
                    selectedTroop.getCell().setTroop(null);
                    selectedTroop.getPlayer().removeTroop(selectedTroop);
                    map.setSelectedTroopToNull();
                }
            }
            refresh();
        }
    }

    private boolean freeToMove(Troop selectedTroop, int xDirection, int yDirection) {
        return selectedTroop.getCell().getNeighbor(xDirection, yDirection).getTroop() == null
                && movableCellTypes.contains(selectedTroop.getCell().getNeighbor(xDirection, yDirection).getTileName());
    }

    private boolean canAttack(Troop selectedTroop, int xDirection, int yDirection) {
        Troop troopToAttack = selectedTroop.getCell().getNeighbor(xDirection, yDirection).getTroop();
        if (troopToAttack != null) {
            if (!troopToAttack.getPlayer().equals(selectedTroop.getPlayer()))
                return true;
        }
        return false;
    }

    private void refresh() {
        // filling screen with black
        context.setFill(Color.BLACK);
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // redrawing every Tile
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Cell cell = map.getCell(x, y);
                if (cell.getTroop() != null) {
                    // draw troop to map
                    Tiles.drawTile(context, cell.getTroop(), x, y);

                    // draw health bar under the troop
                    Troop troop = cell.getTroop();
                    context.setFill(Color.GREEN);
                    context.fillRect(cell.getX() * 32,
                            cell.getY() * 32 + 29,
                            32 * (troop.getHealth() / troop.getMaxHealth()),
                            3);
                    context.setStroke(troop.getPlayer().getColor());
                    context.strokeRect(cell.getX() * 32,
                            cell.getY() * 32,
                            32,
                            32);
                } else {
                    Tiles.drawTile(context, cell, x, y);
                }
            }
        }
    }

    public void logToVBox(String log) {
        rightLogBox.getChildren().add(new Hyperlink(log));
    }

    public void clearVBoxEvent(MouseEvent event) {
        clearVBox();
    }

    public void clearVBox() {
        rightLogBox.getChildren().clear();
    }


}
