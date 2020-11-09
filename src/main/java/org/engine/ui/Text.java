package org.engine.ui;

import java.util.ArrayList;
import java.util.List;

import org.engine.Utilities;
import org.engine.core.Rect;
import org.engine.renderer.*;
import org.engine.renderer.FontTexture;
import org.engine.scene.Entity;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class Text extends UiElement {

    private static final int VERTICES_PER_QUAD = 4;

    private String text;
    private final FontTexture fontTexture;

    private boolean xJustifyCenter = true;
    private boolean wordWrap = true;

    public Text(Canvas canvas, Entity parent, Rect rect, Rect anchor, Vector2f pivot, String textString, FontTexture fontTexture) throws Exception {

        super(canvas, parent, rect, anchor, pivot);

        flags.forwardsInput = false;
        flags.acceptsInput = false;
        flags.buildsMesh = false;

        this.text = textString;
        this.fontTexture = fontTexture;
    }

    private Mesh buildMesh() {

        Mesh mesh = getMesh();
        Material material = null;
        if (mesh != null) {
            material = mesh.getMaterial();
        }

        if (material == null) {
            material = new Material(fontTexture.getTexture());

            ShaderCache shaderCache = ShaderCache.getInstance();
            Shader defaultGuiShader = shaderCache.getShader("defaultGui");

            material.setShader(defaultGuiShader);
        }

        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        float[] normals   = new float[0];
        List<Integer> indices   = new ArrayList<>();

        char[] characters = text.toCharArray();
        int numChars = characters.length;

        float texWidth = (float)fontTexture.getWidth();
        float texHeight = (float)fontTexture.getHeight();

        float maxWidth = rectTrans.globalRect.xMax - rectTrans.globalRect.xMin;
        float maxHeight = rectTrans.globalRect.yMax - rectTrans.globalRect.yMin;

        float scale = canvas.getReferenceScale();

        float maxLineWidth = 0;

        // Pre calculate word width, and character fit.
        // Assume no word wrap and just see how long the line would be.
        for (int i = 0; i < numChars; i++) {

            FontTexture.CharInfo charInfo = fontTexture.getCharInfo(characters[i]);

            float charWidth = (float) charInfo.getWidth();
            maxLineWidth += charWidth * scale;
        }

        float startX = rectTrans.globalRect.xMin;

        // Cannot x center justify if the line is longer than the box can hold.
        // This covers word wrap as well.
        if (xJustifyCenter && maxLineWidth < maxWidth) {
            startX += (maxWidth - maxLineWidth) / 2;
        }

        float startY = rectTrans.globalRect.yMin + (maxHeight - (texHeight * scale)) / 2;
        startY = canvas.workingResolution.y - startY;

        float depth = getDepth();

        boolean inWord = false;
        float wordWidth = 0;

        boolean onLastLine = false;
        boolean full = false;

        for(int i=0; i<numChars; i++) {

            if (wordWrap && !inWord && characters[i] != ' ') {
                inWord = true;
                wordWidth = 0;

                // Pre-emptively calculate whether there's room on another line to wrap to.
                onLastLine = startY + (2 * texHeight * scale) > rectTrans.globalRect.yMax;

                // Start of a new word. Pre-calculate word wrap.
                for (int j = i; j < numChars + 1; j++) {

                    // End of string or word?
                    if (j == numChars || characters[j] == ' ' || characters[j] == '\n') {

                        // If it won't fit on this line.
                        // And we're not on the last line. If we are - mash whatever fits on here.

                        // TODO: If a word doesn't fit on a line all by itself, don't wrap it - just clip it, or ... it.
                        if ((wordWidth > maxWidth || startX + wordWidth > rectTrans.globalRect.xMax) && !onLastLine) {

                            // Wrap and continue.
                            startX = rectTrans.globalRect.xMin;
                            startY += texHeight * scale;

                            if (startY + texHeight > rectTrans.globalRect.yMax) {
                                full = true;
                            }
                        }

                        break;
                    }

                    FontTexture.CharInfo wordCharInfo = fontTexture.getCharInfo(characters[j]);
                    wordWidth += wordCharInfo.getWidth() * scale;
                }
            }

            if (full) {
                // Filled up vertically during word wrap.
                break;
            }

            FontTexture.CharInfo charInfo = fontTexture.getCharInfo(characters[i]);

            float charStartX = (float) charInfo.getStartX();
            float charWidth = (float) charInfo.getWidth();

            // If wrapping didn't happen for whatever reason, and this character doesn't fit, clip it and subsequent letters until a new word starts.
            if (startX + charInfo.getWidth() * scale <= rectTrans.globalRect.xMax) {

                if (characters[i] == ' ') {
                    inWord = false;
                }

                // Build a character tile composed by two triangles

                // Left Top vertex
                positions.add(startX); // x
                positions.add(startY); //y
                positions.add(depth); //z
                textCoords.add(charStartX / texWidth);
                textCoords.add(0.0f);
                indices.add(i * VERTICES_PER_QUAD);

                // Left Bottom vertex
                positions.add(startX); // x
                positions.add(startY - texHeight * scale); //y
                positions.add(depth); //z
                textCoords.add((float) charStartX / texWidth);
                textCoords.add(1.0f);
                indices.add(i * VERTICES_PER_QUAD + 1);

                // Right Bottom vertex
                positions.add(startX + charWidth * scale); // x
                positions.add(startY - texHeight * scale); //y
                positions.add(depth); //z
                textCoords.add((charStartX + charWidth) / texWidth);
                textCoords.add(1.0f);
                indices.add(i * VERTICES_PER_QUAD + 2);

                // Right Top vertex
                positions.add(startX + charWidth * scale); // x
                positions.add(startY); //y
                positions.add(depth); //z
                textCoords.add((charStartX + charWidth) / texWidth);
                textCoords.add(0.0f);
                indices.add(i * VERTICES_PER_QUAD + 3);

                // Add indices por left top and bottom right vertices
                indices.add(i * VERTICES_PER_QUAD);
                indices.add(i * VERTICES_PER_QUAD + 2);
            }

            startX += charWidth * scale;
        }

        float[] posArr = Utilities.listToArray(positions);
        float[] textCoordsArr = Utilities.listToArray(textCoords);
        int[] indicesArr = indices.stream().mapToInt(i->i).toArray();

        if (mesh == null) {
            mesh = new Mesh(Mesh.TRIANGLES, posArr, textCoordsArr, normals, indicesArr);
            setMesh(mesh);
            mesh.setMaterial(material);
        } else {
            mesh.set(Mesh.TRIANGLES,posArr, textCoordsArr, normals, indicesArr);
        }
        
        return mesh;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;

        flags.dirty = true;

//        if (getMesh() != null) {
//            getMesh().deleteBuffers();
//        }

//        setMesh(buildMesh());

 //       getMesh().getMaterial().setDiffuseColor(new Vector4f(1, 1, 1, 1));
    }

    public void update(float interval) {

        if (!flags.dirty) {
            super.update(interval);
            return;
        }

        super.update(interval);

        // Build the mesh but only add verts that fit.
     //   if (getMesh() != null) {
     //       getMesh().deleteBuffers();
     //   }

        buildMesh();
        getMesh().getMaterial().setDiffuseColor(new Vector4f(1, 1, 1, 1));
    }
}
