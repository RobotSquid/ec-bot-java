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

public class BotService {
    BotObject bot;
    GameState gameState;
    List<CommandAction> commandList = new ArrayList<>();
    HashMap<UUID, ResourceNode> nodeMap = new HashMap<>();
    HashMap<UUID, GenericNode> availableNodeMap = new HashMap<>();
    HashMap<UUID, UUID> territoryOwner = new HashMap<>();
    GenericNode nextDestinationNode;
    Set<ResourceNode> capturedNodes = new HashSet<>();
    int[] existingBuilding = new int[20];
    int[] botActions = new int[20];
    boolean fullView = false;
    ResourceNode[][] resourceMap = new ResourceNode[40][40];
    int[][] distanceMap = new int[40][40];
    int[] prevResources = new int[5];
    double[] prevScorePerTick = new double[5];
    double[] prevAvgPerTick = new double[5];
    HashMap<UUID, List<PlayerAction>> nodeActions = new HashMap<>();
    HashMap<UUID, List<PlayerAction>> myActions = new HashMap<>();
    Set<BotTerritory> borderNodes = new HashSet<>();
    UUID[][] territoryMap = new UUID[40][40];
    HashMap<UUID, Integer> unitsToCaptureMemo = new HashMap<>();
    int BAD_UNITS = 0;
    int[] resourceAllocations = {0, 0, 0, 0, 0, 0};
    double[] idealResources = {0, 0, 0, 0, 0, 0};
    int[] currentQty;
    int[][] freeNodes = new int[40][40];
    double averageK = 0;

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

    private double getResourceEfficiency(int resourceType, boolean inv) {
        if (resourceType == 5) return bot.getWood() > 0 ? (inv ? 1/5.0 : 0) :0;
        Optional<Double> factor1 = gameState.getWorld().getMap().getNodes().stream()
                .filter(node -> node.getType() == resourceType)
                .map(node1 -> getScorePerTick(node1, true))
                .max(Double::compare);
        if (factor1.isEmpty()) return 0.0;
        if (gameState.getWorld().getCurrentTick() < 100) return inv ? 1/factor1.get() : factor1.get();
        if (inv) return 1/(0.2*factor1.get() + 0.8* prevAvgPerTick[resourceType]);
        else return 0.2*factor1.get() + 0.8* prevAvgPerTick[resourceType];
    }

    private int getCurrentAssignedUnits(int resourceType) {
        if (resourceType == 5) return 0;
        return bot.getActions().stream()
                .filter(action -> action.getActionType() >= 2 && action.getActionType() <= 4 && nodeMap.get(action.getTargetNodeId()).getType() == resourceType)
                .mapToInt(PlayerAction::getNumberOfUnits)
                .sum();
    }

    private int getUnitsLeaving(ResourceNode node) {
        int time = node.getWorkTime()+gameState.getWorld().getCurrentTick()+(int)getBaseDistance(node.getPosition());
        return nodeActions.getOrDefault(node.getId(), new ArrayList<>()).stream()
                .filter(action -> action.getActionType() >= 2 && action.getActionType() <= 4
                        && time > action.getTickActionCompleted())
                .mapToInt(PlayerAction::getNumberOfUnits)
                .sum();
    }

    private int getTierMax(int resourceType) {
        TierResourceList maxQty = gameState.getPopulationTiers().get(bot.getCurrentTierLevel()).getTierMaxResources();
        int[] tierMax = {0, maxQty.getWood(), maxQty.getFood(), maxQty.getStone(), maxQty.getGold(), 80*bot.getPopulation()};
        return tierMax[resourceType];
    }

    private double getIdealMinRes(int resourceType) {
        PopulationTier thisTier = gameState.getPopulationTiers().get(bot.getCurrentTierLevel());
        int last_pop = bot.getCurrentTierLevel() > 0 ? gameState.getPopulationTiers().get(bot.getCurrentTierLevel()-1).getMaxPopulation() : 0;
        TierResourceList trc = thisTier.getTierResourceConstraints();
        TierResourceList trc2 = gameState.getPopulationTiers().get(bot.getCurrentTierLevel()+1).getTierResourceConstraints();
        int[] thisRes = {0, trc.getWood(), trc.getFood(), trc.getStone(), trc.getGold(), 0};
        int[] nextRes = {0, trc2.getWood(), trc2.getFood(), trc2.getStone(), trc2.getGold(), 0};
        return 2*(thisRes[resourceType] + (nextRes[resourceType]-thisRes[resourceType])*((double)bot.getPopulation()-last_pop)/(thisTier.getMaxPopulation()-last_pop));
    }

