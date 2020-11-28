package org.tiland;

import org.engine.scene.Entity;

public class Trigger extends Entity {

    private Script script = null;
    private TriggerType type = TriggerType.NONE;

    public Trigger(Script script, TriggerType type) {
          
        this.script = script;
        this.type = type;
    }

    public Script getScript() {
        return script;
    }
} 