package Models.GameState;

import java.util.ArrayList;
import java.util.List;

public class Map {
    public List<ScoutTower> scoutTowers;
    public List<ResourceNode> nodes;
    public List<GenericNode> availableNodes;

    public Map(List<ScoutTower> scoutTowers, List<ResourceNode> resourceNodes, List<GenericNode> availableNodes) {
        this.scoutTowers = scoutTowers;
        nodes = resourceNodes;
        this.availableNodes = availableNodes;
    }

    public Map(){
        scoutTowers = new ArrayList<ScoutTower>();
        nodes = new ArrayList<ResourceNode>();
        availableNodes = new ArrayList<GenericNode>();
    }

    public List<ScoutTower> getScoutTowers() {
        return scoutTowers;
    }

    public void setScoutTowers(List<ScoutTower> scoutTowers) {
        this.scoutTowers = scoutTowers;
    }

    public List<ResourceNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ResourceNode> nodes) {
        this.nodes = nodes;
    }

    public List<GenericNode> getAvailableNodes() {
        return availableNodes;
    }

    public void setAvailableNodes(List<GenericNode> availableNodes) {
        this.availableNodes = availableNodes;
    }
}
