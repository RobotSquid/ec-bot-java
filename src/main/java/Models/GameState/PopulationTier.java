package Models.GameState;

import java.util.List;

public class PopulationTier {
    int level = 0;
    String name = null;
    int maxPopulation = 0;
    List<Double> populationChangeFactorRange = null;
    TierResourceList tierResourceConstraints = null;
    TierResourceList tierMaxResources = null;

    public PopulationTier(int level, String name, int maxPopulation, List<Double> populationChangeFactorRange, TierResourceList tierResourceConstraints, TierResourceList tierMaxResources) {
        this.level = level;
        this.name = name;
        this.maxPopulation = maxPopulation;
        this.populationChangeFactorRange = populationChangeFactorRange;
        this.tierResourceConstraints = tierResourceConstraints;
        this.tierMaxResources = tierMaxResources;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxPopulation() {
        return maxPopulation;
    }

    public void setMaxPopulation(int maxPopulation) {
        this.maxPopulation = maxPopulation;
    }

    public List<Double> getPopulationChangeFactorRange() {
        return populationChangeFactorRange;
    }

    public void setPopulationChangeFactorRange(List<Double> populationChangeFactorRange) {
        this.populationChangeFactorRange = populationChangeFactorRange;
    }

    public TierResourceList getTierResourceConstraints() {
        return tierResourceConstraints;
    }

    public void setTierResourceConstraints(TierResourceList tierResourceConstraints) {
        this.tierResourceConstraints = tierResourceConstraints;
    }

    public TierResourceList getTierMaxResources() {
        return tierMaxResources;
    }

    public void setTierMaxResources(TierResourceList tierMaxResources) {
        this.tierMaxResources = tierMaxResources;
    }
}

