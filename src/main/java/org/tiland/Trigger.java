package org.tiland;

import org.engine.scene.*;
import org.engine.scene.Entity;

enum TriggerState {
    none,
    open,
    closed,
    openning,
    locked,
    unlock
};

public class Trigger extends Entity {

    Scene scene = null;

    private Script script = null;
    private TriggerType type = TriggerType.NONE;

    public TriggerState state = TriggerState.none;

    String lockEntityName = null;
    Entity lockEntity = null;

    public Trigger(Scene scene, Script script, TriggerType type) {
          
        this.scene = scene;
        this.script = script;
        this.type = type;
    }

    public Script getScript() {
        return script;
    }

    @Override
    public void update(float interval) {

        // Check for requested state change.
        if (!requestedStateName.equals(stateName)) {
            setState(TriggerState.valueOf(requestedStateName));
        }

        if (state == TriggerState.none) {
            return;
        }

        switch (state) {

            case openning:
                setRotation(0, 115, 0);
                setState(TriggerState.open);
                break;

            default:
                break;
        }

        super.update(interval);
    }

    public void setState(TriggerState state) {
        this.state = state;
        super.stateName = state.name();

        switch (state) {

            case locked:

                if (lockEntity == null) {
                    lockEntity = scene.findEntity(lockEntityName);
                }

                if (lockEntity != null) {
                    lockEntity.setVisible(true);
                }
                break;

            case unlock:
                
                if (lockEntity == null) {
                    lockEntity = scene.findEntity(lockEntityName);
                }

                if (lockEntity != null) {
                    lockEntity.setVisible(false);
                    requestState(TriggerState.closed.toString());
                }

                break;

            default:
                break;
        }
    }

    public TriggerState getState() {
        return state;
    }

    public void setLockEntityName(String name) {
        this.lockEntityName = name;
    }
}