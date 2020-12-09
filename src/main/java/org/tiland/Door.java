package org.tiland;

import org.engine.scene.Entity;

enum DoorState {
    NONE,
    OPEN,
    CLOSED,
    OPENNING
};

public class Door extends Entity {

    public DoorState state = DoorState.OPEN;

    public String targetZone;
    public String targetDoor;

    public boolean isFront;
    public boolean isTrigger;

    @Override
    public void update(float interval) {

        if (state == DoorState.OPEN) {
            return;
        }

        switch (state) {

            case OPENNING:
                setRotation(0, 45, 0);
                setState(DoorState.OPEN);
                break;

            default:
                break;
        }

        super.update(interval);
    }

    public void setState(DoorState state) {
        this.state = state;
    }

    public DoorState getState() {
        return state;
    }
}