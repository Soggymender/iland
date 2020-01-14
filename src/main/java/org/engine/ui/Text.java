package org.engine.ui;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.engine.Utilities;
import org.engine.renderer.FontTexture;
import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.scene.Entity;

public class Text extends Entity {

    private static final float ZPOS = 0.0f;

    private static final int VERTICES_PER_QUAD = 4;

    private String text;
    private final FontTexture fontTexture;

    public Text(String text, FontTexture fontTexture) throws Exception {
        super();
        this.text = text;
        this.fontTexture = fontTexture;
        this.setMesh(buildMesh());
    }

    private Mesh buildMesh() {

        Mesh mesh = getMesh();
        Material material = null;
        if (mesh != null) {
            mesh.deleteBuffers();
            material = mesh.getMaterial();
        }

        if (material == null) {
            material = new Material(fontTexture.getTexture());
        }

        List<Float> positions = new ArrayList<>();
        List<Float> textCoords = new ArrayList<>();
        float[] normals   = new float[0];
        List<Integer> indices   = new ArrayList<>();

        char[] characters = text.toCharArray();
        int numChars = characters.length;

        float startX = 0;
        for(int i=0; i<numChars; i++) {

            FontTexture.CharInfo charInfo = fontTexture.getCharInfo(characters[i]);

            // Build a character tile composed by two triangles

            float charStartX = (float)charInfo.getStartX();
            float charWidth = (float)charInfo.getWidth();
            float texWidth = (float)fontTexture.getWidth();
            float texHeight = (float)fontTexture.getHeight();

            // Left Top vertex
            positions.add(startX); // x
            positions.add(0.0f); //y
            positions.add(ZPOS); //z
            textCoords.add(charStartX / texWidth );
            textCoords.add(0.0f);
            indices.add(i*VERTICES_PER_QUAD);

            // Left Bottom vertex
            positions.add(startX); // x
            positions.add(texHeight); //y
            positions.add(ZPOS); //z
            textCoords.add((float)charStartX / texWidth );
            textCoords.add(1.0f );
            indices.add(i*VERTICES_PER_QUAD + 1);

            // Right Bottom vertex
            positions.add(startX + charWidth); // x
            positions.add(texHeight); //y
            positions.add(ZPOS); //z
            textCoords.add((charStartX + charWidth) / texWidth);
            textCoords.add(1.0f);
            indices.add(i*VERTICES_PER_QUAD + 2);

            // Right Top vertex
            positions.add(startX + charWidth); // x
            positions.add(0.0f); //y
            positions.add(ZPOS); //z
            textCoords.add((charStartX + charWidth) / texWidth);
            textCoords.add(0.0f);
            indices.add(i*VERTICES_PER_QUAD + 3);

            // Add indices por left top and bottom right vertices
            indices.add(i*VERTICES_PER_QUAD);
            indices.add(i*VERTICES_PER_QUAD + 2);

            startX += charWidth;
        }

        float[] posArr = Utilities.listToArray(positions);
        float[] textCoordsArr = Utilities.listToArray(textCoords);
        int[] indicesArr = indices.stream().mapToInt(i->i).toArray();

        mesh = new Mesh(posArr, textCoordsArr, normals, indicesArr);
        mesh.setMaterial(material);

        return mesh;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;

        getMesh().deleteBuffers();
        setMesh(buildMesh());
    }
}
