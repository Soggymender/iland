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
    private Vector3f moveDirRaw;

    private float speed = 3.5f;

    private Vector3f gridPos = new Vector3f();

    private SketchElement moveDirSketch;
    private SketchElement xIndicatorSketch;
    private SketchElement gridSketch;

    public Probe(Scene scene) throws Exception {

        moveDir = new Vector3f(0.0f, 0.0f, 0.0f);
        moveDirRaw = new Vector3f(0.0f, 0.0f, 0.0f);

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
        moveDirRaw.set(0, 0, 0);

        Mouse mouse = input.getMouse();
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
            moveDirRaw.set(moveDir);
        } else {
            if (!mouse.middleButtonPressed()) {

                moveDirRaw.x = mouse.getDisplayVec().x;
                moveDirRaw.z = mouse.getDisplayVec().y;

                if (moveDirRaw.length() >= 1.0f) {
                    

                    moveDir.set(moveDirRaw);
                    moveDir.mul(0.5f);
                }
            } 
        }
    }

    public void update(float interval, Camera camera, Terrain terrain) {

        // Move
        if (moveDir.length() > 0.0f) {
            //moveDir.rotateY(-cameraRotation.y);

            Vector3f cameraRotation = camera.getRotation();

            moveDir.rotateY(cameraRotation.y);

            position.x += moveDir.x * speed * interval;
            position.z += moveDir.z * speed * interval;






        // Turn


            moveDirRaw = moveDirRaw.normalize();

            float angY = Math.forward.angleSigned(moveDirRaw, Math.up);

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
    int lines = 20;

        Color color = new Color();
        Color whiteComp = new Color();

        Vector3f start = new Vector3f();
        Vector3f middle = new Vector3f();
        Vector3f end = new Vector3f();

        for (int z = 0; z <= lines; z++) {

            float half = (float)lines * 0.5f;
            float width = (float)lines;

            float offset;

            offset = position.z - gridPos.z;
            float colorPos = (java.lang.Math.abs((float)z - half)) / half; 

            color.set(Color.LIGHTGREY);
            color.mul(1.0f - colorPos);
            
            whiteComp.set(Color.WHITE);
            whiteComp.mul(colorPos);

            color.add(whiteComp);

            start.set(gridPos.x + (float)z - half, gridPos.y, gridPos.z + 0.0f - half);
            end.set(gridPos.x + (float)z - half, gridPos.y, gridPos.z + width - half);
            middle.set(end);
            middle.sub(start);
            middle.mul(0.5f);
            middle.add(start);

            gridSketch.addLines(Color.WHITE, color, start.x, start.y, start.z, middle.x, middle.y, middle.z);
            gridSketch.addLines(Color.WHITE, color, end.x, end.y, end.z, middle.x, middle.y, middle.z);
/*
            float colorPos = (java.lang.Math.abs((float)z - half)) / half; 

            color.set(Color.LIGHTGREY);
            color.mul(1.0f - colorPos);
            
            whiteComp.set(Color.WHITE);
            whiteComp.mul(colorPos);

            color.add(whiteComp);
*/
            start.set(gridPos.x + 0.0f - half, gridPos.y, gridPos.z + (float)z - half);
            end.set(gridPos.x + width - half, gridPos.y, gridPos.z + (float)z - half);
            middle.set(end);
            middle.sub(start);
            middle.mul(0.5f);
            middle.add(start);

            gridSketch.addLines(Color.WHITE, color, start.x, start.y, start.z, middle.x, middle.y, middle.z);
            gridSketch.addLines(Color.WHITE, color, end.x, end.y, end.z, middle.x, middle.y, middle.z);
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
