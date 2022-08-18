package Models.GameState;

import java.util.UUID;

public class BuildingNode extends GameObject {
    int territorySquare = 0;
    int type = 0;
    int scoreMultiplier = 0;

    public BuildingNode(UUID id, int gameObjectType, Position position, int territorySquare, int type, int scoreMultiplier) {
        super(id, gameObjectType, position);
        this.territorySquare = territorySquare;
        this.type = type;
        this.scoreMultiplier = scoreMultiplier;
    }

    public int getTerritorySquare() {
        return territorySquare;
    }

    public void setTerritorySquare(int territorySquare) {
        this.territorySquare = territorySquare;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getScoreMultiplier() {
        return scoreMultiplier;
    }

    public void setScoreMultiplier(int scoreMultiplier) {
        this.scoreMultiplier = scoreMultiplier;
    }
}
