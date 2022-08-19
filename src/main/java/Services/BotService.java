package Services;

import Enums.*;
import Models.CommandAction.CommandAction;
import Models.CommandAction.PlayerAction;
import Models.CommandAction.PlayerCommand;
import Models.EngineConfig.EngineConfig;
import Models.GameState.*;
/*import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;*/
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BotService {
    BotObject bot;
    GameState gameState;
    List<CommandAction> commandList = new ArrayList<>();
    HashMap<UUID, ResourceNode> nodeMap = new HashMap<>();
    HashMap<UUID, GenericNode> availableNodeMap = new HashMap<>();
    HashMap<UUID, UUID> territoryOwner = new HashMap<>();
    GenericNode nextDestinationNode;
    int[] existingBuilding = new int[20];
    int[] botActions = new int[20];
    boolean fullView = false;
    boolean shouldRecalculate = false;

    int commandCount = 2;
    Random random = new Random();

    EngineConfig engineConfig = new EngineConfig();

    Boolean shouldQuit = false;
    Boolean receivedBotState = false;

    public BotService() {

    }

    public Boolean getReceivedBotState() {
        return receivedBotState;
    }

    public Boolean getShouldQuit() {
        return shouldQuit;
    }

    public void setShouldQuit(Boolean shouldQuit) {
        this.shouldQuit = shouldQuit;
    }

    public void updateBotState(GameState gameState) {
        this.gameState = gameState;
        this.bot = gameState.getBotByID(gameState.getBotId());
        System.out.println("Received bot state for Tick " + gameState.getWorld().getCurrentTick());
        /*ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(gameState);
            FileOutputStream fos = new FileOutputStream(String.format("dumps/bot_state_%03d.txt", gameState.getWorld().getCurrentTick()));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(json);
            bw.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        if (gameState.getWorld().getCurrentTick() > 1) receivedBotState = true;
    }

    private double getBaseDistance(Position pos) {
        return getDistance(pos, bot.getBaseLocation());
    }

    private double getDistance(Position pos1, Position pos2) {
        return Math.sqrt(Math.pow(pos1.x-pos2.x, 2) + Math.pow(pos1.y-pos2.y, 2));
    }

    public void setEngineConfig(EngineConfig engineConfig) {
        this.engineConfig = engineConfig;
    }

    private boolean scoutTowerVisited(UUID towerId) {
        return bot.getMap().getScoutTowers().contains(towerId) || bot.getActions().stream().anyMatch(action -> action.getTargetNodeId().equals(towerId));
    }

    private GameObject findNearestScoutTower() {
        return gameState.getWorld().getMap().scoutTowers.stream()
                .filter(scoutTower -> !scoutTowerVisited(scoutTower.getId()))
                .min(Comparator.comparingDouble(a -> getBaseDistance(a.getPosition())))
                .orElse(null);
    }

    public boolean scout() {
        GameObject nearestScoutTower = findNearestScoutTower();
        if (nearestScoutTower == null) return true;
        commandList.add(new CommandAction(ActionTypes.SCOUT.value, 1, nearestScoutTower.getId()));
        System.out.printf("Scouting %s%n", nearestScoutTower.getId());
        botActions[0]--;
        botActions[ActionTypes.SCOUT.value]++;
        return false;
    }

    private double getScorePerTick(ResourceNode node, boolean useTerritory) {
        if (!useTerritory) return (double)node.getReward()/(node.getWorkTime()+getBaseDistance(node.getPosition()));
        UUID owner = territoryOwner.get(node.getId());
        int reward = (int)Math.round(owner == null ? node.getReward() : (owner.equals(bot.getId()) ? node.getReward()+bot.getStatusMultiplier().get(ResourceType.valueOf(node.getType())) : node.getReward()*0.7));
        return (double)reward/(node.getWorkTime()+getBaseDistance(node.getPosition()));
    }

    private Optional<Double> getInvResourceEfficiency(int resourceType) {
        if (resourceType == 5) return Optional.of(1/5.0);
        return gameState.getWorld().getMap().getNodes().stream()
                .filter(node -> node.getType() == resourceType)
                .map(node1 -> 1/getScorePerTick(node1, true))
                .min(Double::compare);
    }

    private int getCurrentAssignedUnits(int resourceType) {
        if (resourceType == 5) return 0;
        return bot.getActions().stream()
                .filter(action -> action.getActionType() >= 2 && action.getActionType() <= 4 && nodeMap.get(action.getTargetNodeId()).getType() == resourceType)
                .mapToInt(PlayerAction::getNumberOfUnits)
                .sum();
    }

    private int getMaxExtraUnits(int resourceType) {
        if (resourceType == 5) return Math.floorDiv(bot.getWood(), 3);
        return gameState.getWorld().getMap().getNodes().stream()
                .filter(node -> node.getType() == resourceType)
                .mapToInt(node -> node.getMaxUnits()-node.getCurrentUnits())
                .sum();
    }

    private int pickRandomWeighted(double[] weights) {
        double sum = Arrays.stream(weights).sum();
        double r = random.nextDouble() * sum;
        for (int i = 0; i < weights.length; i++) {
            r -= weights[i];
            if (r <= 0) return i;
        }
        return 0;
    }

    public void farm() {
        int pop = bot.getPopulation();
        boolean[] cap = new boolean[6];
        int[] currentQty = {0, bot.getWood(), bot.getFood(), bot.getStone(), bot.getGold(), bot.getHeat()};
        TierResourceList maxQty = gameState.getPopulationTiers().get(bot.getCurrentTierLevel()).getTierMaxResources();
        int[] tierMax = {0, maxQty.getWood(), maxQty.getFood(), maxQty.getStone(), maxQty.getGold(), 1000000};
        for (int i = 1; i <= 5; i++) if (currentQty[i] >= tierMax[i]) cap[i] = true;
        double idealFarm = getInvResourceEfficiency(ResourceType.FOOD.value).orElse(0.0)*Math.max(pop/8.0, (2.0*pop-bot.getFood())/(6+(10-gameState.getWorld().getCurrentTick()%10)));
        double idealFire = getInvResourceEfficiency(ResourceType.HEAT.value).orElse(0.0)*Math.max(bot.getHeat() > 8*pop ? 0 : pop/8.0, (2.0*pop-bot.getHeat())/(6+(10-gameState.getWorld().getCurrentTick()%10)));
        double idealWood = getInvResourceEfficiency(ResourceType.WOOD.value).orElse(0.0)*3*idealFire;
        int totalFarmers = botActions[0]+botActions[ActionTypes.FARM.value]+botActions[ActionTypes.MINE.value]+botActions[ActionTypes.LUMBER.value];
        double ksub = (0.4*getInvResourceEfficiency(ResourceType.WOOD.value).orElse(0.0)
                +0.4*getInvResourceEfficiency(ResourceType.STONE.value).orElse(0.0)
                +0.2*getInvResourceEfficiency(ResourceType.GOLD.value).orElse(0.0));
        double k = ksub == 0 ? 0 : (totalFarmers-idealFarm-idealFire-idealWood)/ksub;
        idealWood += getInvResourceEfficiency(ResourceType.WOOD.value).orElse(0.0)*0.4*k;
        double idealStone = getInvResourceEfficiency(ResourceType.STONE.value).orElse(0.0)*0.4*k;
        double idealGold = getInvResourceEfficiency(ResourceType.GOLD.value).orElse(0.0)*0.2*k;
        double[] idealUnits = {0, idealWood, idealFarm, idealStone, idealGold, idealFire};
        System.out.printf("IDEAL UNITS: %s%n", Arrays.toString(idealUnits));
        for (int i = 1; i <= 5; i++) {
            idealUnits[i] = Math.min(Math.max(0, idealUnits[i]-getCurrentAssignedUnits(i)), getMaxExtraUnits(i));
            if (currentQty[i] >= tierMax[i]) idealUnits[i] = 0;
        }
        int[] resourceAllocations = {0, 0, 0, 0, 0, 0};
        for (int i = 0; i < botActions[0]; i++) resourceAllocations[pickRandomWeighted(idealUnits)]++;
        for (int i = 1; i <= 5; i++) resourceAllocations[i] = Math.min(resourceAllocations[i], getMaxExtraUnits(i));
        for (int i = 1; i <= 4; i++) if (resourceAllocations[i] > 0) {
            int finalI = i;
            List<ResourceNode> nodes = gameState.getWorld().getMap().getNodes().stream()
                    .filter(node -> node.getType() == finalI && node.getAmount() > 0)
                    .sorted(Comparator.comparingDouble(a -> -getScorePerTick(a, true)))
                    .collect(Collectors.toList());
            for (ResourceNode node : nodes) if (resourceAllocations[i] > 0) {
                int amount = Math.min(resourceAllocations[i], node.getMaxUnits()-node.getCurrentUnits());
                if (amount <= 0) continue;
                commandList.add(new CommandAction(ResourceType.valueOf(i).actionType.value, amount, node.getId()));
                System.out.printf("Harvesting %s units at %s%n", amount, node.getId());
                resourceAllocations[i] -= amount;
                botActions[0] -= amount; botActions[ResourceType.valueOf(i).actionType.value] += amount;
            }
        }
        if (resourceAllocations[5] > 0) {
            commandList.add(new CommandAction(ActionTypes.START_CAMPFIRE.value, resourceAllocations[5], UUID.randomUUID()));
            System.out.printf("Starting campfire with %s units%n", resourceAllocations[5]);
            botActions[0] -= resourceAllocations[5];
            botActions[ActionTypes.START_CAMPFIRE.value] += resourceAllocations[5];
        }
    }

    private int[] getBuildingCost(int buildingType) {
        return Arrays.stream(BuildingType.valueOf(buildingType).cost).map(i -> (int)Math.ceil((1+0.5*existingBuilding[buildingType])*i)).toArray();
    }

    private boolean build(boolean expand) {
        if (botActions[0] < 1 || shouldRecalculate) return false;
        int[] toBuild;
        if (!expand) toBuild = new int[] {BuildingType.QUARRY.value, BuildingType.FARMERS_GUILD.value, BuildingType.LUMBER_MILL.value};
        else toBuild = new int[] {BuildingType.ROAD.value, BuildingType.OUTPOST.value};
        int k = Arrays.stream(toBuild).reduce((a, b) -> getBuildingCost(a)[0] < getBuildingCost(b)[0] ? a : b).getAsInt();
        int[] cost = getBuildingCost(k);
        if (cost[0] > bot.getWood() || cost[1] > bot.getStone() || cost[2] > bot.getGold()) return false;
        GenericNode closestNode;
        if (nextDestinationNode == null) recalculateDestinationNode();
        if (nextDestinationNode == null || !fullView) {
            closestNode = gameState.getWorld().getMap().getAvailableNodes().stream()
                    .filter(node -> territoryOwner.get(node.getId()) != null && territoryOwner.get(node.getId()).equals(bot.getId()))
                    .max(Comparator.comparingDouble(a -> getBaseDistance(a.getPosition())))
                    .orElse(null);
        } else {
            closestNode = nextExpansionNode(nextDestinationNode, BuildingType.valueOf(k));
        }
        if (closestNode == null) return false;
        //if (bot.getActions().stream().anyMatch(action -> action.getActionType() == k && action.getTargetNodeId().equals(closestNode.getId()))) return false;
        commandList.add(new CommandAction(k, 1, closestNode.getId()));
        botActions[0]--; botActions[k]++;
        System.out.printf("Building %s at %s%n", k, closestNode.getId());
        System.out.printf("close: %s %s, next: %s %s%n", closestNode.getPosition().x, closestNode.getPosition().y, nextDestinationNode.getPosition().x, nextDestinationNode.getPosition().y);
        if (getDistance(closestNode.getPosition(), nextDestinationNode.getPosition()) < 2) shouldRecalculate = true;
        bot.setWood(bot.getWood()-cost[0]);
        bot.setStone(bot.getStone()-cost[1]);
        bot.setGold(bot.getGold()-cost[2]);
        return true;
    }

    private void recalculateDestinationNode() {
        ResourceNode testNode = gameState.getWorld().getMap().getNodes().stream()
                .filter(node -> territoryOwner.get(node.getId()) == null && (node.getType() == ResourceType.STONE.value || node.getType() == ResourceType.GOLD.value))
                .max(Comparator.comparingDouble(a -> getScorePerTick(a, false)))
                .orElse(null);
        if (testNode == null) return;
        if (nextDestinationNode != null && testNode.getId().equals(nextDestinationNode.getId())) return;
        shouldRecalculate = false;
        nextDestinationNode = testNode;
    }

    private int heuristicDistance(Position node, GenericNode destination, int size) {
        return (int)Math.ceil(Math.max(Math.abs(node.x-destination.getPosition().x), Math.abs(node.y-destination.getPosition().y))/(double)size);
    }

    private List<Position> getNeighbours(Position node, int size) {
        List<Position> neighbours = new ArrayList<>();
        for (int i = -size; i <= size; i++) for (int j = -size; j <= size; j++) {
            if (i == 0 && j == 0) continue;
            if (node.x+i < 0 || node.x+i >= 40 || node.y+j < 0 || node.y+j >= 40) continue;
            neighbours.add(new Position(node.x+i, node.y+j));
        }
        return neighbours;
    }

    private GenericNode nextExpansionNode(GenericNode destination, BuildingType startBuilding) {
        boolean[][] occupied = new boolean[40][40];
        boolean[][] visited = new boolean[40][40];
        PriorityQueue<PathElement> queue = new PriorityQueue<>(Comparator.comparingDouble(a -> a.depth+heuristicDistance(a.node, destination, startBuilding.territorySquare)));
        gameState.getWorld().getMap().getNodes().forEach(node -> occupied[node.getPosition().x][node.getPosition().y] = true);
        gameState.getWorld().getMap().getAvailableNodes()
                .forEach(node -> {
                    if (territoryOwner.get(node.getId()).equals(bot.getId())) {
                        visited[node.getPosition().getX()][node.getPosition().getY()] = true;
                        queue.add(new PathElement(node, node.getPosition(), 1));
                    }
                });
        gameState.getBots().forEach(b -> b.getTerritory().stream()
                .filter(node -> !node.getOwner().equals(bot.getId()))
                .forEach(node -> occupied[node.getX()][node.getY()] = true));
        while (!queue.isEmpty()) {
            PathElement element = queue.poll();
            if (getDistance(element.node, destination.getPosition()) < 2) return element.firstNode;
            for (Position node : getNeighbours(element.node, startBuilding.territorySquare)) {
                if (visited[node.getX()][node.getY()] || occupied[node.getX()][node.getY()]) continue;
                visited[node.getX()][node.getY()] = true;
                queue.add(new PathElement(element.firstNode, node, element.depth + 1));
            }
        }
        return null;
    }

    private boolean expensiveMultiplierBuildings() {
        int[] toBuild = {BuildingType.QUARRY.value, BuildingType.FARMERS_GUILD.value, BuildingType.LUMBER_MILL.value};
        TierResourceList maxQty = gameState.getPopulationTiers().get(bot.getCurrentTierLevel()).getTierMaxResources();
        for (int k : toBuild) {
                if (getBuildingCost(k)[0] > maxQty.getWood() || getBuildingCost(k)[1] > maxQty.getStone() || getBuildingCost(k)[2] > maxQty.getGold())
                    continue;
                return true;
        }
        return false;
    }

    private int calculateCurrentScore() {
        int territoryCount = (int)bot.getTerritory().stream()
                .filter(node -> node.getOwner().equals(bot.getId())).count();
        int buildingFactor = bot.getBuildings().stream().mapToInt(i -> BuildingType.valueOf(i.getType()).scoreMultiplier).sum();
        return bot.getPopulation() * 25 +
                bot.getWood() + bot.getFood() +
                bot.getStone()*2 + bot.getGold()*2 +
                buildingFactor +
                2* territoryCount;
    }

    private int getBuilders() {
        return botActions[BuildingType.FARMERS_GUILD.value] + botActions[BuildingType.LUMBER_MILL.value] + botActions[BuildingType.QUARRY.value] + botActions[BuildingType.ROAD.value] + botActions[BuildingType.OUTPOST.value];
    }

    public PlayerCommand computeNextPlayerAction() {
        long time = System.currentTimeMillis();
        commandList.clear();
        Arrays.fill(botActions, 0);
        botActions[0] = bot.getAvailableUnits();
        bot.getActions().forEach(action -> botActions[action.getActionType()]+=action.getNumberOfUnits());
        nodeMap.clear();
        gameState.getWorld().getMap().getNodes().forEach(node -> nodeMap.put(node.getId(), node));
        availableNodeMap.clear();
        gameState.getWorld().getMap().getAvailableNodes().forEach(node -> availableNodeMap.put(node.getId(), node));
        territoryOwner.clear();
        bot.getTerritory().forEach(territory -> territoryOwner.put(territory.getNodeOnLand(), territory.getOwner()));
        existingBuilding = new int[20];
        bot.getBuildings().forEach(node -> existingBuilding[node.getType()]++);
        if (shouldRecalculate && gameState.getWorld().getCurrentTick()%10==1) recalculateDestinationNode();

        if (bot.getPopulation() >= 5 && getBuilders() < 1) build(false);
        if (fullView && expensiveMultiplierBuildings() && getBuilders() < 1) build(true);
        if (botActions[0] >= 1 && botActions[ActionTypes.SCOUT.value] < 1 && !fullView) fullView = scout();
        farm();
        if (botActions[0] > 5 && botActions[ActionTypes.SCOUT.value] < 2 && !fullView) fullView = scout();

        receivedBotState = false;
        System.out.printf("BOT ACTIONS %s%n", Arrays.toString(botActions));
        System.out.printf("CURRENT SCORE: %8d, UNITS LEFT: %6d, TIME ELAPSED %4d%n%n", calculateCurrentScore(), botActions[0], System.currentTimeMillis()-time);
        commandCount++;
        return new PlayerCommand(bot.getId(), commandList);
    }

    private static class PathElement {
        public GenericNode firstNode;
        public Position node;
        public int depth;

        public PathElement(GenericNode firstNode, Position node, int depth) {
            this.firstNode = firstNode;
            this.node = node;
            this.depth = depth;
        }
    }
}
