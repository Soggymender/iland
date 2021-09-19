package org.tiland;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import org.engine.renderer.Camera;
import org.engine.core.BoundingBox;
import org.engine.core.Transform;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;

public class Zone {
    
    final float maxTalkX = 0.75f;
    final float maxTalkY = 1.0f;

    final float maxTakeX = 0.25f;
    final float maxTakeY = 0.5f;

    final float maxEnterX = 0.5f;
    final float maxEnterY = 0.95f;

    final float maxClimbX = 0.5f;
    final float maxClimbY = 0.5f;

    final float maxTriggerX = 0.25f;
    final float maxTriggerY = 0.25f;

    String zoneName;
    Vector3f zoneMapOffset = new Vector3f();
    float zoneMapHeading;

    Vector3f newZoneMapOffset = new Vector3f();
    float newZoneMapHeading = 0.0f;

    Vector3f entryDoorPosition = new Vector3f(); // Position of the entered door for calculating the requested zone's offset relative to the two connecting doors.
    Vector3f zoneOffset = new Vector3f(); // Zone offset calculated from the two connecting doors.

    String requestedZoneName = new String();
    String requestedDoorName = new String();
    float  requestedTargetHeading = 0.0f;

    ZoneTransition transition = new ZoneTransition();

    Vector3f avatarStart;

    public ArrayList<Script> scripts = new ArrayList<Script>();

    public Avatar avatar;
    public List<Npc> npcs;

    public List<Door> doors;
    public List<Ladder> ladders;
    public List<Trigger> triggers;

    boolean retainEntryBounds = false;
    String boundsName = null;
    BoundingBox avatarBounds;
    BoundingBox cameraBounds;
    BoundingBox oldAvatarBounds;
    BoundingBox oldCameraBounds;

    public Camera camera;

    Scene scene = null;
    SceneLoader.IEventHandler sceneLoader;

    public Entity zoneRoot = null;

    Hud hud;

    public Zone(Scene scene, SceneLoader.IEventHandler sceneLoader, Hud hud) {

        this.scene = scene;

        // Add the scene root. Every zone element should be parented to the zone root so that
        // it can be located and unloaded easily during zone change.
        
    
        avatarStart = new Vector3f();

        npcs = new ArrayList<>();

        doors = new ArrayList<>();
        ladders = new ArrayList<>();

        triggers = new ArrayList<>();

        avatarBounds = new BoundingBox();
        cameraBounds = new BoundingBox();

        this.sceneLoader = sceneLoader;

        this.hud = hud;

        loadScripts();
    }

