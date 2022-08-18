package Models.GameState;

import java.util.UUID;


public class ResourceNode extends GenericNode {
    int amount;
    int reward;
    int workTime;
    RegenerationRate regenerationRate;
    int currentRegenTick;
    int maxResourceAmount;

    public ResourceNode(UUID id, int gameObjectType, Position position, int type, int maxUnits, int currentUnits, int amount, int reward, int workTime, RegenerationRate regenerationRate, int currentRegenTick, int maxResourceAmount) {
        super(id, gameObjectType, position, type, maxUnits, currentUnits);
        this.amount = amount;
        this.reward = reward;
        this.workTime = workTime;
        this.regenerationRate = regenerationRate;
        this.currentRegenTick = currentRegenTick;
        this.maxResourceAmount = maxResourceAmount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getWorkTime() {
        return workTime;
    }

    public void setWorkTime(int workTime) {
        this.workTime = workTime;
    }

    public RegenerationRate getRegenerationRate() {
        return regenerationRate;
    }

    public void setRegenerationRate(RegenerationRate regenerationRate) {
        this.regenerationRate = regenerationRate;
    }

    public int getCurrentRegenTick() {
        return currentRegenTick;
    }

    public void setCurrentRegenTick(int currentRegenTick) {
        this.currentRegenTick = currentRegenTick;
    }

    public int getMaxResourceAmount() {
        return maxResourceAmount;
    }

    public void setMaxResourceAmount(int maxResourceAmount) {
        this.maxResourceAmount = maxResourceAmount;
    }
}