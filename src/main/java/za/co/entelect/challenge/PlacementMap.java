package za.co.entelect.challenge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.command.direction.Direction;
import za.co.entelect.challenge.domain.command.ship.ShipType;
import za.co.entelect.challenge.domain.state.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlacementMap {

    private Logger logger = LoggerFactory.getLogger(PlacementMap.class);

    private GameState gameState;

    private Map<ShipType, int[][]> shipPossiblities;
    private Map<ShipType, float[][]> shipProbabilities;
    private float[][] probabilities;
    private int[][] possibilities;

    public PlacementMap(GameState gameState) {
        this.gameState = gameState;
        update();
    }

    public float[][] getProbabilities() {
        return probabilities;
    }

    public List<Probability> getProbabilitiesWrapped() {
        List<Probability> result = new ArrayList<>();
        for (int i = 0; i < gameState.MapDimension; i++) {
            for (int j = 0; j < gameState.MapDimension; j++) {
                result.add(new Probability(new Point(j, i), probabilities[j][i]));
            }
        }
        return result;
    }

    public List<Probability> getShipProbabilitiesWrapped(ShipType shipType) {
        List<Probability> result = new ArrayList<>();
        float[][] prob = shipProbabilities.get(shipType);
        for (int i = 0; i < gameState.MapDimension; i++) {
            for (int j = 0; j < gameState.MapDimension; j++) {
                result.add(new Probability(new Point(j, i), prob[j][i]));
            }
        }
        return result;
    }

    public float getProbability(int x, int y) {
        return probabilities[x][y];
    }

    public int[][] getPossibilities() {
        return possibilities;
    }

    public int[][] getShipPossibilities(ShipType shipType) {
        return shipPossiblities.get(shipType);
    }

    public float[][] getShipProbabilities(ShipType shipType) {
        return shipProbabilities.get(shipType);
    }

    public void update() {
        long startTime = System.currentTimeMillis();
        int mapDimension = gameState.MapDimension;
        probabilities = new float[mapDimension][mapDimension];
        possibilities = new int[mapDimension][mapDimension];

        shipPossiblities = new HashMap<>();
        shipProbabilities = new HashMap<>();
        for (Ship ship : gameState.PlayerMap.Owner.Ships) {
            int[][] shipPossibilities = getShipPossiblities(ship);
            shipPossiblities.put(ship.ShipType, shipPossibilities);

            float[][] shipProbability = normalizeArray(shipPossibilities, mapDimension);
            shipProbabilities.put(ship.ShipType, shipProbability);

            for (int i = 0; i < mapDimension; i++) {
                for (int j = 0; j < mapDimension; j++) {
                    possibilities[j][i] += shipPossibilities[j][i];
                }
            }
        }
        probabilities = normalizeArray(possibilities, mapDimension);
        logger.info("Probabilities took {}ms", System.currentTimeMillis() - startTime);
    }

    private float[][] normalizeArray(int[][] input, int dimension) {
        float max = Integer.MIN_VALUE;
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (input[j][i] > max) {
                    max = input[j][i];
                }
            }
        }
        assert max >= 0;
        float[][] output = new float[dimension][dimension];
        if (max == 0) {
            return output;
        }

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                output[j][i] = input[j][i] / max;
            }
        }

        return output;
    }

    private int[][] getShipPossiblities(Ship ship) {
        PlayerMap playerMap = gameState.PlayerMap;
        int mapDimension = gameState.MapDimension;
        int[][] shipPossibility = new int[mapDimension][mapDimension];

        if (ship.Placed) {
            return shipPossibility;
        }

        for (int i = 0; i < mapDimension; i++) {
            for (int j = 0; j < mapDimension; j++) {
                Cell cell = playerMap.getCellAt(j, i);
                if (cell.Occupied) {
                    shipPossibility[j][i] = 0;
                } else {
                    updateShipPossiblities(ship, shipPossibility, j, i);
                }
            }
        }
        
        return shipPossibility;
    }

    private int updateShipPossiblities(Ship ship, int[][] shipPossibility, int x, int y) {
        PlayerMap playerMap = gameState.PlayerMap;
        ShipType shipType = ship.ShipType;
        int result = 0;
        for (Direction dir : Direction.values()) {
            if (playerMap.hasCellsForDirection(new Point(x, y), dir, shipType.getSize())) {
                for (Cell cell : playerMap.getAllCellsInDirection(new Point(x, y), dir, shipType.getSize())) {
                    shipPossibility[cell.X][cell.Y] += ship.ShipType.getSize() - Util.dist(cell.getPoint(), new Point(x, y));
                }
            }
        }
        return result;
    }
}
