package org.engine.sketch;

import org.engine.renderer.*;
import org.engine.renderer.shaders.*;
import org.engine.Utilities;

public class Sketch { 

    private Shader sketchShader;

    public Sketch() throws Exception {

        ShaderCache shaderCache = ShaderCache.getInstance();
        sketchShader = shaderCache.addShader("sketchShader");

        String vsName = Utilities.load("/shaders/default_vertex.vs");
        String fsName = Utilities.load("/shaders/default_fragment.fs");

        if (vsName.isEmpty() || fsName.isEmpty()) {
            return;
        }

        sketchShader.createVertexShader(vsName);
        sketchShader.createFragmentShader(fsName);
        sketchShader.link();

        SketchUniformManager uniformManager = new SketchUniformManager(sketchShader);
        sketchShader.setUniformManager(uniformManager);
    }



    
}