package Enums;

public enum ActionTypes {
  ERROR(0),
  SCOUT(1),
  MINE(2),
  FARM(3),
  LUMBER(4),
  START_CAMPFIRE(5),
  QUARRY(6),
  FARMERS_GUILD(7),
  LUMBER_MILL(8),
  ROAD(9),
  OUTPOST(10),
  OCCUPY_LAND(11),
  LEAVE_LAND(12);

  public final Integer value;

  ActionTypes(Integer value) {
    this.value = value;
  }

  public static ActionTypes valueOf(Integer value) {
    for (ActionTypes actionTypes : ActionTypes.values()) {
      if (actionTypes.value == value) return actionTypes;
    }

    throw new IllegalArgumentException("Value not found");
  }
}