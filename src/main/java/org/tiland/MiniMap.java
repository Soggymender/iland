package org.tiland;

import org.joml.Vector3f;

import org.engine.core.BoundingBox;
import org.engine.core.Rect;
import org.engine.renderer.*;
import org.engine.scene.*;
import org.engine.sketch.*;

public class MiniMap {
 
    private Scene scene;
    private Entity target;

    private Camera camera;

    private Hud hud;
 
    private SketchElement locationSketch;
    private SketchElement zoneSketch;  

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
    }

    public Scene getScene() {
        return scene;
    }

    public Camera getCamera() {
        return camera;
    }

    public void addZone(BoundingBox zoneBounds) {

        // This is just test code. Clear the sketch so as we move back and forth between the two test
        // zones the sketch line don't keep stacking.

        // There is no depth yet.
        zoneSketch.clear();

        Vector3f pos1 = new Vector3f();
        Vector3f pos2 = new Vector3f();
        Vector3f pos3 = new Vector3f();
        Vector3f pos4 = new Vector3f();

        pos1.x = zoneBounds.min.x;
        pos1.y = zoneBounds.min.y;
        pos1.z = 0;

        pos2.x = zoneBounds.max.x;
        pos2.y = zoneBounds.min.y;
        pos2.z = 0;
        
        
        pos3.x = zoneBounds.max.x;
        pos3.y = zoneBounds.max.y;
        pos3.z = 0;

        
        pos4.x = zoneBounds.min.x;
        pos4.y = zoneBounds.max.y;
        pos4.z = 0;

        zoneSketch.addLines(Color.WHITE, pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
        zoneSketch.addLines(Color.WHITE, pos2.x, pos2.y, pos2.z, pos3.x, pos3.y, pos3.z);
        zoneSketch.addLines(Color.WHITE, pos3.x, pos3.y, pos3.z, pos4.x, pos4.y, pos4.z);
        zoneSketch.addLines(Color.WHITE, pos4.x, pos4.y, pos4.z, pos1.x, pos1.y, pos1.z);
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

        pos1.x = location.x + -0.25f;
        pos1.y = location.y + -0.25f;
        pos1.z = 0;

        pos2.x = location.x + 0.25f;
        pos2.y = location.y + -0.25f;
        pos2.z = 0;
                
        pos3.x = location.x + 0.25f;
        pos3.y = location.y + 0.25f;
        pos3.z = 0;

        pos4.x = location.x + -0.25f;
        pos4.y = location.y + 0.25f;
        pos4.z = 0;

        locationSketch.clear();
        locationSketch.addLines(Color.WHITE, pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
        locationSketch.addLines(Color.WHITE, pos2.x, pos2.y, pos2.z, pos3.x, pos3.y, pos3.z);
        locationSketch.addLines(Color.WHITE, pos3.x, pos3.y, pos3.z, pos4.x, pos4.y, pos4.z);
        locationSketch.addLines(Color.WHITE, pos4.x, pos4.y, pos4.z, pos1.x, pos1.y, pos1.z);
    }

    public void update(float interval) {

        Vector3f targetPos = new Vector3f(target.getPosition());
        targetPos.z = camera.getPosition().z;

        camera.setPosition(targetPos);

        updateLocationSketch();

        // TODO: Only set the viewport on startup and resize. But the panelneeds to update once for the screen rect to be valid.
        Rect hudPanel = hud.getMapPanelRect();

        camera.setViewport(hudPanel.xMin, hudPanel.yMax, hudPanel.xMax - hudPanel.xMin, hudPanel.yMax - hudPanel.yMin); 
    }
}