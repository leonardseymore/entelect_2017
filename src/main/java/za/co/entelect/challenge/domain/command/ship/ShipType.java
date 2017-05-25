package za.co.entelect.challenge.domain.command.ship;

public enum ShipType {
    Battleship(4, "B"),
    Carrier(5, "C"),
    Cruiser(3, "c"),
    Destroyer(2, "D"),
    Submarine(3, "S");

    private int size;
    private String shortVal;

    ShipType(int size, String shortVal) {
        this.size = size;this.shortVal = shortVal;
    }

    public int getSize() {
        return size;
    }

    public String getShortVal() {
        return shortVal;
    }
}
