package za.co.entelect.challenge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import za.co.entelect.challenge.domain.command.Command;
import za.co.entelect.challenge.domain.command.PlaceShipCommand;
import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.command.direction.Direction;
import za.co.entelect.challenge.domain.command.ship.ShipType;
import za.co.entelect.challenge.domain.state.Cell;
import za.co.entelect.challenge.domain.state.GameState;
import za.co.entelect.challenge.domain.state.OpponentCell;
import za.co.entelect.challenge.domain.state.OpponentShip;
import za.co.entelect.challenge.strategy.placement.LowestPlacementStrategy;
import za.co.entelect.challenge.strategy.placement.Placement;
import za.co.entelect.challenge.strategy.placement.RandomPlacementStrategy;
import za.co.entelect.challenge.strategy.shoot.HuntShootStrategy;
import za.co.entelect.challenge.strategy.placement.PlacementStrategy;
import za.co.entelect.challenge.strategy.shoot.TargetShootStrategy;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BotStrategy {

    private static final Logger logger = LoggerFactory.getLogger(BotStrategy.class);

    private BotState botState;
    private GameState gameState;

    public BotStrategy(GameState gameState, BotState botState) {
        this.botState = botState;
        this.gameState = gameState;
    }

    public Command getFireCommand() {
        if (gameState.Phase == 1) {
            return null;
        } else {
            updateBotState();
            Command fireCommand = makeMove();
            return fireCommand;
        }
    }

    public PlaceShipCommand placeShips() {
        GameState copy = gameState.deepCopy();
        ArrayList<ShipType> shipPlacements = new ArrayList<>();
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Direction> directions = new ArrayList<>();

        List<ShipType> placementOrder = Arrays.asList(ShipType.values());
        placementOrder.sort((o1, o2) -> Integer.compare(o1.getSize(), o2.getSize()));

        for (ShipType shipType : placementOrder) {
            final PlacementStrategy placementStrategy;
            switch (shipType) {
                case Destroyer:
                case Submarine:
                    placementStrategy = new LowestPlacementStrategy(copy, shipType);
                    break;
                default:
                    placementStrategy = new RandomPlacementStrategy(copy, shipType);
                    break;
            }

            logger.info("Placed {} with {}", shipType, placementStrategy.getClass().getName());

            Placement placement = placementStrategy.getPlacement();
            shipPlacements.add(placement.shipType);
            points.add(placement.start);
            directions.add(placement.direction);

            List<Cell> cells = copy.PlayerMap.getAllCellsInDirection(placement.start, placement.direction, shipType.getSize());
            cells.forEach(cell -> cell.Occupied = true);
        }
        PlaceShipCommand placeShipCommand = new PlaceShipCommand(shipPlacements, points, directions);
        if (!placeShipCommand.isValid()) {
            return placeShips();
        }
        return placeShipCommand;
    }

    private Command makeMove() {
        if (botState.Mode == BotMode.HUNT) {
            return new HuntShootStrategy().executeStrategy(gameState, botState);
        }
        return new TargetShootStrategy().executeStrategy(gameState, botState);
    }


    private void updateBotState(){
        Point lastShot = botState.LastShot;
        if (lastShot != null) {
            OpponentCell opponentCell = gameState.OpponentMap.getCellAt(lastShot.x, lastShot.y);
            if (opponentCell.Damaged) {
                botState.LastTrackerHits.add(lastShot);

                if (botState.Mode == BotMode.HUNT) {
                    botState.Mode = BotMode.TARGET;
                }
            }
        }

        List<Point> lastHits = botState.LastTrackerHits;
        for (OpponentShip ship : gameState.OpponentMap.Ships) {
            boolean destroyed = botState.LastOpponentShipStatus.get(ship.ShipType);
            if (ship.Destroyed && !destroyed) {
                botState.LastDestroyedShip = ship.ShipType;
                int size = ship.ShipType.getSize();
                List<List<Point>> p = getDestroyedShipPossibilities(size);
                if (p.size() > 1) {
                    lastHits.removeAll(p.get(0));
                    logger.warn("We have multiple potentials for " + ship.ShipType);
                } else if (p.size() > 0) {
                    lastHits.removeAll(p.get(0));
                    logger.info("Removing " + ship.ShipType + " from last hit targets");
                } else {
                    logger.warn("Got no matching potentials for " + ship.ShipType);
                }
            }
            botState.LastOpponentShipStatus.put(ship.ShipType, ship.Destroyed);
        }

        if (lastHits.size() == 0) {
            botState.Mode = BotMode.HUNT;
        }
    }

    public List<List<Point>> getDestroyedShipPossibilities(int size) {
        List<List<Point>> result = new ArrayList<>();

        List<Point> lastHits = botState.LastTrackerHits;

        Point lastHit = lastHits.get(lastHits.size() - 1);
        for (Direction dir : Direction.values()) {
            PotentialSink potentialSink = isPotential(size, lastHit, dir, lastHits);
            if (potentialSink.isPotentialSink) {
                result.add(potentialSink.points);
            }
        }
        return result;
    }

    private PotentialSink isPotential(int size, Point point, Direction dir, List<Point> lastHits) {
        PotentialSink result = new PotentialSink();

        if (gameState.OpponentMap.hasCellsForDirection(point, dir, size)) {
            List<OpponentCell> cells = gameState.OpponentMap.getAllCellsInDirection(point, dir, size, false);
            for (OpponentCell cell : cells) {
                if (!lastHits.contains(cell.getPoint())) {
                    result.isPotentialSink = false;
                    return result;
                }
            }
            result.isPotentialSink = true;
            result.points = cells.stream().map(cell -> cell.getPoint()).collect(Collectors.toList());
        }
        return result;
    }

    class PotentialSink {
        boolean isPotentialSink;
        List<Point> points;

        public PotentialSink() {
        }

        public PotentialSink(boolean isPotentialSink, List<Point> points) {
            this.isPotentialSink = isPotentialSink;
            this.points = points;
        }
    }
}
