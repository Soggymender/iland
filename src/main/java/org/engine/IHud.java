package org.engine;

public interface IHud {

    Entity[] getEntities();

    default void shutdown() {
        Entity[] entities = getEntities();
        for (Entity entity : entities) {
            entity.getMesh().shutdown();
        }
    }
}
