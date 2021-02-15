package org.niland;

import org.joml.Vector3f;

import org.engine.input.*;

import org.engine.renderer.Camera;
import org.engine.renderer.Color;
import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;
import org.engine.sketch.*;

import static org.lwjgl.glfw.GLFW.*;

import org.engine.Terrain;

import org.engine.core.Math;

public class Probe extends Entity {

    private Vector3f moveDir;

    private float speed = 3.5f;

    private Vector3f gridPos = new Vector3f();

    private SketchElement moveDirSketch;
    private SketchElement xIndicatorSketch;
    private SketchElement gridSketch;

    public Probe(Scene scene) throws Exception {

        moveDir = new Vector3f(0.0f, 0.0f, 0.0f);

        moveDirSketch = new SketchElement(null);
        xIndicatorSketch = new SketchElement(null);
        gridSketch = new SketchElement(null);
        scene.addEntity(moveDirSketch);
        scene.addEntity(xIndicatorSketch);
        scene.addEntity(gridSketch);

        setVisible(false);
    }

    public void initialize() throws Exception {

        // Avatar placeholder.
        Mesh[] avatarMesh = SceneLoader.loadMesh("src/main/resources/iland/models/human01.fbx", "src/main/resources/iland/models/");
        Texture texture = avatarMesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 1.0f);
        avatarMesh[0].setMaterial(material);

        setMeshes(avatarMesh);
    }

    public void input(Input input) {

        moveDir.set(0, 0, 0);

        Keyboard keyboard = input.getKeyboard();

        if (keyboard.keyDown(GLFW_KEY_W) ) {
            moveDir.z = -1;
        }

        if (keyboard.keyDown(GLFW_KEY_S) ) {
            moveDir.z = 1;
        }

        if (keyboard.keyDown(GLFW_KEY_A)) {
            moveDir.x = -1;
        }

        if (keyboard.keyDown(GLFW_KEY_D)) {
            moveDir.x = 1;
        }

        if (moveDir.length() > 0.0f) {
            moveDir.normalize();
        }

        
    }

    public void update(float interval, Camera camera, Terrain terrain) {

        // Move

        float offsetX = moveDir.x * speed * interval;
    //   float offsetY = moveDir.y * speed * interval;
        float offsetZ = moveDir.z * speed * interval;

        Vector3f cameraRotation = camera.getRotation();

        moveDir.rotateY(cameraRotation.y);

        position.x += moveDir.x * speed * interval;
        position.z += moveDir.z * speed * interval;






        // Turn

        if (moveDir.length() > 0.0f) {
            //moveDir.rotateY(-cameraRotation.y);



            float angY = Math.forward.angleSigned(moveDir, Math.up);

            rotation.y = -angY;
        }
 
        Vector3f forward = new Vector3f();
        forward.set(Math.forward);

        forward.rotateY(-rotation.y);

 
        moveDirSketch.clear();
        xIndicatorSketch.clear();
        
    // Grid.
    gridPos.x = (float)java.lang.Math.floor(position.x);
    gridPos.y = -0.15f;
    gridPos.z = (float)java.lang.Math.floor(position.z);

    gridSketch.clear();
    int lines = 10;

        Color color = new Color();
        Color whiteComp = new Color();

        for (int z = 0; z <= lines; z++) {

            float half = (float)lines * 0.5f;
            float width = (float)lines;

            float middle = (float)lines / 2.0f;
            float colorPos = (java.lang.Math.abs((float)z - middle)) / middle; 

            color.set(Color.LIGHTGREY);
            color.mul(1.0f - colorPos);
            
            whiteComp.set(Color.WHITE);
            whiteComp.mul(colorPos);

            color.add(whiteComp);

            gridSketch.addLines(color, gridPos.x + (float)z - half, 0.0f, gridPos.z + 0.0f - half, gridPos.x + (float)z - half, 0.0f, gridPos.z + width - half);
         
            gridSketch.addLines(color, gridPos.x + 0.0f - half, 0.0f, gridPos.z + (float)z - half, gridPos.x + width - half, 0.0f, gridPos.z + (float)z - half);
        }

        // Add ring.
        int numSegments = 20;
        float radius = 0.5f;

        float angle = 0.0f;
        float segmentAngle = 360.0f / (float)numSegments;

        Vector3f prePos = new Vector3f();
        Vector3f curPos = new Vector3f();

        for (int i = 0; i < numSegments + 1; i++) {

            float rads = (float)Math.toRadians(angle);
            curPos.x = radius * (float)Math.cos(rads);
            curPos.y = 0.0f;
            curPos.z = radius * (float)Math.sin(rads);

            curPos.add(position);

            angle += segmentAngle;

            if (i > 0) {
                moveDirSketch.addLines(Color.BLACK, prePos.x, prePos.y, prePos.z, curPos.x, curPos.y, curPos.z);                
            }

            prePos.set(curPos);
        }


        // Add probe.
        Vector3f probeStart = new Vector3f();
        probeStart.set(forward);
        probeStart.mul(radius);

        moveDirSketch.addLines(Color.BLACK,  position.x + probeStart.x, position.y, position.z + probeStart.z, position.x, position.y + radius, position.z);

        // X indicator.
        Vector3f xStart = new Vector3f();
        xStart.set(Math.right);
        xStart.mul(radius);

        xIndicatorSketch.addLines(Color.RED, position.x, position.y, position.z, position.x + xStart.x, position.y, position.z + xStart.z);

    
    }

    public Entity getEntity() {

        return this;
    }
}
