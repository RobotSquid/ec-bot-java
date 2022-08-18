package Models.GameState;

import java.util.List;
import java.util.UUID;

import Models.CommandAction.PlayerAction;

public class BotObject {
    UUID id;
    int currentTierLevel;
    int tick;
    BotMapState map;
    int population;
    Position baseLocation;
    List<PlayerAction> pendingActions;
    List<PlayerAction> actions;
    List<BuildingNode> buildings;
    List<BotTerritory> territory;
    StatusMultiplier statusMultiplier;
    int availableUnits;
    int seed;
    int wood;
    int food;
    int stone;
    int heat;
    int gold;

    public BotObject(UUID id, int currentTierLevel, int tick, BotMapState map, int population, Position baseLocation, List<PlayerAction> pendingActions, List<PlayerAction> actions, List<BuildingNode> buildings, List<BotTerritory> territory, StatusMultiplier statusMultiplier, int availableUnits, int seed, int wood, int food, int stone, int heat, int gold) {
        this.id = id;
        this.currentTierLevel = currentTierLevel;
        this.tick = tick;
        this.map = map;
        this.population = population;
        this.baseLocation = baseLocation;
        this.pendingActions = pendingActions;
        this.actions = actions;
        this.buildings = buildings;
        this.territory = territory;
        this.statusMultiplier = statusMultiplier;
        this.availableUnits = availableUnits;
        this.seed = seed;
        this.wood = wood;
        this.food = food;
        this.stone = stone;
        this.heat = heat;
        this.gold = gold;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getCurrentTierLevel() {
        return currentTierLevel;
    }

    public void setCurrentTierLevel(int currentTierLevel) {
        this.currentTierLevel = currentTierLevel;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public BotMapState getMap() {
        return map;
    }

    public void setMap(BotMapState map) {
        this.map = map;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public Position getBaseLocation() {
        return baseLocation;
    }

    public void setBaseLocation(Position baseLocation) {
        this.baseLocation = baseLocation;
    }

    public List<PlayerAction> getPendingActions() {
        return pendingActions;
    }

    public void setPendingActions(List<PlayerAction> pendingActions) {
        this.pendingActions = pendingActions;
    }

    public List<PlayerAction> getActions() {
        return actions;
    }

    public void setActions(List<PlayerAction> actions) {
        this.actions = actions;
    }

    public List<BuildingNode> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<BuildingNode> buildings) {
        this.buildings = buildings;
    }

    public List<BotTerritory> getTerritory() {
        return territory;
    }

    public void setTerritory(List<BotTerritory> territory) {
        this.territory = territory;
    }

    public StatusMultiplier getStatusMultiplier() {
        return statusMultiplier;
    }

    public void setStatusMultiplier(StatusMultiplier statusMultiplier) {
        this.statusMultiplier = statusMultiplier;
    }

    public int getAvailableUnits() {
        return availableUnits;
    }

    public void setAvailableUnits(int availableUnits) {
        this.availableUnits = availableUnits;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getWood() {
        return wood;
    }

    public void setWood(int wood) {
        this.wood = wood;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getStone() {
        return stone;
    }

    public void setStone(int stone) {
        this.stone = stone;
    }

    public int getHeat() {
        return heat;
    }

    public void setHeat(int heat) {
        this.heat = heat;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}
