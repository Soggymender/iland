package org.engine;

import org.engine.renderer.Skybox;

public class Scene {

    private Entity[] entities;

    private Skybox skybox;

    private SceneLighting sceneLighting;

    public Entity[] getEntities() {
        return entities;
    }

    public void setEntities(Entity[] entities) {
        this.entities = entities;
    }

    public Skybox getSkybox() {
        return skybox;
    }

    public void setSkybox(Skybox skybox) {
        this.skybox = skybox;
    }

    public SceneLighting getSceneLighting() {
        return sceneLighting;
    }

    public void setSceneLighting(SceneLighting sceneLighting) {
        this.sceneLighting = sceneLighting;
    }
}
