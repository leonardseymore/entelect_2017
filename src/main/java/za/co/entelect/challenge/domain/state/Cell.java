package za.co.entelect.challenge.domain.state;


import za.co.entelect.challenge.domain.command.Point;

import java.io.Serializable;

public class Cell implements Serializable {

    public boolean Occupied;

    public boolean Hit;

    public int X;

    public int Y;

    public Point getPoint() {

        return new Point(X, Y);
    }
}