    private int getMaxExtraUnits(int resourceType) {
        if (resourceType == 5)
            return bot.getPopulation()*(2500-gameState.getWorld().getCurrentTick())/10 < bot.getHeat() ? 0 : Math.floorDiv(bot.getWood(), 3);
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
        return weights.length-1;
    }

    private void updateAllocations(int nUnits) {
        System.out.printf("IDEAL :   WOOD %6.2f, FARM %6.2f, STONE %6.2f, GOLD %6.2f, FIRE %6.2f\n", idealResources[1], idealResources[2], idealResources[3], idealResources[4], idealResources[5]);
        for (int k = 0; k < 3; k++) {
            for (int i = 1; i <= 5; i++) idealResources[i] = Math.min(idealResources[i], Math.max(0, Math.min(getMaxExtraUnits(i), getTierMax(i)-currentQty[i]-getCurrentAssignedUnits(i))-resourceAllocations[i]));
            int toAssign = Math.min(botActions[0]-Arrays.stream(resourceAllocations).sum(), nUnits);
            if (toAssign > 1000) for (int i = 0; i < (int)(0.95*toAssign/20); i++) resourceAllocations[pickRandomWeighted(idealResources)]+=20;
            for (int i = 0; i < toAssign-((toAssign > 1000) ? ((int)(0.95*toAssign/20))*20 : 0); i++) resourceAllocations[pickRandomWeighted(idealResources)]++;
        }
    }

