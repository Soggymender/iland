package org.tiland;

import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;
import org.joml.*;

public class Npc extends Sprite {

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
}
