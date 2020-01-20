package org.engine.renderer;

import java.util.HashMap;
import java.util.Map;

public class ShaderCache {

    private static ShaderCache INSTANCE;

    private Map<String, Shader> shadersMap;

    private ShaderCache() {
        shadersMap = new HashMap<>();
    }

    public static synchronized ShaderCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ShaderCache();
        }

        return INSTANCE;
    }

    public Shader addShader(String name) throws Exception {

        name = name.toLowerCase();

        Shader shader = new Shader(name);
        shadersMap.put(name, shader);

        return shader;
    }

    public void addShader(String name, Shader shader) throws Exception {

        name = name.toLowerCase();

        shadersMap.put(name, shader);
    }

    public Shader getShader(String name) {

        name = name.toLowerCase();

        return shadersMap.get(name);
    }
}