package za.co.entelect.challenge.domain.state;

import za.co.entelect.challenge.domain.command.ship.ShipType;

public class OpponentShip {

    public boolean Destroyed;

    public int ShipType;

    public ShipType getShipType(){
        for (ShipType t : za.co.entelect.challenge.domain.command.ship.ShipType.values()) {
            if (t.ordinal() == ShipType) {
                return t;
            }
        }
        return null;
    }

    public int getShipSize() {
        return getShipType().getSize();
    }
}
