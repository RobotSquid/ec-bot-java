package Models.GameState;

import java.util.UUID;

public class TerritoryOccupant {
    UUID botId;
    int count;
    int pressure;

    public TerritoryOccupant(UUID botId, int count, int pressure) {
        this.botId = botId;
        this.count = count;
        this.pressure = pressure;
    }

    public UUID getBotId() {
        return botId;
    }

    public void setBotId(UUID botId) {
        this.botId = botId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }
}
