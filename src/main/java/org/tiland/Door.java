package org.tiland;

import org.engine.scene.Entity;

enum DoorState {
    none,
    open,
    closed,
    openning
};

public class Door extends Entity {

    public DoorState state = DoorState.open;

    public String targetZone;
    public String targetDoor;
    public float  targetHeading;

    public boolean isFront;
    public boolean isTrigger;
    public boolean retainBounds;

    @Override
    public void update(float interval) {

        // Check for requested state change.
        if (!requestedStateName.equals(stateName)) {
            setState(DoorState.valueOf(requestedStateName));
        }

        if (state == DoorState.open) {
            return;
        }

        switch (state) {

            case openning:
                setRotation(0, 45, 0);
                setState(DoorState.open);
                break;

            default:
                break;
        }

        super.update(interval);
    }

    public void setState(DoorState state) {
        this.state = state;
        super.stateName = state.name();
    }

    public DoorState getState() {
        return state;
    }
}