    public void farm() {
        int pop = bot.getPopulation();
        resourceAllocations = new int[] {0, 0, 0, 0, 0, 0};

        idealResources = new double[] {0, 0, 0, 0, 0, 0};
        idealResources[ResourceType.FOOD.value] = getResourceEfficiency(ResourceType.FOOD.value, true)*(0.6*pop/((double)bot.getFood()/pop+1.0));
        idealResources[ResourceType.HEAT.value] = getResourceEfficiency(ResourceType.HEAT.value, true)*(0.6*pop/((double)bot.getHeat()/pop+1.0));
        idealResources[ResourceType.WOOD.value] = getResourceEfficiency(ResourceType.WOOD.value, true)*(0.6667*0.6*pop/((double)bot.getHeat()/pop+1.0));
        System.out.printf("INV_EFF FOOD: %6.2f, WOOD: %6.2f%n", getResourceEfficiency(ResourceType.FOOD.value, true), getResourceEfficiency(ResourceType.WOOD.value, true));
        updateAllocations((int)Math.ceil(Arrays.stream(idealResources).sum()));

        if (bot.getCurrentTierLevel() >= 6) {
            idealResources = new double[] {0, 0, 0, 0, 0, 0};
            double k = 0;
            for (int i = 1; i <= 4; i++) k += getResourceEfficiency(i, false);
            k = k > 0 ? (botActions[0]-Arrays.stream(resourceAllocations).sum())/k : 0;
            for (int i = 1; i <= 4; i++) idealResources[i] = getResourceEfficiency(i, false)*k;
            updateAllocations(botActions[0]);
        }

        double[] unitWeights = {0, 0, 0, 0, 0, 0};
        idealResources = new double[] {0, 0, 0, 0, 0, 0};
        double k = 0;
        for (int i = 1; i <= 4; i++) {
            unitWeights[i] = Math.max(0, getIdealMinRes(i)-currentQty[i]);
            k += unitWeights[i]*getResourceEfficiency(i, true);
        }
        k = k > 0 ? (botActions[0]-Arrays.stream(resourceAllocations).sum())/k : 0;
        for (int i = 1; i <= 4; i++) idealResources[i] = getResourceEfficiency(i, true)*unitWeights[i]*k;
        updateAllocations((int)(0.2*botActions[0]));

        int totalWood = gameState.getWorld().getMap().getNodes().stream().filter(n -> n.getType() == ResourceType.WOOD.value).mapToInt(ResourceNode::getAmount).sum();
        double woodFactor = Math.max(0, gameState.getWorld().getCurrentTick() > 200 ? 1-totalWood/5000000.0 : 0)*20;
        unitWeights = new double[] {0, 1+.66667*woodFactor, 1, 1.4, 1, woodFactor};
        idealResources = new double[] {0, 0, 0, 0, 0, 0};
        k = 0;
        for (int i = 1; i <= 5; i++) k += unitWeights[i]*getResourceEfficiency(i, true);
        k = k > 0 ? (botActions[0]-Arrays.stream(resourceAllocations).sum())/k : 0;
        for (int i = 1; i <= 5; i++) idealResources[i] = getResourceEfficiency(i, true)*unitWeights[i]*k;
        updateAllocations((int)(0.8*botActions[0]));

        System.out.printf("ACTUAL:   WOOD %6d, FARM %6d, STONE %6d, GOLD %6d, FIRE %6d\n", resourceAllocations[1], resourceAllocations[2], resourceAllocations[3], resourceAllocations[4], resourceAllocations[5]);
        for (int i = 1; i <= 4; i++) if (resourceAllocations[i] > 0) {
            int finalI = i;
            List<ResourceNode> nodes = gameState.getWorld().getMap().getNodes().stream()
                    .filter(node -> node.getType() == finalI && node.getAmount() > 0)
                    .sorted(Comparator.comparingDouble(a -> -getScorePerTick(a, true)))
                    .collect(Collectors.toList());
            prevScorePerTick[i] = 0;
            int origResAlloc = resourceAllocations[i];
            for (ResourceNode node : nodes) if (resourceAllocations[i] > 0) {
                int amount = Math.min(resourceAllocations[i], node.getMaxUnits()-node.getCurrentUnits());
                if (amount <= 0) continue;
                RegenerationRate regen = node.getRegenerationRate() == null ? new RegenerationRate(100, 0) : node.getRegenerationRate();
                int time = (int)Math.ceil(node.getWorkTime()+getBaseDistance(node.getPosition()));
                int resourcesAtTime = node.getAmount()-getUnitsLeaving(node);
                if (resourcesAtTime < 0) continue;
                if (time >= regen.getTicks()-node.getCurrentRegenTick()) resourcesAtTime += regen.getAmount() + regen.getAmount()*((time-regen.getTicks()+node.getCurrentRegenTick())/regen.getTicks());
                amount = Math.min(amount, resourcesAtTime);
                commandList.add(new CommandAction(ResourceType.valueOf(i).actionType.value, amount, node.getId()));
                //System.out.printf("Harvesting %s units at %s%n", amount, node.getId());
                resourceAllocations[i] -= amount;
                botActions[0] -= amount; botActions[ResourceType.valueOf(i).actionType.value] += amount;
                prevScorePerTick[i] += getScorePerTick(node, true)*amount;
            }
            if (origResAlloc > resourceAllocations[i]) {
                prevScorePerTick[i] /= origResAlloc - resourceAllocations[i];
                prevAvgPerTick[i] -= prevAvgPerTick[i] / 50;
                prevAvgPerTick[i] += prevScorePerTick[i] / 50;
            }
        }
        if (resourceAllocations[5] > 0) {
            commandList.add(new CommandAction(ActionTypes.START_CAMPFIRE.value, resourceAllocations[5], UUID.randomUUID()));
            //System.out.printf("Starting campfire with %s units%n", resourceAllocations[5]);
            botActions[0] -= resourceAllocations[5];
            botActions[ActionTypes.START_CAMPFIRE.value] += resourceAllocations[5];
            bot.setWood(bot.getWood()-resourceAllocations[5]*3);
        }
    }

    private int[] getBuildingCost(int buildingType) {
        return Arrays.stream(BuildingType.valueOf(buildingType).cost).map(i -> (int)Math.ceil((1+0.5*existingBuilding[buildingType])*i)).toArray();
    }

