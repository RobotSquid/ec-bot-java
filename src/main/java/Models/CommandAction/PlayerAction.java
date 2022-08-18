package Models.CommandAction;

import Enums.ActionTypes;

import java.util.UUID;

public class PlayerAction {
    UUID targetNodeId;
    int numberOfUnits;
    int tickActionCompleted;
    int tickActionStart;
    int tickReceived;
    int actionType;

    public PlayerAction(UUID targetNodeId, int numberOfUnits, int tickActionCompleted, int tickActionStart, int tickReceived, int actionType) {
        this.targetNodeId = targetNodeId;
        this.numberOfUnits = numberOfUnits;
        this.tickActionCompleted = tickActionCompleted;
        this.tickActionStart = tickActionStart;
        this.tickReceived = tickReceived;
        this.actionType = actionType;
    }

    public UUID getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(UUID targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public int getNumberOfUnits() {
        return numberOfUnits;
    }

    public void setNumberOfUnits(int numberOfUnits) {
        this.numberOfUnits = numberOfUnits;
    }

    public int getTickActionCompleted() {
        return tickActionCompleted;
    }

    public void setTickActionCompleted(int tickActionCompleted) {
        this.tickActionCompleted = tickActionCompleted;
    }

    public int getTickActionStart() {
        return tickActionStart;
    }

    public void setTickActionStart(int tickActionStart) {
        this.tickActionStart = tickActionStart;
    }

    public int getTickReceived() {
        return tickReceived;
    }

    public void setTickReceived(int tickReceived) {
        this.tickReceived = tickReceived;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }
}
