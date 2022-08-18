package Models.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameState {
    private World world;
    List<BotObject> bots;
    UUID botId;
    List<PopulationTier> populationTiers;

    public GameState() {
        bots = new ArrayList<BotObject>();
    }

    public List<PopulationTier> getPopulationTiers() {
        return populationTiers;
    }

    public void setPopulationTiers(List<PopulationTier> populationTiers) {
        this.populationTiers = populationTiers;
    }

    public GameState(World world, UUID botId, List<BotObject> bots, List<PopulationTier> populationTiers) {
        this.world = world;
        this.botId = botId;
        this.bots = bots;
        this.populationTiers = populationTiers;
    }

    public GameState(World world, UUID botId, List<BotObject> bots) {
        this.world = world;
        this.botId = botId;
        this.bots = bots;
    }

    public BotObject getBotByID(UUID id) {
        for (BotObject bot : bots) {
            if (bot.getId().equals(id)) {
                return bot;
            }
        }
        return null;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public UUID getBotId() {
        return botId;
    }

    public void setBotId(UUID botId) {
        this.botId = botId;
    }

    public List<BotObject> getBots() {
        return bots;
    }

    public void setBots(List<BotObject> bots) {
        this.bots = bots;
    }
}