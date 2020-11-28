package org.tiland;

import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;
import org.joml.*;

public class Npc extends Sprite {

    private String homeName;
    private String zoneName;

    private Script script = null;

    public boolean isItem = false;

    public Npc(Scene scene, Vector3f position, String home, String meshFilename, Script script) {

        super(scene);

        moveVec = new Vector2f();
        flags.dynamic = false;
        flags.collidable = false;

        this.homeName = home;
        this.zoneName = home;

        initialize(scene, position, meshFilename, script);
    }

    public void initialize(Scene scene, Vector3f position, String meshFilename, Script script) {

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

        this.script = script;
    }

    @Override
    public void update(float interval) {

    


        super.update(interval);

        /*
        if (flags.dynamic) {
            if (velocity.y == 0.0f) {
                flags.dynamic = false;
                flags.collidable = false;
            }
        }
        */
    }

    public String getHome() {
        return homeName;

    }
    
    public Script getScript() {
        return script;
    }

    public String getZone() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }
}