    public void update(float interval) {
        transition.update(interval);
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    private void loadScripts() {



        List<String> scriptFiles = new ArrayList<String>();
        File dir = new File("src/main/resources/tiland/scripts/");
        for (File file : dir.listFiles()) {

            if (file.getName().endsWith((".txt"))) {
                scriptFiles.add(file.getName());
            }
        }
        
        for (String filename : scriptFiles) {
            scripts.add(new Script(filename));
        }
    }

    public void requestZone(String zoneName, String doorName, float targetHeading) {
        requestedZoneName = zoneName;
        requestedDoorName = doorName;
        requestedTargetHeading = targetHeading;
    }

    public String getRequestedZone() {
        return requestedZoneName;
    }

    public float getRequestedTargetHeading()
    {
        return requestedTargetHeading;
    }

    public boolean enteredByDoor() {
        return !requestedDoorName.isEmpty();
    }

    public void loadRequestedZone(MiniMap map) {

        if (requestedZoneName.isEmpty()) {
            return;
        }

        if (zoneRoot != null) {
            scene.removeEntity(zoneRoot, false);
        }

        reset();

        zoneName = requestedZoneName;
        load(requestedZoneName);

        requestedZoneName = "";

        if (retainEntryBounds) {
            //avatarBounds = oldAvatarBounds;
            cameraBounds = oldCameraBounds;
        }

        // If the zone metadata specified a specific bounds object, get it and bash the bounds to its
        // extents instead of using all zone entities.
        if (boundsName != null) {

            Entity entity = scene.findEntity(boundsName);
            if (entity != null) {

                avatarBounds.reset();
                cameraBounds.reset();

                avatarBounds.min.x = 9999.0f;
                avatarBounds.min.y = 9999.0f;
                avatarBounds.max.x = -9999.0f;
                avatarBounds.max.y = -9999.0f;

                expandBounds(entity.getPosition(), entity.getBBox());
            }
        }

        Entity entity = scene.findEntity(requestedDoorName);
        if (entity != null) {
            setAvatarStart(entity.getPosition());

            boolean entryIsLadder = (entity instanceof Ladder);

            // Try to calculate the zoneMapOffset
           // if (!retainEntryBounds) {
                Vector3f oldMapOffset = new Vector3f(map.offset);
                float oldMapHeading = map.heading;
                //  public float heading;

                // "map" position of entry door.
                Vector3f entryDoorPos = new Vector3f();

                entryDoorPos.set(entryDoorPosition);
                entryDoorPos.z = 0.0f;
                entryDoorPos.sub(oldAvatarBounds.min);

                newZoneMapOffset.set(oldMapOffset);
                newZoneMapOffset.add(entryDoorPos);

                /*
                Auto gap rules:
                - entry exit doors on the same plane are a gap in X
                - entry exit ladders on the same plane are a gap in Y
                - entry on 0 heading, exit on 90 heading, gap in Z
                - entry on 90 heading, exit on 0 heading, gap in Z

                - "retain bounds" z tunneling doors gap on same plane. 
                    - have not tried z tunneling in 90 heading zone yet.
                */

                Vector3f gap = new Vector3f();

               // if

                if (retainEntryBounds) {

                    gap.set(0.0f, 0.0f, -1.0f);
                //    newZoneMapOffset.add(0.0f, 0.0f, -1.0f);
                } else if (entryIsLadder) {

                    newZoneMapHeading = zoneMapHeading;
                    
                    gap.set(0.0f, -5.0f, 0.0f);
                  
                } else if (oldMapHeading == 0 && requestedTargetHeading == 0) {

                    // New zone is rotated, or old zone is rotated and new zone is not. ie, new zone is on the XY plane.
                    newZoneMapHeading = zoneMapHeading;
                    
                    gap.set(5.0f, 0.0f, 0.0f);
                    //newZoneMapOffset.add(5.0f, 0.0f, 0.0f);

                } else {

                    newZoneMapHeading = oldMapHeading + requestedTargetHeading;
                    
                    if (newZoneMapHeading == -90.0f) {
                        gap.set(0.0f, 0.0f, 5.0f);
                //        newZoneMapOffset.add(0.0f, 0.0f, 5.0f);
                    } else {//} if (newZoneMapHeading == 90.0f) {
                        gap.set(0.0f, 0.0f, -5.0f);
                //        newZoneMapOffset.add(0.0f, 0.0f, -5.0f);
                    }
                }

                // Rotate the gap.
                if (gap.length() > 0.0f) {
            //        gap.rotateY(oldMapHeading);
                }

        //        newZoneMapOffset.add(gap);

                Vector3f newZoneOffset = new Vector3f();
                
                newZoneOffset.set(avatarBounds.min);
                newZoneOffset.sub(entity.getPosition());
                newZoneOffset.z = 0.0f;

                newZoneOffset.add(gap);

              //  newZoneOffset.rotateY(newZoneMapHeading);

                newZoneMapOffset.sub(oldMapOffset);
                newZoneMapOffset.rotateY((float)Math.toRadians(oldMapHeading));
                newZoneMapOffset.add(oldMapOffset);

                newZoneMapOffset.add(newZoneOffset);

                // Eh... sub origin, rotate?
           // }
        }


        /* Maybe I don't do this anymore? Doesn't seem like it.
           The code above means a door can link to any type of entity.
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
        */
    }

    public void load(String zoneName) {
        
        zoneRoot = new Entity();

        String zoneFilename = new String("src/main/resources/tiland/models/" + zoneName + ".fbx");
        SceneLoader.loadEntities(zoneRoot, zoneFilename, "src/main/resources/tiland/textures/", sceneLoader);

        // Reparent any items that were from another zone but moved here.
        for (int i = 0; i < npcs.size(); i++) {

            Npc npc = npcs.get(i);

            String npcZone = npc.getZone();

            if (npcZone != null)  {

                // This NPC was in this zone previously.
                if (npcZone.equals(zoneName)) {
                    npc.setParent(zoneRoot);
                }
            }
        }

        scene.addEntity(zoneRoot);

        // Retain entry bounds means we want to position the new zone relative to the old so that the 
        // avatar and camera framing and boundis are all consistent after entering the new zone. The
        // connecting doors are used to figure out the math but otherwise are not important. It could be
        // necessary if there are multiple doors to specify one to always use for the connection?

        // ALL of the loaded entities need to be offset. The exception is NPCs that get loaded once and
        // retain a persistent position within their home zone across loads.

        if (retainEntryBounds) {

            Entity entity = scene.findEntity(requestedDoorName);
            if (entity != null) {
             
                Vector3f offset = new Vector3f();

                offset.set(entryDoorPosition);
                offset.sub(entity.getPosition());

                //zoneRoot.getPosition().add(offset);

                // Iterate the children and apply the offset.
                for (Entity child : zoneRoot.children) {
                    if (child instanceof Npc && ((Npc)child).beenAround) {
                        continue;
                    }
                    child.getPosition().add(offset);
                }

                // Move the avatar bounds.
                avatarBounds.min.add(offset);
                avatarBounds.max.add(offset);
            }
        }    
    }

    public void reset() {

        avatarStart.zero();

        //npcs.clear();

        doors.clear();
        ladders.clear();

        triggers.clear();

        boundsName = null;

        // Copy the current bounds in case the new zone wants to retain them.
        oldAvatarBounds = new BoundingBox(avatarBounds);
        oldCameraBounds = new BoundingBox(cameraBounds);

        avatarBounds.reset();
        cameraBounds.reset();

        avatarBounds.min.x = 9999.0f;
        avatarBounds.min.y = 9999.0f;
        avatarBounds.max.x = -9999.0f;
        avatarBounds.max.y = -9999.0f;
    }

    /*
    Normally doors should be on layer 1 with the avatar in order to have proper background and
    foreground depth. But 1 is above the fade layer, and during heading transitions, we only
    want the entered / requested door to be visible so far off doors aren't swinging into view.
    Move all unentered doors to layer 0 to be obscured by fade.
    */
    public void hideUnenteredDoors() {

        for (int i = 0; i < doors.size(); i++) {

            Door door = doors.get(i);
            if (door.getName().equals(requestedDoorName)) {
                continue;
            }

            door.setLayer(0);
        }        
    }

    public void unhideUnenteredDoors() {

        for (int i = 0; i < doors.size(); i++) {

            Door door = doors.get(i);
            if (door.getName().equals(requestedDoorName)) {
                continue;
            }

            door.setLayer(1);
        }        
    }

    public void setAvatarStart(Vector3f avatarStart) {

        this.avatarStart = new Vector3f(avatarStart);
    }

    public void setMetadata(Map<String, String>properties) {
    
        if (properties.get("p_retain_bounds") != null) {
            retainEntryBounds = true;
        } else {
            retainEntryBounds = false;
        }

        boundsName = properties.get("p_bounds");

        String offsetString = properties.get("p_map_offset");
        if (offsetString == null) {
            offsetString = properties.get("p_offset");
        }

        if (offsetString.length() > 0) {
            String[] coords = offsetString.split(",");
        
            zoneMapOffset.x = Float.parseFloat(coords[0]);
            zoneMapOffset.y = Float.parseFloat(coords[1]);
            zoneMapOffset.z = Float.parseFloat(coords[2]);
        }

        String headingString = properties.get("p_map_heading");
        if (headingString == null) {
            headingString = properties.get("p_heading");
        }
        if (headingString.length() > 0) {
            zoneMapHeading = Float.parseFloat(headingString);
        }
    }

    public Entity loadNpc(String name, Map<String, String>properties, boolean isItem) {

        // Was this NPC created during a previous load?
        for (int i = 0; i < npcs.size(); i++) {

            Npc npc = npcs.get(i);

            String npcName = npc.getName();
            String npcHome = npc.getHome();
            //String npcZone = npc.getZone();

            if (npcName != null && npcHome != null)  {
                if (npcName.equals(name) && npcHome.equals(zoneName)) {

                    npc.beenAround = true;
                    return null; // Signal not to create or modify the existing NPC.
                }
            }
        }


        String meshFilename = properties.get("p_filename");
        String scriptFilename = properties.get("p_script") + ".txt";

        // Find the script for this NPC.
        Script script = null;
        if (scriptFilename != null && scriptFilename.length() > 0) {
            for (Script curScript : scripts) {
                if (curScript.name.equals(scriptFilename)) {
                    script = curScript;
                    break;
                }
            }
        }

        // If there was no script and this is an item, use the generic "take" script.
        if (isItem && script == null) {
            scriptFilename = "take.txt";

            if (scriptFilename != null && scriptFilename.length() > 0) {
                for (Script curScript : scripts) {
                    if (curScript.name.equals(scriptFilename)) {
                        script = curScript;
                        break;
                    }
                }
            }
        }

        Npc npc = new Npc(scene, this, new Vector3f(0, 5, 0), zoneName, meshFilename, script);
        npc.isItem = isItem;
        npcs.add(npc);
        
        // The NPC is returned and the scene loader sets it's parent to the zoneRoot, and gives it a name.

        return npc;
    }

    public Entity loadTrigger(Map<String, String>properties, TriggerType type) {

        String scriptFilename = properties.get("p_script") + ".txt";


        // Find the script for this NPC.
        Script script = null;
        if (scriptFilename != null && scriptFilename.length() > 0) {
            for (Script curScript : scripts) {
                if (curScript.name.equals(scriptFilename)) {
                    script = curScript;
                    break;
                }
            }
        }

        Trigger trigger = new Trigger(scene, script, type);

        String lock = properties.get("p_lock");
        if (lock != null && lock.length() != 0) {
            trigger.setLockEntityName(lock);
        }

        String state = properties.get("p_state");
        if (state != null && state.length() != 0) {
            trigger.requestState(state);
        }

        triggers.add(trigger);

        return trigger;
    }

    public Entity loadDoor(Map<String, String>properties, boolean frontDoor, boolean isTrigger) {
    
        Door door = new Door();

        String target = properties.get("p_target");
        if (target != null) {
            // New format.
            String[] targets = target.split("\\.");
            door.targetZone = targets[0];
            door.targetDoor = targets[1];
        } else {
            // Old format.
            door.targetZone = properties.get("p_target_zone");
            door.targetDoor = properties.get("p_target_object");
        }

        String headingString = properties.get("p_target_heading");
        if (headingString == null) {
            door.targetHeading = 0.0f;
        } else {
            door.targetHeading = Float.parseFloat(headingString);
        }
        
        door.isFront = frontDoor;
        door.isTrigger = isTrigger;

        //if (frontDoor) {
        //    door.setLayer(3);
        //}

        // Model the doors with the proper depth, and put them on the avatar layer
        // so the zone heading out transition can keep the door and avatar "above" the fade layer.
        
        if (!isTrigger) {
            door.setLayer(1);
        }
        
        String state = properties.get("p_state");
        if (state != null && state.length() != 0) {
            if (state.equals("closed")) {
                door.setState(DoorState.closed);
            }
        }

        doors.add(door);

        return door;
    }

    public Entity loadLadder(Map<String, String>properties) {
    
        Ladder ladder = new Ladder();

        ladders.add(ladder);

        ladder.flags.collidable = true;
        ladder.flags.platform_collision = true;


        return ladder;
    }

    public Entity createNpc(String name, String meshFilename, String scriptFilename, boolean isItem) {

        // Find the script for this NPC.
        Script script = null;
        if (scriptFilename != null && scriptFilename.length() > 0) {
            for (Script curScript : scripts) {
                if (curScript.name.equals(scriptFilename)) {
                    script = curScript;
                    break;
                }
            }
        }

        // If there was no script and this is an item, use the generic "take" script.
        if (isItem && script == null) {
            scriptFilename = "take.txt";

            if (scriptFilename != null && scriptFilename.length() > 0) {
                for (Script curScript : scripts) {
                    if (curScript.name.equals(scriptFilename)) {
                        script = curScript;
                        break;
                    }
                }
            }
        }
        
        Npc npc = new Npc(scene, this, new Vector3f(0, 0, 0), zoneName, meshFilename, script);
        npc.isItem = isItem;
        npcs.add(npc);
        
        // Have to manually set the name and parent.
        npc.setName(name);
        npc.setParent(zoneRoot);

        return npc;
    }

    public void addEntity(Entity entity) {

        // Handle post-load processing.

        // Mostly we don't care about entities that don't affect zone boundaries.
        if ((entity instanceof Door) ||
            (entity instanceof Ladder) ||
            (entity instanceof Npc)) {
                return;
        }

        // Try to run any script on_load labels, mostly for the purpose of restoring state data,
        // but maybe for fancy scripted events on entry?.
        if ((entity instanceof Trigger)) {

            processScript((Entity)avatar, entity, "on_load");
            return;
        }

        /*
        This will generate an Error.
        Make a zone metadata property that retains the existing bounds when loading the new zone.
        This will simplify the process of making shops / interiors, and maintaining them as the exterior
        zone changes shape!

        It will also make it so you can enter a shop from different zones and retain the bounding appropriate
        for the zone entered from instead of abruptly snapping to one specific zone's bounds. All win.

        Also make it so the shop interiors are not location specific, but when entering / loading them, place them
        in the world relative to the entry so they fit in the existing bounds properly.

        Maybe this leads to the conclusion that all Doors and Exits should automatically calculate the connecting geometry
        with a hard coded gap between same-plane Exits? Bonus: it would simplify zone metadata.
        */

       // if (!retainEntryBounds) {
            expandBounds(entity.getPosition(), entity.getBBox());
       // }
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

        if (!retainEntryBounds) {
            cameraBounds.max.x = avatarBounds.max.x;
            cameraBounds.min.x = avatarBounds.min.x;
            cameraBounds.max.y = avatarBounds.max.y;
            cameraBounds.min.y = avatarBounds.min.y;
        
            //float fov = 53.5f; // TODO: pull this from the camera.
            //float camz = 4.25f; // TODO: look this up too.

            //float halfFov = (float)java.lang.Math.toRadians(fov);

            // Shift this bounds to the right  to account for FOV.
            cameraBounds.min.x += 4.5;//halfFov * camz;
            cameraBounds.max.x -= 4.5;//halfFov * camz;
            }
    }

    public BoundingBox getAvatarBounds() {
        return avatarBounds;
    }

    public BoundingBox getCameraBounds() {
        return cameraBounds;
    }

    public boolean canEnterDoor(Entity entity, boolean useBack, boolean useFront) {

        for (int i = 0; i < doors.size(); i++) {

            Door door = doors.get(i);

            // If there was no interaction, skip doors that require it / are not triggers.
            if (!door.isTrigger) {
                if (!useFront && !useBack) {
                    continue;
                }

                if (door.isFront && !useFront) {
                    continue;
                }

                if (!door.isFront && !useBack) {
                    continue;
                }
            }

            // If this is a trigger door, and we entered through it, and we're no longer overlapping with it
            // clear it out, allowing re-entry.
            // But, if the requestedZoneName is also set, then we are in the process of leaving this zone and the door name
            // is for the next zone not this one.
    
            if (entitiesOverlap(entity, door, maxEnterX, maxEnterY)) {

                if (door.isTrigger) {
                    continue;
                }

                return true;
            }
        }

        return false;
    }

    public boolean enterDoor(Entity entity, boolean useBack, boolean useFront) {

        String targetZone = null;
        String targetDoor = null;
        float targetHeading = 0.0f;

        for (int i = 0; i < doors.size(); i++) {

            Door door = doors.get(i);

            // If there was no interaction, skip doors that require it / are not triggers.
            if (!door.isTrigger) {
                if (!useFront && !useBack) {
                    continue;
                }

                if (door.isFront && !useFront) {
                    continue;
                }

                if (!door.isFront && !useBack) {
                    continue;
                }
            }

            // If this is a trigger door, and we entered through it, and we're no longer overlapping with it
            // clear it out, allowing re-entry.
            // But, if the requestedZoneName is also set, then we are in the process of leaving this zone and the door name
            // is for the next zone not this one.
            boolean enteredFromHere = requestedZoneName.length() == 0 && door.getName().equals(requestedDoorName);

            if (entitiesOverlap(entity, door, maxEnterX, maxEnterY)) {

                if (enteredFromHere && door.isTrigger) {
                    continue;
                }

                if (door.getState() == DoorState.closed) {
                    door.setState(DoorState.openning);
                    continue;
                }

                targetZone = door.targetZone;
                targetDoor = door.targetDoor;
                targetHeading = door.targetHeading;
            
                entryDoorPosition.set(door.getPosition());

                break;
            } else {
                if (enteredFromHere) {
                    requestedDoorName = null;
                }
            }
        }

        if (targetZone != null) {
            
            requestZone(targetZone, targetDoor, targetHeading);

            return true;
        }

        return false;
    }

    public boolean checkUpInteraction(Entity entity) {

        // Take?
        if (canTake(entity))
            return true;

        if (canInteractWithTrigger(entity))
            return true;

        if (canTalk(entity))
            return true;

        if (canEnterDoor(entity, true, false))
            return true;

        return false;
    }

    public boolean checkDownInteraction(Entity entity) {

        if (canEnterDoor(entity, false, true))
            return true;

        return false;
    }

    public Entity interactAll(Entity entity, Entity other, boolean canTake) {

        Entity result = null;

        if (other != null) {
            return continueInteraction(entity, other);
        }

        // Take?
        if (canTake) {
            result = take(entity, other);
            if (result != null) 
                return result;
        }

        result = interactWithTrigger(entity, other);
        if (result != null) {
            return result;
        }

        result = talk(entity, other);
        if (result != null) 
            return result;

        return null;
    } 

    public boolean canContinueInteraction(Entity entity, Entity other) {
        if (other == null)
            return false;

        return true;
    }

    /*
    Move an in-progress interaction forward.
    */
    private Entity continueInteraction(Entity entity, Entity other)
    {
        if (!canContinueInteraction(entity, other))
            return null;

        Script script = null;

        if (other instanceof Trigger) {
            script = ((Trigger)other).getScript();    
        } else {
            script = ((Npc)other).getScript();
        }

        // TODO: Make sure this NPC is in this zone.

        if (entitiesNear(entity, other, maxTalkX, maxTalkY)) {
            processScript(entity, other, null);
            if (script.talking) {
                return other;
            } else {
                return null;
            }
        }

        endInteraction(entity, script, false);
        return null;
    }

    public Entity validateInteraction(Entity entity, Entity other) {

        if (other != null) {

            if (entitiesNear(entity, other, maxTalkX, maxTalkY)) {
                return other;
            }

            Script script = null;

            if (other instanceof Trigger) {
                script = ((Trigger)other).getScript();    
            } else {
                script = ((Npc)other).getScript();
            }

            interruptInteraction(entity, script);
            return null;
        }

        return null;
    }     

    public boolean canTake(Entity entity) {

        for (int i = 0; i < npcs.size(); i++) {

            Npc npc = npcs.get(i);

            if (!npc.getVisible()) {
                continue;
            }

            // This is probably already being carried.
            if (npc.getZone() == null) {
                continue;
            }

            // Can't interact with NPCs in other zones.
            if (!npc.getZone().equals(zoneName)) {
                continue;
            }

            if (!npc.isItem) {
                continue;
            }

            if (entitiesNear(entity, npc, maxTakeX, maxTakeY)) {
                return true;
            }
        }

        return false;
    }

    public Entity take(Entity entity, Entity other) {

        for (int i = 0; i < npcs.size(); i++) {

            Npc npc = npcs.get(i);

            if (!npc.getVisible()) {
                continue;
            }

            // This is probably already being carried.
            if (npc.getZone() == null) {
                continue;
            }

            // Can't interact with NPCs in other zones.
            if (!npc.getZone().equals(zoneName)) {
                continue;
            }

            if (!npc.isItem) {
                continue;
            }

            if (entitiesNear(entity, npc, maxTakeX, maxTakeY)) {
            
                processScript(entity, npc, null);
                // The caller shouldn't hold on to this as an interaction.
                return npc;
            }
        }

        return null;
    }

    public boolean canInteractWithTrigger(Entity entity) {

        // Interact? (with Triggers)
        for (int i = 0; i < triggers.size(); i++) {

            Trigger trigger = triggers.get(i);

            if (entitiesOverlap(entity, trigger, maxTriggerX, maxTriggerY)) {
                return true;
            }
        }

        return false;
    }

    public Entity interactWithTrigger(Entity entity, Entity other) {

        // Interact? (with Triggers)
        for (int i = 0; i < triggers.size(); i++) {

            Trigger trigger = triggers.get(i);

            if (entitiesOverlap(entity, trigger, maxTriggerX, maxTriggerY)) {
            
                if (processScript(entity, trigger, null)) {
                    return trigger;
                }

                // Tried to interact but there was no script. Keep looking.
            }
        }

        return null;
    }

    boolean canTalk(Entity entity) {

        // Talk?
        for (int i = 0; i < npcs.size(); i++) {

            Npc npc = npcs.get(i);
            if (npc.isItem) {
                continue;
            }

            // Can't interact with NPCs in other zones.
            if (!npc.getZone().equals(zoneName)) {
                continue;
            }

            if (entitiesNear(entity, npc, maxTalkX, maxTalkY)) {
                return true;

            }
        }

        return false;
    }

    Entity talk(Entity entity, Entity other) {

        // Talk?
        for (int i = 0; i < npcs.size(); i++) {

            Npc npc = npcs.get(i);
            if (npc.isItem) {
                continue;
            }

            // Can't interact with NPCs in other zones.
            if (!npc.getZone().equals(zoneName)) {
                continue;
            }

            if (entitiesNear(entity, npc, maxTalkX, maxTalkY)) {
            
                processScript(entity, npc, null);
                if (npc.getScript().talking) {
                    return npc;
                }
            }
        }

        return null;
    }

    public Entity climb(Entity entity) {

        for (int i = 0; i < ladders.size(); i++) {

            Ladder ladder = ladders.get(i);
 
            // Over ladder.
            if (entitiesOverlap(entity, ladder, maxClimbX, maxClimbY)) {
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
        }

        return false;
    }

    public boolean entitiesNear(Entity entityA, Entity entityB, float xMinDistance, float yMinDistance) {

        Vector3f entityAPos = entityA.getPosition();
        //BoundingBox entityABox = entityA.getBBox();

        Vector3f entityBPos = entityB.getPosition();
        //BoundingBox entityBBox = entityB.getBBox();

        Vector3f xVec = new Vector3f(entityBPos);
        float dist;

        xVec.z = 0.0f;
        xVec.sub(entityAPos);
        xVec.y = 0.0f;
    
        dist = xVec.length();

        if (dist > xMinDistance) {
            return false;
        }

        Vector3f yVec = new Vector3f(entityBPos);
        yVec.z = 0.0f;
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
        return newZoneMapOffset;
    }

    public float getMapHeading() {
        return zoneMapHeading;
    }

    public boolean processScript(Entity entity, Entity other, String label) {
        
        Script script = null;

        if (other instanceof Trigger) {
            script = ((Trigger)other).getScript();    
        } else {
            script = ((Npc)other).getScript();
        }

        if (script == null) {
            return false;
        }

        // Goto label if specified. If the script doesn't have the label, don't process.
        // This is for running things like on_load.
        if (label != null) {
            if (!script.gotoLabel(label)) {
                return false;
            }
        }

        // The target starts as other (the script bearer). But "star" may change it to another entity
        // in order to manipulate it "remotely". 
        Entity target = other;

        //int prevCommand = script.nextCommand;

        outer:
        while (script.nextCommand < script.numCommands) {

            String command = script.commands.get(script.nextCommand);
            String[] args = command.split(":");

            script.nextCommand++;

            switch (args[0]) {

                // Spawn an item and take it.
                case "ainv": {

                    Npc item = (Npc)createNpc(args[1], args[1], args[1] + ".txt", true);
                    if (item == null) {
                        break;
                    }

                    //item.setVisible(false);
                    Avatar avatar = (Avatar)entity;
                    avatar.take(item);

                    break;
                }

                // Check if the avatar's inventory contains an item.
                case "cinv": {

                    String[] inventoryNames = ((Avatar)entity).inventory.getInventoryNames();

                    for (int i = 0; i < inventoryNames.length; i++) {
                        if (inventoryNames[i].equals(args[1])) {
                            // Found it.
                            // Inline goto:
                            script.gotoLabel(args[2]);
                            //script.nextCommand = Integer.parseInt(args[2]);
                            break;
                        }
                    }

                    break;
                }

                // Remove item from avatar's inventory.
                case "rinv": {

                    ((Avatar)entity).inventory.removeInventoryItem(args[1], this);

                    break;
                }

                // Check if the avatar is holding a specific item.
                case "chld": {

                    Npc heldItem = ((Avatar)entity).inventory.getHeldItem();
                    if (heldItem != null){
                        if (heldItem.getName().equals(args[1])) {
                            // Found it.
                            // Inline goto:
                            script.gotoLabel(args[2]);
                            //script.nextCommand = Integer.parseInt(args[2]);
                            break;
                        }
                    }

                    break;
                }

                // Add a progress key.
                case "akey": {

                    ((Avatar)entity).inventory.addKey(args[1]);

                    break;
                }

                // Check a progress key.
                case "ckey": {
                    
                    List<String> keys = ((Avatar)entity).inventory.getKeys();

                    for (String key : keys) {

                        if (key.equals(args[1])) {
                            // Found it.
                            // Inline goto:
                            script.gotoLabel(args[2]);
                            //script.nextCommand = Integer.parseInt(args[2]);
                            break;
                        }
                    }

                    break;
                }

                // Set destination.
                case "dest": {

                    // Find and set the specified destination.
                    Entity destEntity = scene.findEntity(args[1]);
                    ((Npc)target).setDestinationEntity(destEntity);

                    break;
                }

                // Remove collision.
                case "rcol": {
                    
                    if (target != null) {
                        target.flags.collidable = false;
                    }
                    
                    /*
                    else {
                        // Find the named entity.
                        // Remove collision.
                        for (int i = 0; i < triggers.size(); i++) {

                            Trigger trigger = triggers.get(i);
                            if (trigger.getName().equals(args[1])) {

                                trigger.flags.collidable = false;
                                break;
                            }
                        }
                    }
                    */

                    break;
                }

                // Set the target for following commands replacing other until the end of call.
                case "star": {
                    
                    if (args.length == 1) {
                        // Empty, reset target to other:
                        target = other;
                    } else {
                        // TODO: ??? Note that Blender may have dumped a .001 etc on to the entity, and the loader
                        // only strips that off for certain types - mostly non level geometry. So you might not find those.
                        Entity result = scene.findEntity(args[1]);
                        if (result != null) {
                            target = result;
                        }
                    }

                    break;
                }

                // Take an existing item from the zone.
                case "take": {

                    Avatar avatar = (Avatar)entity;
                    avatar.take((Npc)target);

                    break;
                }

                // Start or continue dialog.
                case "talk":
                    script.talking = true;
                    hud.setDialogText(args[1]);
            
                    // Set this every frame in case the camera moves.
                    hud.showDialog(target, true);
    
                    break outer;

                // End interaction / Script processing.
                case "eint":
                    endInteraction(entity, script, false);
                    break outer;

                case "goto":
                    script.gotoLabel(args[1]);
                    //script.nextCommand = Integer.parseInt(args[1]);
                    break;
                  
                case "zone":
                    requestZone(new String(args[1]), new String(args[2]), 0.0f);
                    break;

                // Check the entity state string for equality.
                case "csta":
                    if (target.getStateName().equals(args[1])) {
                        script.gotoLabel(args[2]);
                    }

                    break;

                case "rsta":
                    target.requestState(args[1]);
                    break;
                
                default:
                    // Most likely a label. Skip it.
                    break;
            }
        }

    //    if (prevCommand == script.nextCommand) {
    //        // We were at the end. Close any active dialog.
    //        endInteraction(entity, script, true);         
    //    }

        return true;
    }

    public void interruptInteraction(Entity entity, Script script) {
        
        if (script.talking) {
            
            hud.showDialog(null, false);

            script.nextCommand--;
        }
    }
    
    public void endInteraction(Entity entity, Script script, boolean reset) {
        
        script.talking = false;
        
        hud.showDialog(null, false);

        if (reset) {
            script.nextCommand = 0;
        }
    }  
}