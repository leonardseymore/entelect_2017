package za.co.entelect.challenge.domain.state;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class GameState implements Serializable {

    public PlayerMap PlayerMap;

    public OpponentMap OpponentMap;

    public String GameVersion;

    public int GameLevel;

    public int Round;

    public int MapDimension;

    public int Phase;

    public PlayerMap Player1Map;

    public PlayerMap Player2Map;

    public GameState deepCopy(){
        return SerializationUtils.clone(this);
    }
}
