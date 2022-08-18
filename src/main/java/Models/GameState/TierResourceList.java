package Models.GameState;

public class TierResourceList {
    int food;
    int wood;
    int stone;
    int gold;

    public TierResourceList(int food, int wood, int stone, int gold) {
        this.food = food;
        this.wood = wood;
        this.stone = stone;
        this.gold = gold;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getWood() {
        return wood;
    }

    public void setWood(int wood) {
        this.wood = wood;
    }

    public int getStone() {
        return stone;
    }

    public void setStone(int stone) {
        this.stone = stone;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}
