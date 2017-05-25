package za.co.entelect.challenge.strategy.placement;

import za.co.entelect.challenge.domain.command.Point;
import za.co.entelect.challenge.domain.command.direction.Direction;
import za.co.entelect.challenge.domain.command.ship.ShipType;

public class Placement {
    public ShipType shipType;
    public Point start;
    public Direction direction;

    public Placement(ShipType shipType, Point start, Direction direction) {
        this.shipType = shipType;
        this.start = start;
        this.direction = direction;
    }
}
