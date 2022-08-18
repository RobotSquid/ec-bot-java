package Models.GameState;

public class RegenerationRate {
    int ticks = 0;
    int amount = 0;

    public RegenerationRate(int ticks, int amount) {
        this.ticks = ticks;
        this.amount = amount;
    }

    public int getTicks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}