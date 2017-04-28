package za.co.entelect.challenge.domain.command.ship;

public enum ShipType {
    Battleship(4),
    Carrier(5),
    Cruiser(3),
    Destroyer(2),
    Submarine(3);

    private int size;

    ShipType(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
