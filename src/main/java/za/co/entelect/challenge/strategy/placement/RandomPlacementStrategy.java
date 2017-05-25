package za.co.entelect.challenge.strategy.placement;

import za.co.entelect.challenge.PlacementMap;
import za.co.entelect.challenge.Probability;
import za.co.entelect.challenge.domain.command.PlaceShipCommand;
import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.command.direction.Direction;
import za.co.entelect.challenge.domain.command.ship.ShipType;
import za.co.entelect.challenge.domain.state.Cell;
import za.co.entelect.challenge.domain.state.GameState;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class RandomPlacementStrategy extends PlacementStrategy {

    public RandomPlacementStrategy(GameState gameState, ShipType shipType) {
        super(gameState, shipType);
    }

    public Placement getPlacement() {
        Point start = getPosition();
        return new Placement(shipType, start, getDirection(start));
    }

    public Point getPosition() {
        PlacementMap map = new PlacementMap(gameState);
        List<Probability> probabilities = map.getShipProbabilitiesWrapped(shipType);
        List<Probability> options = probabilities.stream().filter(probability -> probability.value > 0).collect(Collectors.toList());
        Collections.shuffle(options);
        return options.get(0).getPoint();
    }

    public Direction getDirection(Point start) {
        List<Direction> directions = Arrays.asList(Direction.values());
        Collections.shuffle(directions);
        for (Direction dir : directions) {
            CanPlace canPlace = canPlace(start, dir);
            if (canPlace.canPlace) {
                return dir;
            }
        }
        return null;
    }

    /*
    public PlaceShipCommand getShipPlacement(GameState gameState) {
        HashMap shipSizes = new HashMap<ShipType, Integer>();
        shipSizes.put(ShipType.Battleship, 4);
        shipSizes.put(ShipType.Carrier, 5);
        shipSizes.put(ShipType.Cruiser, 3);
        shipSizes.put(ShipType.Destroyer, 2);
        shipSizes.put(ShipType.Submarine, 3);

        ArrayList<ShipType> shipPlacements = new ArrayList<>();
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Direction> directions = new ArrayList<>();

        Iterator it = shipSizes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ShipType, Integer> pair = (Map.Entry) it.next();
            ShipWithSize shipWithSize = new ShipWithSize(pair.getKey(), pair.getValue());

            Point location = new Point(ThreadLocalRandom.current().nextInt(0, gameState.PlayerMap.MapWidth - 1), ThreadLocalRandom.current().nextInt(0, gameState.PlayerMap.MapHeight - 1));

            Direction direction;

            CanPlace canPlace = tryToPlace(gameState, shipWithSize.shipSize, location);
            if (canPlace.canPlace) {
                direction = canPlace.direction;
                shipPlacements.add(shipWithSize.shipType);
                points.add(location);
                directions.add(direction);
            }

            it.remove();
        }

        return new PlaceShipCommand(shipPlacements, points, directions);
    }
    */
}

