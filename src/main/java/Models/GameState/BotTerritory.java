package Models.GameState;

import java.util.List;
import java.util.UUID;

public class BotTerritory {
    UUID owner;
    UUID nodeOnLand;
    List<TerritoryOccupant> occupants;
    int x;
    int y;

    public BotTerritory(UUID owner, UUID nodeOnLand, List<TerritoryOccupant> occupants, int x, int y) {
        this.owner = owner;
        this.nodeOnLand = nodeOnLand;
        this.occupants = occupants;
        this.x = x;
        this.y = y;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getNodeOnLand() {
        return nodeOnLand;
    }

    public void setNodeOnLand(UUID nodeOnLand) {
        this.nodeOnLand = nodeOnLand;
    }

    public List<TerritoryOccupant> getOccupants() {
        return occupants;
    }

    public void setOccupants(List<TerritoryOccupant> occupants) {
        this.occupants = occupants;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}