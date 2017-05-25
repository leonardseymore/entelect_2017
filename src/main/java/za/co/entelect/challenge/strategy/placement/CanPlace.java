package za.co.entelect.challenge.strategy.placement;

import za.co.entelect.challenge.domain.state.Cell;

import java.util.ArrayList;
import java.util.List;

public class CanPlace {
    public boolean canPlace;
    public List<Cell> cells;

    public CanPlace() {
    }

    public CanPlace(boolean canPlace, List<Cell> cells) {
        this.canPlace = canPlace;
        this.cells = cells;
    }
}
