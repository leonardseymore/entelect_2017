package za.co.entelect.challenge;

import za.co.entelect.challenge.domain.command.Command;
import za.co.entelect.challenge.domain.command.PlaceShipCommand;
import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.state.GameState;
import za.co.entelect.challenge.domain.state.OpponentCell;
import za.co.entelect.challenge.domain.state.OpponentShip;
import za.co.entelect.challenge.strategy.HuntShootStrategy;
import za.co.entelect.challenge.strategy.RandomPlacementStrategy;
import za.co.entelect.challenge.strategy.TargetShootStrategy;

import java.util.ArrayList;
import java.util.List;

public class BotStrategy {

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
        return new RandomPlacementStrategy().getShipPlacement(gameState);
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

        for (OpponentShip ship : gameState.OpponentMap.Ships) {
            boolean destroyed = botState.LastOpponentShipStatus.get(ship.getShipType());
            if (ship.Destroyed && !destroyed) {
                int size = ship.getShipSize();
                List<Point> lastHits = botState.LastTrackerHits;
                lastHits.subList(lastHits.size() - size, lastHits.size()).clear();

                if (lastHits.size() == 0) {
                    botState.Mode = BotMode.HUNT;
                }
            }
            botState.LastOpponentShipStatus.put(ship.getShipType(), ship.Destroyed);
        }
    }
}
