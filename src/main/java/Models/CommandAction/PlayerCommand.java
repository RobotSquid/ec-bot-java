package Models.CommandAction;

import java.util.List;
import java.util.UUID;


public class PlayerCommand {
    UUID playerID = null;
    List<CommandAction> actions = null;
    public PlayerCommand(UUID playerID, List<CommandAction> actions) {
        this.playerID = playerID;
        this.actions = actions;
    }
    public UUID getPlayerID() {
        return playerID;
    }
    public void setPlayerID(UUID playerID) {
        this.playerID = playerID;
    }
    public List<CommandAction> getActions() {
        return actions;
    }
    public void setActions(List<CommandAction> actions) {
        this.actions = actions;
    }
}
