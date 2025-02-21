package za.co.entelect.challenge.strategy.placement;

import za.co.entelect.challenge.PlacementMap;
import za.co.entelect.challenge.Probability;
import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.command.direction.Direction;
import za.co.entelect.challenge.domain.command.ship.ShipType;
import za.co.entelect.challenge.domain.state.Cell;
import za.co.entelect.challenge.domain.state.GameState;

import java.util.*;
import java.util.stream.Collectors;

public class LowestPlacementStrategy extends PlacementStrategy {

    public LowestPlacementStrategy(GameState gameState, ShipType shipType) {
        super(gameState, shipType);
    }

    public Placement getPlacement() {
        Point start = getPosition();
        return new Placement(shipType, start, getDirection(start));
    }

    private boolean isTooCloseToCorner(Point p) {
        int mapDimension = gameState.MapDimension;
        int x = p.getX();
        int y = p.getY();

        if (x < 2 && y < 2) {
            return true;
        }

        if (x < 2 && y > mapDimension - 3) {
            return true;
        }

        if (x > mapDimension - 3 && y < 2) {
            return true;
        }

        if (x > mapDimension -3 && y > mapDimension - 3) {
            return true;
        }

        return false;
    }

    public Point getPosition() {
        PlacementMap map = new PlacementMap(gameState);
        List<Probability> probabilities = map.getProbabilitiesWrapped();
        List<Probability> options = probabilities
                .stream()
                .filter(p -> p.value > 0.1 && !isTooCloseToCorner(p.getPoint()))
                .collect(Collectors.toList());
        Collections.sort(options, (o1, o2) -> Float.compare(o1.value, o2.value));
        List<Probability> randomOptions = options.subList(0, 10);
        Collections.shuffle(randomOptions);
        return randomOptions.get(0).getPoint();
    }

    public Direction getDirection(Point start) {
        PlacementMap map = new PlacementMap(gameState);
        float[][] input = map.getShipProbabilities(shipType);

        Map<Direction, Float> sumProbs = new HashMap<>();
        for (Direction dir : Direction.values()) {
            CanPlace canPlace = canPlace(start, dir);
            if (!canPlace.canPlace) {
                continue;
            }

            float sum = 0;
            for (Cell cell : canPlace.cells) {
                sum += input[cell.X][cell.Y];
            }

            sumProbs.put(dir, sum);
        }

        Direction bestDir = null;
        float bestVal = Float.MAX_VALUE;
        for (Map.Entry<Direction, Float> entry : sumProbs.entrySet()) {
            if (entry.getValue() < bestVal) {
                bestVal = entry.getValue();
                bestDir = entry.getKey();
            }
        }

        return bestDir;
    }
}

