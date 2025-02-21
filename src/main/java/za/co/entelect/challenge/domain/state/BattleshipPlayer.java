package za.co.entelect.challenge.domain.state;

import java.io.Serializable;
import java.util.ArrayList;

public class BattleshipPlayer implements Serializable {

    public int FailedFirstRoundCommands;

    public String Name;

    public ArrayList<Ship> Ships;

    public int Points;

    public int Energy;

    public boolean Killed;

    public boolean IsWinner;

    public int ShotsFired;

    public int ShotsHit;

    public int ShipsRemaining;

    public char Key;
}
