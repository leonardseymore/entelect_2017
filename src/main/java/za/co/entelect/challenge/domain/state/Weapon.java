package za.co.entelect.challenge.domain.state;

import java.io.Serializable;

public class Weapon  implements Serializable {

    public WeaponType WeaponType;

    public enum WeaponType {
        SingleShot
    }
}
