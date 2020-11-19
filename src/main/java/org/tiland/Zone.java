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

    String zoneName;
    Vector3f zoneOffset = new Vector3f();
    float zoneHeading;

    String requestedZoneName = new String();
    String requestedDoorName = new String();

    Vector3f avatarStart;

    public List<Npc> npcs;

    public List<Door> doors;
    public List<Ladder> ladders;

    BoundingBox avatarBounds;
    BoundingBox cameraBounds;

    Scene scene = null;
    SceneLoader.IEventHandler sceneLoader;

    Entity zoneRoot = null;

    Hud hud;

    public Zone(Scene scene, SceneLoader.IEventHandler sceneLoader, Hud hud) {

        this.scene = scene;

        // Add the scene root. Every zone element should be parented to the zone root so that
        // it can be located and unloaded easily during zone change.
        
    
        avatarStart = new Vector3f();

        npcs = new ArrayList<>();

        doors = new ArrayList<>();
        ladders = new ArrayList<>();

        avatarBounds = new BoundingBox();
        cameraBounds = new BoundingBox();

        this.sceneLoader = sceneLoader;

        this.hud = hud;
    }

    public void requestZone(String zoneName, String doorName) {
        requestedZoneName = zoneName;
        requestedDoorName = doorName;
    }

    public String getRequestedZone() {
        return requestedZoneName;
    }

    public boolean enteredByDoor() {
        return !requestedDoorName.isEmpty();
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

        zoneName = requestedZoneName;
        requestedZoneName = "";

        if (!requestedDoorName.isEmpty()) {

            for (int i = 0; i < doors.size(); i++) {

                Door door = doors.get(i);
                if (door.getName().equals(requestedDoorName)) {
                    
                    setAvatarStart(door.getPosition());
                    return;
                }
            }

            for (int i = 0; i < ladders.size(); i++) {

                if (ladders.get(i).getName().equals(requestedDoorName)) {

                    setAvatarStart(ladders.get(i).getPosition());
                    return;
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

        npcs.clear();

        doors.clear();
        ladders.clear();

        avatarBounds.reset();
        cameraBounds.reset();
    }

    public void setAvatarStart(Vector3f avatarStart) {

        this.avatarStart = new Vector3f(avatarStart);
    }

    public void setMetadata(Map<String, String>properties) {
    
        String offsetString = properties.get("p_offset");
        if (offsetString.length() > 0) {
            String[] coords = offsetString.split(",");
        
            zoneOffset.x = Float.parseFloat(coords[0]);
            zoneOffset.y = Float.parseFloat(coords[1]);
            zoneOffset.z = Float.parseFloat(coords[2]);
        }

        String headingString = properties.get("p_heading");
        if (headingString.length() > 0) {
            zoneHeading = Float.parseFloat(headingString);
        }
    }

    public Entity createNpc(Map<String, String>properties) {
    
        String meshFilename = properties.get("p_filename");
        //String scriptFilename = properties.get("p_script");

        Npc npc = new Npc(scene, new Vector3f(0, 5, 0), meshFilename);

        npcs.add(npc);

        return npc;
    }

    public Entity createDoor(Map<String, String>properties, boolean isTrigger) {
    
        Door door = new Door();

        door.targetZone = properties.get("p_target_zone");
        door.targetDoor = properties.get("p_target_object");
        door.isTrigger = isTrigger;

        doors.add(door);

        return door;
    }

    public Entity createLadder(Map<String, String>properties) {
    
        Ladder ladder = new Ladder();

        ladders.add(ladder);

        ladder.flags.collidable = true;
        ladder.flags.platform_collision = true;


        return ladder;
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

    public boolean enterDoor(Entity entity, boolean use) {

        String targetZone = null;
        String targetDoor = null;

        for (int i = 0; i < doors.size(); i++) {

            Door door = doors.get(i);

            // If there was no interaction, skip doors that require it.
            if (!door.isTrigger && !use) {
                continue;
            }

            // If this is a trigger door, and we entered through it, and we're no longer overlapping with it
            // clear it out, allowing re-entry.
            boolean enteredFromHere = door.getName().equals(requestedDoorName);

            if (entitiesOverlap(entity, door, 0.5f, 0.95f)) {

                if (enteredFromHere && door.isTrigger) {
                    continue;
                }

                targetZone = door.targetZone;
                targetDoor = door.targetDoor;
            
                break;
            } else {
                if (enteredFromHere) {
                    requestedDoorName = null;
                }
            }
        }

        if (targetZone != null) {
            
            requestZone(targetZone, targetDoor);

            return true;
        }

        return false;
    }

    public Entity interact(Entity entity, Entity other) {

        if (other != null) {

            Npc npc = (Npc)other;

            if (entitiesNear(entity, npc, 1.0f, 1.0f)) {
                npc.interact(hud);
                return other;
            }

            npc.endInteraction(hud);
            return null;
        }

        for (int i = 0; i < npcs.size(); i++) {

            Npc npc = npcs.get(i);

            if (entitiesNear(entity, npc, 1.0f, 1.0f)) {
            
                npc.interact(hud);
                return npc;
            }
        }

        return null;
    } 
    
    public Entity validateInteraction(Entity entity, Entity other) {

        if (other != null) {

            Npc npc = (Npc)other;

            if (entitiesNear(entity, npc, 1.0f, 1.0f)) {
                return other;
            }

            npc.endInteraction(hud);
            return null;
        }

        return null;
    }     

    public Entity climb(Entity entity) {

        for (int i = 0; i < ladders.size(); i++) {

            Ladder ladder = ladders.get(i);
 
            // Over ladder.
            if (entitiesOverlap(entity, ladder, 0.5f, 0.5f)) {
                return ladder;
            }
        }

        return null;
    }

    public boolean entitiesOverlap(Entity entityA, Entity entityB, float xMinOverlapPct, float yMinOverlapPct) {

        Vector3f entityAPos = entityA.getPosition();
        BoundingBox entityABox = entityA.getBBox();

        Vector3f entityBPos = entityB.getPosition();
        BoundingBox entityBBox = entityB.getBBox();

        float aL = entityAPos.x + entityABox.min.x;
        float aR = entityAPos.x + entityABox.max.x;
        float aT = entityAPos.y + entityABox.max.y;
        float aB = entityAPos.y + entityABox.min.y;

        float bL = entityBPos.x + entityBBox.min.x;
        float bR = entityBPos.x + entityBBox.max.x;
        float bT = entityBPos.y + entityBBox.max.y;
        float bB = entityBPos.y + entityBBox.min.y;

        if ((aL <= bR && aR >= bL) && (aT >= bB && aB <= bT)) {

            float width;
            float height;

            if (aR > bR) {
                width = bR - aL;
            } else {
                width = aR - bL;
            }

            if (aB < bT) {
                height = aT - bB;
            } else {
                height = bT - aB;
            }

            float aWidth = aR - aL;
            float aHeight = aT - aB;

            float xPct = width / aWidth;
            float yPct = height / aHeight;

            if (xPct >= xMinOverlapPct && yPct >= yMinOverlapPct) {
                return true;
            }

            /*
            float xOverlap = (entityAPos.x + entityABox.max.x) - (entityBPos.x + entityBBox.min.x);
            float yOverlap = (entityAPos.y + entityABox.max.y) - (entityBPos.y + entityBBox.min.y);

            float xOverlapPct = Math.abs(xOverlap) / (entityABox.max.x - entityABox.min.x);
            float yOverlapPct = Math.abs(yOverlap) / (entityABox.max.y - entityABox.min.y);

            if (xOverlapPct >= xMinOverlapPct && yOverlapPct >= yMinOverlapPct) {
                return true;
            }
            */
        }

        return false;
    }

    public boolean entitiesNear(Entity entityA, Entity entityB, float xMinDistance, float yMinDistance) {

        Vector3f entityAPos = entityA.getPosition();
        BoundingBox entityABox = entityA.getBBox();

        Vector3f entityBPos = entityB.getPosition();
        BoundingBox entityBBox = entityB.getBBox();

        Vector3f xVec = new Vector3f(entityBPos);
        float dist;

        xVec.sub(entityAPos);
        xVec.y = 0.0f;

        dist = xVec.length();

        if (dist > xMinDistance) {
            return false;
        }

        Vector3f yVec = new Vector3f(entityBPos);
        yVec.sub(entityAPos);
        yVec.x = 0.0f;

        dist = yVec.length();

        if (dist > yMinDistance) {
            return false;
        }

        return true;
    }

    public String getName() {
        return zoneName;
    }

    public Vector3f getMapOffset() {
        return zoneOffset;
    }

    public float getMapHeading() {
        return zoneHeading;
    }
}