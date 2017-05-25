Building
--------
Standard Maven 3 Java project
mvn clean package

Strategy
--------
Switch between two high level shooting modes. These modes use a probability map to determine the probability of a ship being at a specific location. 
- Mode 1 - Hunting Mode
We don't have a target yet
Fires at the most likely position where a ship could be

- Mode 2 - Targetting Mode
We have a hit, but haven't sunk the ship yet
Keep firing at most likely location where ship is at
If ship is sunk, remove most likely locations from tracked hits
If we still have hits in the queue, they must be from other ships so try targetting those
Otherwise switch back to hunting mode

To optimize the hunting mode only every second cell is targetted. (Referred to as parity as it is not necessary to shoot at every cell to find a target)