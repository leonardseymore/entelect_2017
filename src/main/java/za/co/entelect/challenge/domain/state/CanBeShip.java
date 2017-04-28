package za.co.entelect.challenge.domain.state;


import java.util.ArrayList;

public class CanBeShip {

    public ArrayList<OpponentCell> OpponentCells;
    public boolean CanBeShip;

    public CanBeShip() {
    }

    public CanBeShip(ArrayList<OpponentCell> opponentCells, boolean canBeShip) {
        OpponentCells = opponentCells;
        CanBeShip = canBeShip;
    }
}
