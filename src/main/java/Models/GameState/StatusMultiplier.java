package Models.GameState;

import Enums.ResourceType;

public class StatusMultiplier {
    int woodReward;
    int foodReward;
    int stoneReward;
    int goldReward;
    int heatReward;

    public StatusMultiplier(int woodReward, int foodReward, int stoneReward, int goldReward, int heatReward) {
        this.woodReward = woodReward;
        this.foodReward = foodReward;
        this.stoneReward = stoneReward;
        this.goldReward = goldReward;
        this.heatReward = heatReward;
    }

    public int get(ResourceType type) {
        switch (type) {
            case WOOD:
                return woodReward;
            case FOOD:
                return foodReward;
            case STONE:
                return stoneReward;
            case GOLD:
                return goldReward;
            case HEAT:
                return heatReward;
            default:
                return 0;
        }
    }

    public int getWoodReward() {
        return woodReward;
    }

    public void setWoodReward(int woodReward) {
        this.woodReward = woodReward;
    }

    public int getFoodReward() {
        return foodReward;
    }

    public void setFoodReward(int foodReward) {
        this.foodReward = foodReward;
    }

    public int getStoneReward() {
        return stoneReward;
    }

    public void setStoneReward(int stoneReward) {
        this.stoneReward = stoneReward;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public void setGoldReward(int goldReward) {
        this.goldReward = goldReward;
    }

    public int getHeatReward() {
        return heatReward;
    }

    public void setHeatReward(int heatReward) {
        this.heatReward = heatReward;
    }
}
