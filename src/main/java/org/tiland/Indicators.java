package org.tiland;

import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Scene;
import org.engine.scene.SceneLoader;

public class Indicators {
   
    Sprite upIndicator = null;
    Sprite downIndicator = null;

    Sprite activeIndicator = null;

	public Indicators(Scene scene, Avatar avatar) throws Exception  {

        upIndicator = LoadIndicator(scene, avatar, "src/main/resources/tiland/models/icon_up.fbx");
        downIndicator = LoadIndicator(scene, avatar, "src/main/resources/tiland/models/icon_down.fbx");
    }

    Sprite LoadIndicator(Scene scene, Avatar avatar, String meshFilename) throws Exception {

        Sprite indicator = new Sprite(scene);
        
        indicator.setPosition(-0.1f, 1.0f, 0.00f);

        Mesh[] mesh = SceneLoader.loadMesh(meshFilename, "src/main/resources/tiland/textures/");
        Texture texture = mesh[0].getMaterial().getTexture();
        Material material = new Material(texture, 0.0f);
        mesh[0].setMaterial(material);

        indicator.setMeshes(mesh);

        indicator.flags.collidable = false;
        indicator.flags.dynamic = false;

        indicator.setLayer(3);
        indicator.setParent(avatar);
        scene.addEntity(indicator);
        indicator.setVisible(false);
        
        return indicator;
    }

    public void deactivateIndicator() {
 
        if (activeIndicator == null)
            return;

        activeIndicator.setVisible(false);
        activeIndicator = null;
    }

    public void activateUpIndicator() {
        deactivateIndicator();

        activeIndicator = upIndicator;
        activeIndicator.setVisible(true);
    }

    public void activateDownIndicator() {
        deactivateIndicator();

        activeIndicator = downIndicator;
        activeIndicator.setVisible(true);
    }

    public Sprite getActiveIndicator() {
        return activeIndicator;
    }
}
