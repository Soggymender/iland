package org.tiland;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import org.engine.core.BoundingBox;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;

public class Zone {

    String requestedZoneName = new String();
    String requestedDoorName = new String();

    Vector3f avatarStart;

    public List<Door> doors;

    BoundingBox avatarBounds;
    BoundingBox cameraBounds;

    Scene scene = null;
    SceneLoader.IEventHandler sceneLoader;

    Entity zoneRoot = null;

    public Zone(Scene scene, SceneLoader.IEventHandler sceneLoader) {

        this.scene = scene;

        // Add the scene root. Every zone element should be parented to the zone root so that
        // it can be located and unloaded easily during zone change.
        
    
        avatarStart = new Vector3f();

        doors = new ArrayList<>();

        avatarBounds = new BoundingBox();
        cameraBounds = new BoundingBox();

        this.sceneLoader = sceneLoader;
    }

    public void requestZone(String zoneName, String doorName) {
        requestedZoneName = zoneName;
        requestedDoorName = doorName;
    }

    public String getRequestedZone() {
        return requestedZoneName;
    }

    public void loadRequestedZone() {
        if (requestedZoneName.isEmpty()) {
            return;
        }

        if (zoneRoot != null) {
            scene.removeEntity(zoneRoot);
        }

        reset();
        load(requestedZoneName);

        requestedZoneName = "";

        if (!requestedDoorName.isEmpty()) {

            for (int i = 0; i < doors.size(); i++) {

                Door door = doors.get(i);
                if (door.getName().equals(requestedDoorName)) {
                    
                    setAvatarStart(door.getPosition());
                    break;
                }
            }
        }
    }

    public void load(String zoneName) {
        
        zoneRoot = new Entity();

        String zoneFilename = new String("src/main/resources/tiland/models/" + zoneName + ".fbx");
        SceneLoader.loadEntities(zoneRoot, zoneFilename, "src/main/resources/tiland/textures/", sceneLoader);

        scene.addEntity(zoneRoot);
    }

    public void reset() {

        avatarStart.zero();

        doors.clear();

        avatarBounds.reset();
        cameraBounds.reset();
    }

    public void setAvatarStart(Vector3f avatarStart) {

        this.avatarStart = new Vector3f(avatarStart);
    }

    public Entity createDoor(Map<String, String>properties) {
    
        Door door = new Door();

        door.targetZone = properties.get("p_target_zone");
        door.targetDoor = properties.get("p_target_object");

        doors.add(door);

        return door;
    }

    public void addEntity(Entity entity) {

        expandBounds(entity.getPosition(), entity.getBBox());
    }

    public void expandBounds(Vector3f pos, BoundingBox bBox) {

        if (pos.x + bBox.min.x < avatarBounds.min.x) {
            avatarBounds.min.x = pos.x + bBox.min.x;
        }

        if (pos.y + bBox.min.y < avatarBounds.min.y) {
            avatarBounds.min.y = pos.y + bBox.min.y;
        }

        if (pos.x + bBox.max.x > avatarBounds.max.x) {
            avatarBounds.max.x = pos.x + bBox.max.x;
        }

        if (pos.y + bBox.max.y > avatarBounds.max.y) {
            avatarBounds.max.y = pos.y + bBox.max.y;
        }

        cameraBounds.max.x = avatarBounds.max.x;
        cameraBounds.min.x = avatarBounds.min.x;
        cameraBounds.max.y = avatarBounds.max.y;
        cameraBounds.min.y = avatarBounds.min.y;

        float fov = 60.0f; // TODO: pull this from the camera.
        float camz = 4.25f; // TODO: look this up too.

        float halfFov = (float)java.lang.Math.toRadians(fov);

        // Shift this bounds to the right  to account for FOV.
        cameraBounds.min.x += halfFov * camz;
        cameraBounds.max.x -= halfFov * camz;
    }

    public BoundingBox getAvatarBounds() {
        return avatarBounds;
    }

    public BoundingBox getCameraBounds() {
        return cameraBounds;
    }

    public boolean enterDoor(Entity entity) {

        String targetZone = null;
        String targetDoor = null;

        Vector3f entityPos = entity.getPosition();
        BoundingBox entityBox = entity.getBBox();

        for (int i = 0; i < doors.size(); i++) {

            Door door = doors.get(i);
            Vector3f doorPos = door.getPosition();
            BoundingBox doorBox = door.getBBox();
 
            // Over door.
            if ((entityPos.x + entityBox.min.x <= doorPos.x + doorBox.max.x && entityPos.x + entityBox.max.x >= doorPos.x + doorBox.min.x) &&
                (entityPos.y + entityBox.min.y <= doorPos.y + doorBox.max.y && entityPos.y + entityBox.max.y >= doorPos.y + doorBox.min.y)) {

                float xOverlap = (doorPos.x + doorBox.min.x) - (entityPos.x + entityBox.max.x);
                float yOverlap = (doorPos.y + doorBox.min.y) - (entityPos.y + entityBox.max.y);

                float xOverlapPct = Math.abs(xOverlap) / (doorBox.max.x - doorBox.min.x);
                float yOverlapPct = Math.abs(yOverlap) / (doorBox.max.y - doorBox.min.y);
    
                if (xOverlapPct > 0.5f && yOverlapPct > 0.95f) {

                    targetZone = door.targetZone;
                    targetDoor = door.targetDoor;

                    break;
                }
            }
        }

        if (targetZone != null) {
            
            requestZone(targetZone, targetDoor);

            return true;
        }

        return false;
    }   
}