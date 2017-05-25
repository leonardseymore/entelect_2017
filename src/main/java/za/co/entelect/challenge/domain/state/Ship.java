package za.co.entelect.challenge.domain.state;

import java.io.Serializable;
import java.util.ArrayList;

public class Ship  implements Serializable {

    public boolean Destroyed;

    public boolean Placed;

    public za.co.entelect.challenge.domain.command.ship.ShipType ShipType;

    public ArrayList<Weapon> Weapons;

    public ArrayList<Cell> Cells;
}
