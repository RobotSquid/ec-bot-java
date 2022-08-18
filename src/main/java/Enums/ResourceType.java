package Enums;

public enum ResourceType {
    ERROR(0, ActionTypes.ERROR),
    WOOD(1, ActionTypes.LUMBER),
    FOOD(2, ActionTypes.FARM),
    STONE(3, ActionTypes.MINE),
    GOLD(4, ActionTypes.MINE),
    HEAT(5, ActionTypes.START_CAMPFIRE);

    public final int value;
    public final ActionTypes actionType;
    private ResourceType(int value, ActionTypes actionType)
    {
        this.value = value;
        this.actionType = actionType;
    }

    public int getValue() {
        return value;
    }

    public static ResourceType valueOf(int Value){
        for (ResourceType Types : ResourceType.values()) {
            if (Types.value == Value) return Types;
          }
      
          throw new IllegalArgumentException("Value not found"); 
    }

}