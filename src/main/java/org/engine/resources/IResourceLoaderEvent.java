package org.engine.resources;

import org.engine.Entity;

public interface IResourceLoaderEvent {

    public void resourceLoadedEvent(String type, Entity entity) throws Exception;
}
