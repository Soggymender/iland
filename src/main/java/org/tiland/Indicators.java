package org.tiland;

import org.joml.Vector3f;
import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;
import org.tiland.Sprite;

public class Indicators {
   
    Sprite upIndicator = null;

    Sprite activeIndicator = null;

	public Indicators(Scene scene) throws Exception  {

        upIndicator = new Sprite(scene);
        
        upIndicator.setPosition(-0.1f, 1.0f, 0.00f);

        Mesh[] mesh = SceneLoader.loadMesh("src/main/resources/tiland/models/icon_up.fbx", "src/main/resources/tiland/textures/");
        Texture texture = mesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 0.0f);
        mesh[0].setMaterial(material);

        upIndicator.setMeshes(mesh);

        upIndicator.flags.collidable = false;
        upIndicator.flags.dynamic = false;

        upIndicator.setLayer(1);
        scene.addEntity(upIndicator);
        upIndicator.setVisible(false);
    }

    public void deactivateIndicator() {
 
        if (activeIndicator == null)
            return;

        activeIndicator.setVisible(false);
        activeIndicator = null;
    }

    public void activateUpIndicator() {
        activeIndicator = upIndicator;
        activeIndicator.setVisible(true);
    }

    public Sprite getActiveIndicator() {
        return activeIndicator;
    }
}
