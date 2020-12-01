package org.tiland;

import org.engine.core.BoundingBox;
import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Entity;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;
import org.joml.*;

public class Npc extends Sprite {

    private String homeName;
    private String zoneName;

    private Script script = null;

    private Entity destEntity = null;

    public boolean isItem = false;

    private Zone zone = null;

    public Npc(Scene scene, Zone zone, Vector3f position, String home, String meshFilename, Script script) {

        super(scene);

        this.zone = zone;

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

    public void setDestinationEntity(Entity destEntity) {
        this.destEntity = destEntity;

        flags.dynamic = true;
        flags.collidable = true;
    }

    @Override
    public void update(float interval) {

        moveVec.zero();

        if (destEntity != null) {

            // TODO: Shut off dynamic and collidable once the destination is reached.

            // Set moveVec to approach the destination object.
            Vector3f posDelta = new Vector3f(destEntity.getPosition());
            posDelta.sub(position);

            moveVec.x = java.lang.Math.max(-1, java.lang.Math.min(posDelta.x,1));
            moveVec.y = 0.0f;
        }

        super.update(interval);

        BoundingBox bounds = zone.getAvatarBounds();

        // TODO: There's a bug here because frameVelocity will show a larger value than what was effectively applied.
        // But it should only matter if a collision happens that needs to be resolved while trying to pass the boundary.
        if (position.x + bBox.min.x < bounds.min.x) {
            position.x = bounds.min.x - bBox.min.x;
        }

        if (position.x +bBox.max.x > bounds.max.x) {
            position.x = bounds.max.x - bBox.max.x;
        }

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
