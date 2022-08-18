package Enums;

public enum BuildingType {
    ERROR(0, 0, 0, 0),
    BASE(1, 0, 0, 0),
    QUARRY(6, 90, 90, 45),
    FARMERS_GUILD(7, 50, 50, 25),
    LUMBER_MILL(8, 40, 40, 15),
    OUTPOST(9, 220, 220, 110),
    ROAD(10, 50, 50, 35);

    public final int value;
    public final int wood;
    public final int stone;
    public final int gold;

    BuildingType(int value, int wood, int stone, int gold) {
        this.value = value;
        this.wood = wood;
        this.stone = stone;
        this.gold = gold;
    }

    public static BuildingType valueOf(Integer value) {
        for (BuildingType buildingType : BuildingType.values()) {
            if (buildingType.value == value) return buildingType;
        }

        throw new IllegalArgumentException("Value not found");
    }
}
