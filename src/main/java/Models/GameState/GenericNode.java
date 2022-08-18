package Models.GameState;

import java.util.UUID;

public class GenericNode extends GameObject {
    int type;
    int maxUnits;
    int currentUnits;

    public GenericNode(UUID id, int gameObjectType, Position position, int type, int maxUnits, int currentUnits) {
        super(id, gameObjectType, position);
        this.type = type;
        this.maxUnits = maxUnits;
        this.currentUnits = currentUnits;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMaxUnits() {
        return maxUnits;
    }

    public void setMaxUnits(int maxUnits) {
        this.maxUnits = maxUnits;
    }

    public int getCurrentUnits() {
        return currentUnits;
    }

    public void setCurrentUnits(int currentUnits) {
        this.currentUnits = currentUnits;
    }
}
