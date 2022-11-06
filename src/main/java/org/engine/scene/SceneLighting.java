package org.engine.scene;

import org.joml.Vector3f;
import org.engine.renderer.DirectionalLight;
import org.engine.renderer.PointLight;
import org.engine.renderer.SpotLight;

import java.util.List;
import java.util.ArrayList;

public class SceneLighting {

    private Vector3f ambientLight;

    private PointLight[] pointLightList;

    private SpotLight[] spotLightList;

    private List<DirectionalLight> directionalLights = new ArrayList<DirectionalLight>();

    public Vector3f getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(Vector3f ambientLight) {
        this.ambientLight = ambientLight;
    }

    public PointLight[] getPointLightList() {
        return pointLightList;
    }

    public void setPointLightList(PointLight[] pointLightList) {
        this.pointLightList = pointLightList;
    }

    public SpotLight[] getSpotLightList() {
        return spotLightList;
    }

    public void setSpotLightList(SpotLight[] spotLightList) {
        this.spotLightList = spotLightList;
    }

    public DirectionalLight getDirectionalLight() {
        return directionalLights.get(0);
    }

    public List<DirectionalLight> getDirectionalLights() {
        return directionalLights;
    }

    public void addDirectionalLight(DirectionalLight directionalLight) {
        directionalLights.add(directionalLight);
    }
}