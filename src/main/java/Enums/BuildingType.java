package Enums;

public enum BuildingType {
    ERROR(0, new int[] {0, 0, 0}, 0, 0),
    BASE(1, new int[] {0, 0, 0}, 0, 3),
    QUARRY(6, new int[] {90, 90, 45}, 6, 2),
    FARMERS_GUILD(7, new int[] {50, 50, 25}, 4, 2),
    LUMBER_MILL(8, new int[] {40, 40, 15}, 2, 1),
    OUTPOST(9, new int[] {220, 220, 110}, 0, 3),
    ROAD(10, new int[] {50, 50, 35}, 0, 1);

    public final int value;
    public final int[] cost;
    public final int scoreMultiplier;
    public final int territorySquare;

    BuildingType(int value, int[] cost, int scoreMultiplier, int territorySquare) {
        this.value = value;
        this.cost = cost;
        this.scoreMultiplier = scoreMultiplier;
        this.territorySquare = territorySquare;
    }

    public static BuildingType valueOf(Integer value) {
        for (BuildingType buildingType : BuildingType.values()) {
            if (buildingType.value == value) return buildingType;
        }

        throw new IllegalArgumentException("Value not found");
    }
}
