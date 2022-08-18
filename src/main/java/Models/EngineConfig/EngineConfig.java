package Models.EngineConfig;

import Models.GameState.PopulationTier;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class EngineConfig {
    public String runnerUrl;
    public String runnerPort;
    public Integer botCount;
    public Integer maxTicks;
    public Integer scoutWorkTime;
    public Integer tickRate;
    public Integer regionSize = 0;
    public Integer processTick;

    public Integer baseZoneSize;
    public Integer numberOfRegionsInMapLength = 0;
    public Integer worldLength = regionSize * numberOfRegionsInMapLength;
    public Integer worldArea = worldLength ^ 2;
    public Integer resourceWorldCoverage;
    public double populationDecreaseRatio;
    public Integer worldSeed;
    public HashMap<String, BigDecimal> consumptionRatio;//null
    public HashMap<String, Integer> scoreRates;//null
    public ResourceScoreMultiplier resourceScoreMultiplier;
    public UnitConsumptionRatio unitConsumptionRatio;
    public List<PopulationTier> populationTiers;
    public Integer startingFood;

    public Integer startingUnits;

    public ResourceGenerationConfig resourceGenerationConfig;

    public Seeds seeds;
    public Integer minimumPopulation;

    public ResourceImportance resourceImportance;
    public Integer minimumUnits;
}
