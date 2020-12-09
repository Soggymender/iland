package org.tiland;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import org.engine.core.BoundingBox;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;

import org.tiland.Trigger.*;

public class Zone {

    String zoneName;
    Vector3f zoneOffset = new Vector3f();
    float zoneHeading;

    String requestedZoneName = new String();
    String requestedDoorName = new String();

    Vector3f avatarStart;

    public ArrayList<Script> scripts = new ArrayList<Script>();

    public Avatar avatar;
    public List<Npc> npcs;

    public List<Door> doors;
    public List<Ladder> ladders;
    public List<Trigger> triggers;

    BoundingBox avatarBounds;
    BoundingBox cameraBounds;

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
            scene.removeEntity(zoneRoot, false);
        }

        reset();

        zoneName = requestedZoneName;
        load(requestedZoneName);

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
    }

    public void reset() {

        avatarStart.zero();

        //npcs.clear();

        doors.clear();
        ladders.clear();

        triggers.clear();

        avatarBounds.reset();
        cameraBounds.reset();

        avatarBounds.min.x = 9999.0f;
        avatarBounds.min.y = 9999.0f;
        avatarBounds.max.x = -9999.0f;
        avatarBounds.max.y = -9999.0f;
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

    public Entity loadNpc(String name, Map<String, String>properties, boolean isItem) {

        // Was this NPC created during a previous load?
        for (int i = 0; i < npcs.size(); i++) {

            Npc npc = npcs.get(i);

            String npcName = npc.getName();
            String npcHome = npc.getHome();
            String npcZone = npc.getZone();

            if (npcName != null && npcHome != null)  {
                if (npcName.equals(name) && npcHome.equals(zoneName)) {

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

        Trigger trigger = new Trigger(script, type);

        triggers.add(trigger);

        return trigger;
    }

    public Entity loadDoor(Map<String, String>properties, boolean frontDoor, boolean isTrigger) {
    
        Door door = new Door();

        door.targetZone = properties.get("p_target_zone");
        door.targetDoor = properties.get("p_target_object");
        door.isFront = frontDoor;
        door.isTrigger = isTrigger;

        String state = properties.get("p_state");
        if (state != null && state.length() != 0) {
            if (state.equals("closed")) {
                door.setState(DoorState.CLOSED);
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

    public boolean enterDoor(Entity entity, boolean useBack, boolean useFront) {

        String targetZone = null;
        String targetDoor = null;

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
            boolean enteredFromHere = door.getName().equals(requestedDoorName);

            if (entitiesOverlap(entity, door, 0.5f, 0.95f)) {

                if (enteredFromHere && door.isTrigger) {
                    continue;
                }

                if (door.getState() == DoorState.CLOSED) {
                    door.setState(DoorState.OPENNING);
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

    public Entity interact(Entity entity, Entity other, boolean canTake) {

        // If there's an ongoing interaction make sure it is still valid.
        if (other != null) {

            Script script = null;

            if (other instanceof Trigger) {
                script = ((Trigger)other).getScript();    
            } else {
                script = ((Npc)other).getScript();
            }

            // TODO: Make sure this NPC is in this zone.

            if (entitiesNear(entity, other, 1.0f, 1.0f)) {
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

        // Take?
        if (canTake) {
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

                if (entitiesNear(entity, npc, 0.25f, 0.5f)) {
                
                    processScript(entity, npc, null);
                    // The caller shouldn't hold on to this as an interaction.
                    return npc;
                }
            }
        }

        // Interact? (with Triggers)
        for (int i = 0; i < triggers.size(); i++) {

            Trigger trigger = triggers.get(i);

            if (entitiesOverlap(entity, trigger, 0.25f, 0.25f)) {
            
                if (processScript(entity, trigger, null)) {
                    return trigger;
                }

                // Tried to interact but there was no script. Keep looking.
            }
        }

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


            if (entitiesNear(entity, npc, 1.0f, 1.0f)) {
            
                processScript(entity, npc, null);
                if (npc.getScript().talking) {
                    return npc;
                }
            }
        }

        return null;
    } 
    
    public Entity validateInteraction(Entity entity, Entity other) {

        if (other != null) {

            if (entitiesNear(entity, other, 1.0f, 1.0f)) {
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

    public Entity onLadder(Entity entity) {

        for (int i = 0; i < ladders.size(); i++) {

            Ladder ladder = ladders.get(i);
 
            // Over ladder.
            if (entitiesOverlap(entity, ladder, 0.5f, 0.01f)) {
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

        int prevCommand = script.nextCommand;

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
                    hud.showDialog(true);
                    hud.setDialogText(args[1]);
    
                    break outer;

                // End interaction / Script processing.
                case "eint":
                    endInteraction(entity, script, false);
                    break outer;

                case "goto":
                    script.gotoLabel(args[1]);
                    //script.nextCommand = Integer.parseInt(args[1]);
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
            
            hud.showDialog(false);

            script.nextCommand--;
        }
    }
    
    public void endInteraction(Entity entity, Script script, boolean reset) {
        
        script.talking = false;
        
        hud.showDialog(false);

        if (reset) {
            script.nextCommand = 0;
        }
    }    
}