package za.co.entelect.challenge.domain.state;

import za.co.entelect.challenge.domain.command.ship.ShipType;

import java.io.Serializable;

public class OpponentShip implements Serializable {

    public boolean Destroyed;

    public za.co.entelect.challenge.domain.command.ship.ShipType ShipType;
}
