package za.co.entelect.challenge.strategy.placement;

import za.co.entelect.challenge.domain.command.PlaceShipCommand;
import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.command.direction.Direction;
import za.co.entelect.challenge.domain.command.ship.ShipType;
import za.co.entelect.challenge.domain.state.Cell;
import za.co.entelect.challenge.domain.state.GameState;
import za.co.entelect.challenge.domain.state.Ship;

import java.util.ArrayList;
import java.util.Collections;

public abstract class PlacementStrategy {

    public GameState gameState;
    public ShipType shipType;

    public PlacementStrategy(GameState gameState, ShipType shipType) {
        this.gameState = gameState;
        this.shipType = shipType;
    }

    public abstract Placement getPlacement();

    /*
    public void placeShip(Placement placement) {
        Point bestPosition = getPosition();
        Direction bestDirection = getDirection();

        ArrayList<Cell> cells = gameState.PlayerMap.getAllCellsInDirection(bestPosition, bestDirection, shipType.getSize());
        cells.forEach((x) -> x.Occupied = true);
        for (Ship ship : gameState.PlayerMap.Owner.Ships) {
            if (ship.ShipType == shipType) {
                ship.Placed = true;
            }
        }
        return new Placement(shipType, bestPosition, bestDirection);
    }



    public PlaceShipCommand getShipPlacement(GameState gameState) {
        ArrayList<ShipType> shipPlacements = new ArrayList<>();
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Direction> directions = new ArrayList<>();

        for (ShipType shipType : ShipType.values()) {
            Point bestPosition = getHighestPosition(gameState, shipType);
            Direction bestDirection = getHighestDirection(gameState, shipType, bestPosition);

            ArrayList<Cell> cells = gameState.PlayerMap.getAllCellsInDirection(bestPosition, bestDirection, shipType.getSize());
            cells.forEach((x) -> x.Occupied = true);

            shipPlacements.add(shipType);
            points.add(bestPosition);
            directions.add(bestDirection);
        }
        return new PlaceShipCommand(shipPlacements, points, directions);
    }
    */

    protected CanPlace canPlace(Point location, Direction direction) {
        CanPlace result = new CanPlace();

        if (!gameState.PlayerMap.hasCellsForDirection(location, direction, shipType.getSize())) {
            result.canPlace = false;
            return result;
        }

        ArrayList<Cell> cells = gameState.PlayerMap.getAllCellsInDirection(location, direction, shipType.getSize());
        if (cells.stream().anyMatch(x -> x.Occupied)) {
            result.canPlace = false;
            return result;
        }

        result.canPlace = true;
        result.cells = cells;

        return result;
    }
}

