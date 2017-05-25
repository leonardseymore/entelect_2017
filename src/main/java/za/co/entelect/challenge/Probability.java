package za.co.entelect.challenge;


import za.co.entelect.challenge.domain.command.Point;

public class Probability {

    public Point point;
    public float value;

    public Probability(Point point, float value) {
        this.point = point;
        this.value = value;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
