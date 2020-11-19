package org.tiland;

import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;
import org.joml.*;

public class Npc extends Sprite {

    private int dialogLine = -1;
    private int numDialogLines = 1;

    public Npc(Scene scene, Vector3f position, String meshFilename) {

        super(scene);

        moveVec = new Vector2f();
        flags.dynamic = false;
        flags.collidable = false;

        initialize(scene, position, meshFilename);
    }

    public void initialize(Scene scene, Vector3f position, String meshFilename) {

        setPosition(position);

        Mesh[] npcMesh;

        try {
            npcMesh = SceneLoader.loadMesh("src/main/resources/tiland/models/" + meshFilename + ".fbx",
                    "src/main/resources/tiland/textures/");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Texture texture = npcMesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 1.0f);
        npcMesh[0].setMaterial(material);

        setMeshes(npcMesh);
    }

    @Override
    public void update(float interval) {

        super.update(interval);
    }

    public void interact(Hud hud) {
        
        if (dialogLine == -1) {
            hud.showDialog(true);
            hud.setDialogText("water is becoming dangerously scarce. we've sent all from the diviner's guild, and all have failed. our survival is in your hands.");
        }

        if (dialogLine == numDialogLines - 1) {
            hud.showDialog(false);
            dialogLine = -1;
            return;
        }

        dialogLine++;
    }

    public void endInteraction(Hud hud) {
        hud.showDialog(false);
        dialogLine = -1;
    }
}