    private void build(boolean expand) {
        if (botActions[0] < 1) return;
        int[] toBuild;
        if (!expand) toBuild = new int[] {BuildingType.QUARRY.value, BuildingType.FARMERS_GUILD.value, BuildingType.LUMBER_MILL.value};
        else toBuild = new int[] {BuildingType.ROAD.value, BuildingType.OUTPOST.value};
        int k = Arrays.stream(toBuild).reduce((a, b) -> getBuildingCost(a)[1] < getBuildingCost(b)[1] ? a : b).getAsInt();
        int[] cost = getBuildingCost(k);
        if (cost[0] > bot.getWood() || cost[1] > bot.getStone() || cost[2] > bot.getGold()) return;
        nextDestinationNode = recalculateDestinationNode();
        GenericNode closestNode = nextExpansionNode(nextDestinationNode, BuildingType.valueOf(k));
        //if (closestNode == null) closestNode = nextBlobNode();
        if (closestNode == null) return;

        commandList.add(new CommandAction(k, 1, closestNode.getId()));
        botActions[0]--; botActions[k]++;
        //System.out.printf("Building %s at %s%n", k, closestNode.getId());
        bot.setWood(bot.getWood()-cost[0]);
        bot.setStone(bot.getStone()-cost[1]);
        bot.setGold(bot.getGold()-cost[2]);
    }

