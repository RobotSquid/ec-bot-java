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
    int[] botActions = new int[20];

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

    private double getDistance(Position pos) {
        return Math.sqrt(Math.pow(pos.x-bot.getBaseLocation().x, 2) + Math.pow(pos.y-bot.getBaseLocation().y, 2));
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
                .min(Comparator.comparingDouble(a -> getDistance(a.getPosition())))
                .orElse(null);
    }

    public void scout() {
        if (botActions[0] < 1) return;
        GameObject nearestScoutTower = findNearestScoutTower();
        if (nearestScoutTower != null) {
            commandList.add(new CommandAction(ActionTypes.SCOUT.value, 1, nearestScoutTower.getId()));
            System.out.printf("Scouting %s%n", nearestScoutTower.getId());
            botActions[0]--; botActions[ActionTypes.SCOUT.value]++;
        }
    }

    private double getScorePerTick(ResourceNode node) {
        return (double)node.getReward()/(node.getWorkTime()+getDistance(node.getPosition()));
    }

    private Optional<Double> getInvResourceEfficiency(int resourceType) {
        if (resourceType == 5) return Optional.of(1/5.0);
        return gameState.getWorld().getMap().getNodes().stream()
                .filter(node -> node.getType() == resourceType)
                .map(node1 -> 1/getScorePerTick(node1))
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
        double idealFarm = getInvResourceEfficiency(ResourceType.FOOD.value).orElse(0.0)*Math.max(pop/10.0, (2.0*pop-bot.getFood())/(10+(10-gameState.getWorld().getCurrentTick()%10)));
        double idealFire = getInvResourceEfficiency(ResourceType.HEAT.value).orElse(0.0)*Math.max(pop/10.0, (2.0*pop-bot.getHeat())/(10+(10-gameState.getWorld().getCurrentTick()%10)));
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
        int[] currentQty = {0, bot.getWood(), bot.getFood(), bot.getStone(), bot.getGold(), bot.getHeat()};
        TierResourceList maxQty = gameState.getPopulationTiers().get(bot.getCurrentTierLevel()).getTierMaxResources();
        int[] tierMax = {0, maxQty.getWood(), maxQty.getFood(), maxQty.getStone(), maxQty.getGold(), 1000000};
        for (int i = 1; i <= 5; i++) {
            idealUnits[i] = Math.min(Math.max(0, idealUnits[i]-getCurrentAssignedUnits(i)), getMaxExtraUnits(i));
            if (currentQty[i] >= tierMax[i]) idealUnits[i] = 0;
        }
        int[] resourceAllocations = {0, 0, 0, 0, 0, 0};
        for (int i = 0; i < botActions[0]; i++) resourceAllocations[pickRandomWeighted(idealUnits)]++;
        for (int i = 1; i <= 5; i++) resourceAllocations[i] = Math.min(resourceAllocations[i], getMaxExtraUnits(i));
        for (int i = 1; i <= 4; i++) if (resourceAllocations[i] > 0) {
            int finalI = i;
            List<ResourceNode> nodes = gameState.getWorld().getMap().getNodes().stream().filter(node -> node.getType() == finalI)
                            .sorted(Comparator.comparingDouble(a -> -getScorePerTick(a))).collect(Collectors.toList());
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

    private void build() {
        if (botActions[0] < 1) return;
        int[] existingBuilding = new int[20];
        bot.getBuildings().forEach(node -> existingBuilding[node.getType()]++);
        int[] toBuild = {BuildingType.QUARRY.value, BuildingType.FARMERS_GUILD.value, BuildingType.LUMBER_MILL.value};
        int k = Arrays.stream(toBuild).reduce((a, b) -> (1+0.5*existingBuilding[a])*BuildingType.valueOf(a).wood < (1+0.5*existingBuilding[b])*BuildingType.valueOf(b).wood ? a : b).getAsInt();
        int wood = (int)Math.ceil((1+0.5*existingBuilding[k])*BuildingType.valueOf(k).wood);
        int stone = (int)Math.ceil((1+0.5*existingBuilding[k])*BuildingType.valueOf(k).stone);
        int gold = (int)Math.ceil((1+0.5*existingBuilding[k])*BuildingType.valueOf(k).gold);
        if (wood > bot.getWood() || stone > bot.getStone() || gold > bot.getGold()) return;
        GenericNode closestNode = gameState.getWorld().getMap().getAvailableNodes().stream()
                .filter(node -> territoryOwner.get(node.getId()).compareTo(bot.getId()) == 0)
                .min(Comparator.comparingDouble(a -> getDistance(a.getPosition()))).orElse(null);
        if (closestNode == null) return;
        commandList.add(new CommandAction(k, 1, closestNode.getId()));
        botActions[0]--; botActions[k]++;
        System.out.printf("Building %s at %s%n", k, closestNode.getId());
        bot.setWood(bot.getWood()-wood);
        bot.setStone(bot.getStone()-stone);
        bot.setGold(bot.getGold()-gold);
    }

    public PlayerCommand computeNextPlayerAction() {
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
        System.out.printf("Available units: %s%n", botActions[0]);

        if (bot.getPopulation() >= 5) build();
        if (botActions[ActionTypes.SCOUT.value] < 1) scout();
        farm();

        receivedBotState = false;
        System.out.println("Sending Command " + commandCount);
        commandCount++;
        return new PlayerCommand(bot.getId(), commandList);
    }
}
