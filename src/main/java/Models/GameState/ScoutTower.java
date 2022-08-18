package Models.GameState;

import java.util.List;
import java.util.UUID;

public class ScoutTower extends GameObject {
    List<UUID> nodes;
    //List<BotTerritory> territory;
    int xRegion;
    int yRegion;

    public ScoutTower(UUID id, int gameObjectType, Position position, List<UUID> nodes, int xRegion, int yRegion) {
        super(id, gameObjectType, position);
        this.nodes = nodes;
        this.xRegion = xRegion;
        this.yRegion = yRegion;
    }

    public List<UUID> getNodes() {
        return nodes;
    }

    public void setNodes(List<UUID> nodes) {
        this.nodes = nodes;
    }

    public int getxRegion() {
        return xRegion;
    }

    public void setxRegion(int xRegion) {
        this.xRegion = xRegion;
    }

    public int getyRegion() {
        return yRegion;
    }

    public void setyRegion(int yRegion) {
        this.yRegion = yRegion;
    }
}
