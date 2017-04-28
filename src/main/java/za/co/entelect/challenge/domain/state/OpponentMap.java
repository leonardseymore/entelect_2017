package za.co.entelect.challenge.domain.state;

import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.command.direction.Direction;
import za.co.entelect.challenge.domain.command.ship.ShipType;
import za.co.entelect.challenge.strategy.RandomPlacementStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OpponentMap {

    public boolean Alive;

    public int Points;

    public String Name;

    public ArrayList<OpponentShip> Ships;

    public ArrayList<OpponentCell> Cells;

    private OpponentCell[][] opponentCells;

    private int MapDimension = Integer.MIN_VALUE;

    private int GetMapDimension() {
        if (MapDimension == Integer.MIN_VALUE) {
            MapDimension = (int) Math.sqrt(Cells.size());
        }
        return MapDimension;
    }

    public OpponentCell getCellAt(int x, int y) {
        int MapDimension = GetMapDimension();
        if (x < 0 || x > MapDimension - 1 || y < 0 || y > MapDimension - 1) {
            return null;
        }

        if (opponentCells == null) {
            opponentCells = new OpponentCell[MapDimension][MapDimension];
            for (OpponentCell cell : Cells) {
                opponentCells[cell.X][cell.Y] = cell;
            }
        }
        return opponentCells[x][y];
    }

    public OpponentCell getAdjacentCell(OpponentCell cell, Direction direction) {
        if (cell == null) {
            return null;
        }

        switch (direction) {
            case North:
                return getCellAt(cell.X, cell.Y + 1);
            case East:
                return getCellAt(cell.X + 1, cell.Y);
            case South:
                return getCellAt(cell.X, cell.Y - 1);
            case West:
                return getCellAt(cell.X - 1, cell.Y);
            default:
                throw new IllegalArgumentException(String.format("The direction passed %s does not exist", direction));
        }
    }

    public ArrayList<OpponentCell> getAllCellsInDirection(Point startLocation, Direction direction, int length, boolean stopOnEmpty) {
        OpponentCell startCell = getCellAt(startLocation.getX(), startLocation.getY());
        ArrayList<OpponentCell> cells = new ArrayList<>();
        cells.add(startCell);

        if (startCell == null) {
            return cells;
        }

        for (int i = 1; i < length; i++) {
            OpponentCell nextCell = getAdjacentCell(startCell, direction);
            if (nextCell == null) {
                if (stopOnEmpty) {
                    break;
                }
                throw new IllegalArgumentException("Not enough cells for the requested length");
            }

            cells.add(nextCell);
            startCell = nextCell;
        }

        return cells;
    }

    public boolean hasCellsForDirection(Point startLocation, Direction direction, int length) {
        OpponentCell startCell = getCellAt(startLocation.getX(), startLocation.getY());

        if (startCell == null) {
            return false;
        }

        for (int i = 1; i < length; i++) {
            OpponentCell nextCell = getAdjacentCell(startCell, direction);
            if (nextCell == null) {
                return false;
            }
            startCell = nextCell;
        }
        return true;
    }

    public CanBeShip canBeShip(Point location, Direction dir, int size) {
        if (!hasCellsForDirection(location, dir, size)) {
            return new CanBeShip(null, false);
        }

        ArrayList<OpponentCell> cells = getAllCellsInDirection(location, dir, size, false);
        CanBeShip result = new CanBeShip();
        boolean canBeShip = true;
        if (cells.stream().anyMatch(x -> x.Missed)) {
            canBeShip = false;
        }
        result.CanBeShip = canBeShip;
        result.OpponentCells = cells;

        return result;
    }
}
