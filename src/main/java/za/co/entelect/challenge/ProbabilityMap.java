package za.co.entelect.challenge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.command.direction.Direction;
import za.co.entelect.challenge.domain.command.ship.ShipType;
import za.co.entelect.challenge.domain.state.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProbabilityMap {

    private Logger logger = LoggerFactory.getLogger(ProbabilityMap.class);

    private GameState gameState;
    private BotState botState;
    private ProbabilityMapType type;

    private Map<ShipType, int[][]> shipPossiblities;
    private Map<ShipType, float[][]> shipProbabilities;
    private float[][] probabilities;
    private int[][] possibilities;

    public ProbabilityMap(GameState gameState, ProbabilityMapType type, BotState botState) {
        this.gameState = gameState;
        this.botState = botState;
        this.type = type;
        update();
    }

    public float[][] getProbabilities() {
        return probabilities;
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
        for (OpponentShip opponentShip : gameState.OpponentMap.Ships) {
            int[][] shipPossibilities = getShipPossiblities(opponentShip);
            shipPossiblities.put(opponentShip.getShipType(), shipPossibilities);

            float[][] shipProbability = normalizeArray(shipPossibilities, mapDimension);
            shipProbabilities.put(opponentShip.getShipType(), shipProbability);

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
        float max = 0;
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (input[j][i] > max) {
                    max = input[j][i];
                }
            }
        }

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

    private int[][] getShipPossiblities(OpponentShip opponentShip) {
        OpponentMap opponentMap = gameState.OpponentMap;
        int mapDimension = gameState.MapDimension;
        int[][] shipPossibility = new int[mapDimension][mapDimension];

        if (opponentShip.Destroyed) {
            return shipPossibility;
        }

        if (type == ProbabilityMapType.TARGET) {
            if (botState != null) {
                List<OpponentCell> lastTargetHits = botState.LastTrackerHits.stream().map(loc -> gameState.OpponentMap.getCellAt(loc.x, loc.y)).collect(Collectors.toList());
                if (lastTargetHits != null && lastTargetHits.size() > 0) {
                    for (OpponentCell cell : lastTargetHits) {
                        updateShipPossiblities(opponentShip, shipPossibility, cell.X, cell.Y);
                    }
                }
            }
        } else {
            for (int i = 0; i < mapDimension; i++) {
                for (int j = 0; j < mapDimension; j++) {
                    OpponentCell cell = opponentMap.getCellAt(j, i);
                    if (cell.Missed || cell.Damaged) {
                        shipPossibility[j][i] = 0;
                    } else {
                        updateShipPossiblities(opponentShip, shipPossibility, j, i);
                    }
                }
            }
        }
        return shipPossibility;
    }

    private int updateShipPossiblities(OpponentShip opponentShip, int[][] shipPossibility, int x, int y) {
        OpponentMap opponentMap = gameState.OpponentMap;
        ShipType shipType = opponentShip.getShipType();
        int result = 0;
        for (Direction dir : Direction.values()) {
            if (type == ProbabilityMapType.HUNT) {
                CanBeShip canBeShip = opponentMap.canBeShip(new Point(x, y), dir, shipType.getSize());
                if (canBeShip.CanBeShip) {
                    for (OpponentCell cell : canBeShip.OpponentCells) {
                        shipPossibility[cell.X][cell.Y] += 1;
                    }
                }
            } else {
                for (OpponentCell cell : opponentMap.getAllCellsInDirection(new Point(x, y), dir, shipType.getSize(), true)) {
                    shipPossibility[cell.X][cell.Y] += opponentShip.getShipSize() - Util.dist(cell.getPoint(), new Point(x, y));
                }
            }
        }
        return result;
    }
}
