package Models.GameState;

import java.util.UUID;

public class GameObject {
    UUID id;
    int gameObjectType;
    Position position;

    public GameObject(UUID id, int gameObjectType, Position position) {
        this.id = id;
        this.gameObjectType = gameObjectType;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getGameObjectType() {
        return gameObjectType;
    }

    public void setGameObjectType(int gameObjectType) {
        this.gameObjectType = gameObjectType;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