    private ResourceNode recalculateDestinationNode() {
        if (!fullView) return null;
        int stone = (int) capturedNodes.stream().filter(node -> node.getType() == ResourceType.STONE.value).count();
        int gold = (int) capturedNodes.stream().filter(node -> node.getType() == ResourceType.GOLD.value).count();
        int type = stone < gold ? ResourceType.STONE.value : ResourceType.GOLD.value;
        return gameState.getWorld().getMap().getNodes().stream()
                .filter(node -> (territoryOwner.get(node.getId()) == null || territoryOwner.get(node.getId()).equals(bot.getId()))
                        && (node.getType() == type)
                        && !capturedNodes.contains(node))
                .max(Comparator.comparingDouble(a -> getScorePerTick(a, false)/(distanceMap[a.getPosition().x][a.getPosition().y]+15)))
                .orElse(null);
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

    private GenericNode nextBlobNode() {
        return gameState.getWorld().getMap().getAvailableNodes().stream()
                .filter(node -> territoryOwner.get(node.getId()) != null && territoryOwner.get(node.getId()).equals(bot.getId()))
                .max(Comparator.comparingDouble(node -> (double)(freeNodes[node.getPosition().x][node.getPosition().y]+1)/getBaseDistance(node.getPosition())))
                .orElse(null);
    }

    private GenericNode nextExpansionNode(GenericNode destination, BuildingType startBuilding) {
        if (nextDestinationNode == null) return null;
        boolean[][] occupied = new boolean[40][40];
        boolean[][] visited = new boolean[40][40];
        PriorityQueue<PathElement> queue = new PriorityQueue<>(Comparator.comparingDouble(a -> a.depth+heuristicDistance(a.node, destination, startBuilding.territorySquare)));
        gameState.getWorld().getMap().getNodes().forEach(node -> occupied[node.getPosition().x][node.getPosition().y] = true);
        gameState.getWorld().getMap().getScoutTowers().forEach(node -> occupied[node.getPosition().x][node.getPosition().y] = true);
        gameState.getWorld().getMap().getAvailableNodes()
                .forEach(node -> {
                    if (territoryOwner.get(node.getId()) != null && territoryOwner.get(node.getId()).equals(bot.getId())) {
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

    private void updateDistanceMap() {
        for (int i = 0; i < 40; i++) for (int j = 0; j < 40; j++) distanceMap[i][j] = -1;
        Queue<PathElement> queue = new LinkedList<>();
        bot.getTerritory().forEach(node -> {
            queue.add(new PathElement(null, new Position(node.getX(), node.getY()), 0));
            distanceMap[node.getX()][node.getY()] = 0;
        });
        while (!queue.isEmpty()) {
            PathElement element = queue.poll();
            for (Position node : getNeighbours(element.node, 1)) {
                if (distanceMap[node.getX()][node.getY()] == -1) {
                    distanceMap[node.getX()][node.getY()] = element.depth + 1;
                    queue.add(new PathElement(null, node, element.depth+1));
                }
            }
        }
    }

    private int unitsToCapture(BotTerritory terr) {
        if (terr.getOwner() != null && terr.getOwner().equals(bot.getId())) return 0;
        int maxPressure = terr.getOccupants().stream()
                .filter(o -> !o.getBotId().equals(bot.getId()))
                .max(Comparator.comparingInt(TerritoryOccupant::getPressure))
                .orElse(new TerritoryOccupant(bot.getId(), 0, 0))
                .getPressure();
        if (maxPressure == 0) return 0;
        int currentUnits = terr.getOccupants().stream()
                .filter(o -> o.getBotId().equals(bot.getId()))
                .mapToInt(TerritoryOccupant::getCount)
                .findFirst()
                .orElse(0);
        double factor = Math.floor(1 + 10.0/(getBaseDistance(new Position(terr.getX(), terr.getY()))+0.01));
        return (int)Math.ceil((maxPressure + 1)/factor) - (currentUnits + 1);
    }

    private void fight() {
        int extraSoldiers = (int)(Math.max(0, 0.2*averageK-botActions[ActionTypes.OCCUPY_LAND.value]));
        extraSoldiers = Math.min(extraSoldiers, botActions[0]);
        List<BotTerritory> potential = borderNodes.stream()
                .filter(terr -> unitsToCaptureMemo.get(terr.getNodeOnLand()) > 0)
                .sorted(Comparator.comparingInt(terr -> unitsToCaptureMemo.get(terr.getNodeOnLand())))
                .collect(Collectors.toList());
        //System.out.println("EXTRA: " + extraSoldiers + ", BORDER: " + potential.size());
        for (BotTerritory terr : potential) if (unitsToCaptureMemo.get(terr.getNodeOnLand()) <= extraSoldiers) {
            int newUnits = unitsToCapture(terr) - (myActions.containsKey(terr.getNodeOnLand()) ? nodeActions.get(terr.getNodeOnLand()).stream()
                    .filter(ac -> ac.getActionType() == ActionTypes.OCCUPY_LAND.value)
                    .mapToInt(PlayerAction::getNumberOfUnits)
                    .sum() : 0);
            if (newUnits <= 0) continue;
            commandList.add(new CommandAction(ActionTypes.OCCUPY_LAND.value, newUnits, terr.getNodeOnLand()));
            //System.out.printf("Sending %d units to fight at %s,%s%n", newUnits, terr.getX(), terr.getY());
            extraSoldiers -= newUnits; BAD_UNITS += newUnits;
            botActions[0] -= newUnits; botActions[ActionTypes.OCCUPY_LAND.value] += newUnits;
            if (extraSoldiers <= 0) break;
        }
    }

    private void refreshStores() {
        Arrays.fill(botActions, 0);
        botActions[0] = bot.getAvailableUnits();
        botActions[ActionTypes.OCCUPY_LAND.value] = BAD_UNITS;
        bot.getActions().forEach(action -> botActions[action.getActionType()]+=action.getNumberOfUnits());
        nodeMap.clear();
        gameState.getWorld().getMap().getNodes().forEach(node -> nodeMap.put(node.getId(), node));
        availableNodeMap.clear();
        gameState.getWorld().getMap().getAvailableNodes().forEach(node -> availableNodeMap.put(node.getId(), node));
        territoryOwner.clear();
        for (int i = 0; i < 40; i++) for (int j = 0; j < 40; j++) {
            resourceMap[i][j] = null;
            territoryMap[i][j] = null;
            freeNodes[i][j] = 0;
        }
        gameState.getBots().forEach(b -> b.getTerritory().forEach(territory -> {
            territoryOwner.put(territory.getNodeOnLand(), territory.getOwner());
            territoryMap[territory.getX()][territory.getY()] = territory.getOwner();
        }));
        existingBuilding = new int[20];
        bot.getBuildings().forEach(node -> existingBuilding[node.getType()]++);
        gameState.getWorld().getMap().getNodes().forEach(node -> resourceMap[node.getPosition().x][node.getPosition().y] = node);
        capturedNodes.clear();
        bot.getBuildings().forEach(node -> {
            for (int i = -1; i <= 1; i++) for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                if (node.getPosition().x+i < 0 || node.getPosition().x+i >= 40 || node.getPosition().y+j < 0 || node.getPosition().y+j >= 40) continue;
                if (resourceMap[node.getPosition().x+i][node.getPosition().y+j] != null) {
                    capturedNodes.add(resourceMap[node.getPosition().x+i][node.getPosition().y+j]);
                }
            }
        });
        updateDistanceMap();
        nodeActions.clear();
        gameState.getBots().stream().filter(b -> b.getActions() != null).forEach(b -> b.getActions()
                .forEach(action -> {
                    nodeActions.putIfAbsent(action.getTargetNodeId(), new ArrayList<>());
                    nodeActions.get(action.getTargetNodeId()).add(action);
                }));
        myActions.clear();
        if (bot.getActions() != null) bot.getActions().forEach(action -> {
            myActions.putIfAbsent(action.getTargetNodeId(), new ArrayList<>());
            myActions.get(action.getTargetNodeId()).add(action);
        });
        borderNodes.clear();
        gameState.getBots().stream()
                .filter(b -> b.getTerritory() != null && b.getId() != null && !b.getId().equals(bot.getId()))
                .forEach(b -> b.getTerritory().forEach(territory -> {
                    if (territory.getOccupants().stream().anyMatch(o -> o.getBotId().equals(bot.getId()) && o.getCount() > 0)
                            || getNeighbours(new Position(territory.getX(), territory.getY()), 1).stream()
                            .anyMatch(n -> territoryMap[n.getX()][n.getY()] != null && territoryMap[n.getX()][n.getY()].equals(bot.getId()))) {
                        borderNodes.add(territory);
                        unitsToCaptureMemo.put(territory.getNodeOnLand(), unitsToCapture(territory));
                    }
                }));
        gameState.getWorld().getMap().getScoutTowers().forEach(node -> territoryMap[node.getPosition().x][node.getPosition().y] = UUID.randomUUID());
        currentQty = new int[] {0, bot.getWood(), bot.getFood(), bot.getStone(), bot.getGold(), bot.getHeat()};
        gameState.getWorld().getMap().getAvailableNodes().stream()
                .filter(node -> territoryOwner.get(node.getId()) != null && territoryOwner.get(node.getId()).equals(bot.getId()))
                .forEach(node -> freeNodes[node.getPosition().x][node.getPosition().y] = (int)getNeighbours(node.getPosition(), 1).stream().filter(pos -> territoryMap[pos.x][pos.y] == null).count());
    }

    public PlayerCommand computeNextPlayerAction() {
        long time = System.currentTimeMillis();
        commandList.clear();

        refreshStores();
        System.out.println("===================================================");
        System.out.printf("CURRENT TICK: %4d, POPULATION: %8d, AVAILABLE: %8d%n", gameState.getWorld().getCurrentTick(), bot.getPopulation(), bot.getAvailableUnits());
        System.out.printf("FOOD: %8d, WOOD: %8d, STONE: %8d, GOLD: %8d, HEAT: %8d%n", bot.getFood(), bot.getWood(), bot.getStone(), bot.getGold(), bot.getHeat());

        if (bot.getPopulation() >= 5 && getBuilders() < 1) build(expensiveMultiplierBuildings());
        if (botActions[0] >= 1 && botActions[ActionTypes.SCOUT.value] < 1 && !fullView) fullView = scout();
        fight();
        farm();
        if (botActions[0] > 5 && botActions[ActionTypes.SCOUT.value] < 2 && !fullView) fullView = scout();

        prevResources = new int[] {0, bot.getWood(), bot.getFood(), bot.getStone(), bot.getGold()};
        averageK -= averageK/50.0;
        averageK += botActions[0]/50.0;
        receivedBotState = false;
        //System.out.printf("BOT ACTIONS %s%n", Arrays.toString(botActions));
        System.out.printf("CURRENT SCORE: %8d, UNITS LEFT: %8d, DELTA T: %4d%n, AWAY: %d, TERR: %2d %% %n", calculateCurrentScore(), botActions[0], System.currentTimeMillis()-time, botActions[ActionTypes.OCCUPY_LAND.value], (int)(100*bot.getTerritory().size()/1600.0));
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
