package za.co.entelect.challenge.strategy.shoot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.co.entelect.challenge.BotState;
import za.co.entelect.challenge.ProbabilityMap;
import za.co.entelect.challenge.ProbabilityMapType;
import za.co.entelect.challenge.domain.command.Command;
import za.co.entelect.challenge.domain.command.code.Code;
import za.co.entelect.challenge.domain.state.GameState;
import za.co.entelect.challenge.domain.state.OpponentCell;
import za.co.entelect.challenge.domain.state.OpponentMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HuntShootStrategy {

    private static Logger logger = LoggerFactory.getLogger(HuntShootStrategy.class);

    public Command executeStrategy(GameState gameState, BotState botState, boolean highest) {
        return huntShotCommand(gameState, botState, highest ? -1 : 1);
    }

    private Command huntShotCommand(GameState gameState, BotState botState, int order) {
        ProbabilityMap huntMap = new ProbabilityMap(gameState, ProbabilityMapType.HUNT, null);
        int MapDimension = gameState.MapDimension;
        OpponentMap opponentMap = gameState.OpponentMap;
        List<ShootCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < MapDimension; i++) {
            for (int j = 0; j < MapDimension; j++) {
                if ((i + j) % 2 == 0) {
                    continue;
                }
                OpponentCell cell = opponentMap.getCellAt(j, i);
                if (cell.Damaged || cell.Missed) {
                    continue;
                }
                float probability = huntMap.getProbability(j, i);
                if (probability > 0) {
                    candidates.add(new ShootCandidate(cell, probability));
                }
            }
        }

        Optional<ShootCandidate> bestShot = candidates.stream()
                .sorted((a, b) -> order * Float.compare(a.probability, b.probability))
                .findFirst();

        if (!bestShot.isPresent()) {
            logger.error("Could not find hunting shot!!!");
            return new Command(Code.FIRESHOT, 0, 0);
        }

        ShootCandidate bestCandidate = bestShot.get();
        OpponentCell bestTarget = bestCandidate.opponentCell;
        return new Command(Code.FIRESHOT, bestTarget.X, bestTarget.Y);
    }

    private static class ShootCandidate {
        public OpponentCell opponentCell;
        public float probability;

        public ShootCandidate(OpponentCell opponentCell, float probability) {
            this.opponentCell = opponentCell;
            this.probability = probability;
        }
    }
}
