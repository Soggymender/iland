package org.tiland;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;

import org.engine.core.BoundingBox;
import org.engine.core.Math;
import org.engine.core.Rect;
import org.engine.renderer.*;
import org.engine.scene.*;
import org.engine.sketch.*;

final class MapZone {

    public Vector3f offset = new Vector3f();
    public Vector3f origin = new Vector3f();
    public float heading;
}

public class MiniMap {
 
    private Scene scene;
    private Entity target;

    private Camera camera;
    private Vector3f offset;
    private Vector3f origin;
    private float heading;

    private Hud hud;
 
    private SketchElement locationSketch;
    private SketchElement zoneSketch;  

    private Map<String, MapZone> mapZones = null;

    public MiniMap(Scene scene, Entity target, Hud hud, Window window) throws Exception {

        this.scene = scene;
        this.target = target;

        this.hud = hud;

        camera = new Camera();
                
        camera.setPosition(0,2.5f, 30.0f);

    
        zoneSketch = new SketchElement(null);
        scene.addEntity(zoneSketch);

        locationSketch = new SketchElement(null);
        scene.addEntity(locationSketch);

        zoneSketch.clear();

        mapZones = new HashMap<>();
    }

    public Scene getScene() {
        return scene;
    }

    public Camera getCamera() {
        return camera;
    }

    public void addZone(String name, Vector3f offset, float heading, BoundingBox zoneBounds) {

        MapZone mapZone = mapZones.get(name);
        if (mapZone != null) {
            return;
        }

        mapZone = new MapZone();
        mapZones.put(name, mapZone);

        mapZone.offset.set(offset);
        mapZone.origin.set(zoneBounds.min);
        mapZone.heading = heading;

        Vector3f pos1 = new Vector3f();
        Vector3f pos2 = new Vector3f();
        Vector3f pos3 = new Vector3f();
        Vector3f pos4 = new Vector3f();

        float width = zoneBounds.max.x - zoneBounds.min.x;
        float height = zoneBounds.max.y - zoneBounds.min.y;

        pos1.x = 0;
        pos1.y = 0;
        pos1.z = 0.0f;

        pos2.x = width;
        pos2.y = 0;
        pos2.z = 0.0f;
        
        pos3.x = width;
        pos3.y = height;
        pos3.z = 0.0f;

        pos4.x = 0;
        pos4.y = height;
        pos4.z = 0.0f;

        float rad = Math.toRadians(heading);

        pos1.rotateY(rad);
        pos2.rotateY(rad);
        pos3.rotateY(rad);
        pos4.rotateY(rad);

        pos1.x += zoneBounds.min.x + offset.x;
        pos1.y += zoneBounds.min.y + offset.y;
        pos1.z += offset.z;

        pos2.x += zoneBounds.min.x + offset.x;
        pos2.y += zoneBounds.min.y + offset.y;
        pos2.z += offset.z;
        
        
        pos3.x += zoneBounds.min.x + offset.x;
        pos3.y += zoneBounds.min.y + offset.y;
        pos3.z += offset.z;

        
        pos4.x += zoneBounds.min.x + offset.x;
        pos4.y += zoneBounds.min.y + offset.y;
        pos4.z += offset.z;

        zoneSketch.addLines(Color.WHITE, pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
        zoneSketch.addLines(Color.WHITE, pos2.x, pos2.y, pos2.z, pos3.x, pos3.y, pos3.z);
        zoneSketch.addLines(Color.WHITE, pos3.x, pos3.y, pos3.z, pos4.x, pos4.y, pos4.z);
        zoneSketch.addLines(Color.WHITE, pos4.x, pos4.y, pos4.z, pos1.x, pos1.y, pos1.z);
    }

    public void enterZone(String name) {

        MapZone mapZone = mapZones.get(name);
        if (mapZone == null) {
            return;
        }

        offset = mapZone.offset;
        origin = mapZone.origin;
        heading = mapZone.heading;
    }
    
    private void updateLocationSketch() {

        // Use the main game camera's location to represent the avatar's location. This is nicer because
        // the camera can't go right up to the zone bounds, and doesn't bounce when jumping. It's more of an 
        // "average" location. A nicer stable presentation.

        Vector3f pos1 = new Vector3f();
        Vector3f pos2 = new Vector3f();
        Vector3f pos3 = new Vector3f();
        Vector3f pos4 = new Vector3f();

        Vector3f location = new Vector3f(target.getPosition());

        pos1.x = location.x - origin.x + -0.25f;
        pos1.y = location.y - origin.y + -0.25f;
        pos1.z = -origin.z;

        pos2.x = location.x - origin.x + 0.25f;
        pos2.y = location.y - origin.y + -0.25f;
        pos2.z = -origin.z;
                
        pos3.x = location.x - origin.x + 0.25f;
        pos3.y = location.y - origin.y + 0.25f;
        pos3.z = -origin.z;

        pos4.x = location.x - origin.x + -0.25f;
        pos4.y = location.y - origin.y + 0.25f;
        pos4.z = -origin.z;

        float rad = Math.toRadians(heading);

        pos1.rotateY(rad);
        pos2.rotateY(rad);
        pos3.rotateY(rad);
        pos4.rotateY(rad);

        pos1.x += origin.x + offset.x;
        pos1.y += origin.y + offset.y;
        pos1.z += origin.z + offset.z;

        pos2.x += origin.x + offset.x;
        pos2.y += origin.y + offset.y;
        pos2.z += origin.z + offset.z;
                
        pos3.x += origin.x + offset.x;
        pos3.y += origin.y + offset.y;
        pos3.z += origin.z + offset.z;

        pos4.x += origin.x + offset.x;
        pos4.y += origin.y + offset.y;
        pos4.z += origin.z + offset.z;

        locationSketch.clear();
        locationSketch.addLines(Color.WHITE, pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
        locationSketch.addLines(Color.WHITE, pos2.x, pos2.y, pos2.z, pos3.x, pos3.y, pos3.z);
        locationSketch.addLines(Color.WHITE, pos3.x, pos3.y, pos3.z, pos4.x, pos4.y, pos4.z);
        locationSketch.addLines(Color.WHITE, pos4.x, pos4.y, pos4.z, pos1.x, pos1.y, pos1.z);
    }

    public void update(float interval) {

        Vector3f targetPos = new Vector3f(target.getPosition());

        // "Zoom" out a good ways for that sweet minimap feel.
        targetPos.z = 30;

        float rad = Math.toRadians(heading);

        targetPos.sub(origin);
        targetPos.rotateY(rad);
        targetPos.add(origin);
        targetPos.add(offset);

        camera.setPosition(targetPos);
        camera.setRotation(0, rad, 0);



        updateLocationSketch();

        // TODO: Only set the viewport on startup and resize. But the panelneeds to update once for the screen rect to be valid.
        Rect hudPanel = hud.getMapPanelRect();

        camera.setViewport(hudPanel.xMin, hudPanel.yMax, hudPanel.xMax - hudPanel.xMin, hudPanel.yMax - hudPanel.yMin); 
    }
}