package za.co.entelect.challenge;

import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.command.ship.ShipType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BotState {
    public BotMode Mode = BotMode.HUNT;
    public List<Point> LastTrackerHits = new ArrayList<>();
    public Point LastShot;
    public List<Point> LastMisses = new ArrayList<>();
    public Map<ShipType, Boolean> LastOpponentShipStatus = new HashMap<>();
    public ShipType LastDestroyedShip;

    @Override
    public String toString() {
        return "BotState{" +
                "Mode=" + Mode +
                ", LastTrackerHits=" + LastTrackerHits +
                ", LastShot=" + LastShot +
                ", LastMisses=" + LastMisses +
                ", LastOpponentShipStatus=" + LastOpponentShipStatus +
                ", LastDestroyedShip=" + LastDestroyedShip +
                '}';
    }
}
