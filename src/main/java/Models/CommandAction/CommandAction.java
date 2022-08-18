package Models.CommandAction;

import java.util.UUID;

public class CommandAction {
    int type = 0;
    int units = 0;
    UUID id = null;

    public CommandAction(int type, int units, UUID id) {
        this.type = type;
        this.units = units;
        this.id = id;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public int getUnits() {
        return units;
    }
    public void setUnits(int units) {
        this.units = units;
    }
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
}
