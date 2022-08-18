package Models.GameState;

import java.util.List;
import java.util.UUID;

public class BotMapState {
    List<UUID> scoutTowers;
    List<UUID> nodes;
    List<UUID> availableNodes;

    public BotMapState(List<UUID> scoutTower, List<UUID> nodes, List<UUID> availableNodes) {
        this.scoutTowers = scoutTower;
        this.nodes = nodes;
        this.availableNodes = availableNodes;
    }

    public List<UUID> getScoutTowers() {
        return scoutTowers;
    }

    public void setScoutTowers(List<UUID> scoutTowers) {
        this.scoutTowers = scoutTowers;
    }

    public List<UUID> getNodes() {
        return nodes;
    }

    public void setNodes(List<UUID> nodes) {
        this.nodes = nodes;
    }

    public List<UUID> getAvailableNodes() {
        return availableNodes;
    }

    public void setAvailableNodes(List<UUID> availableNodes) {
        this.availableNodes = availableNodes;
    }
}
