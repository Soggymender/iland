package org.engine.ui;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.engine.Utilities;
import org.engine.core.Rect;
import org.engine.renderer.FontTexture;
import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.FontTexture;
import org.engine.scene.Entity;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Text extends UiElement {

    private static final float ZPOS = 0.1f;

    private static final int VERTICES_PER_QUAD = 4;

    private String text;
    private final FontTexture fontTexture;

    public Text(Canvas canvas, Entity parent, Rect rect, Rect anchor, Vector2f pivot, String textString, FontTexture fontTexture) throws Exception {

        super(canvas, parent, rect, anchor, pivot);

        this.forwardsInput = false;
        this.acceptsInput = false;
        this.buildsMesh = false;

        this.text = textString;
        this.fontTexture = fontTexture;
        //this.setMesh(buildMesh());

        update();
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

        float maxWidth = rectTrans.globalRect.xMax - rectTrans.globalRect.xMin;
        float maxHeight = rectTrans.globalRect.yMax - rectTrans.globalRect.yMin;

        float scale = canvas.getReferenceScale();

        float startX = rectTrans.globalRect.xMin;
        float startY = rectTrans.globalRect.yMin;
        for(int i=0; i<numChars; i++) {

            FontTexture.CharInfo charInfo = fontTexture.getCharInfo(characters[i]);

            // Build a character tile composed by two triangles

            float charStartX = (float)charInfo.getStartX();
            float charWidth = (float)charInfo.getWidth();
            float texWidth = (float)fontTexture.getWidth();
            float texHeight = (float)fontTexture.getHeight();

            // Left Top vertex
            positions.add(startX); // x
            positions.add(startY); //y
            positions.add(ZPOS); //z
            textCoords.add(charStartX / texWidth );
            textCoords.add(0.0f);
            indices.add(i*VERTICES_PER_QUAD);

            // Left Bottom vertex
            positions.add(startX); // x
            positions.add(startY + texHeight * scale); //y
            positions.add(ZPOS); //z
            textCoords.add((float)charStartX / texWidth );
            textCoords.add(1.0f );
            indices.add(i*VERTICES_PER_QUAD + 1);

            // Right Bottom vertex
            positions.add(startX + charWidth * scale); // x
            positions.add(startY + texHeight * scale); //y
            positions.add(ZPOS); //z
            textCoords.add((charStartX + charWidth) / texWidth);
            textCoords.add(1.0f);
            indices.add(i*VERTICES_PER_QUAD + 2);

            // Right Top vertex
            positions.add(startX + charWidth * scale); // x
            positions.add(startY); //y
            positions.add(ZPOS); //z
            textCoords.add((charStartX + charWidth) / texWidth);
            textCoords.add(0.0f);
            indices.add(i*VERTICES_PER_QUAD + 3);

            // Add indices por left top and bottom right vertices
            indices.add(i*VERTICES_PER_QUAD);
            indices.add(i*VERTICES_PER_QUAD + 2);

            startX += charWidth * scale;
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

        getMesh().getMaterial().setDiffuseColor(new Vector4f(1, 1, 1, 1));
    }

    public void update() {

        super.update();

        // Build the mesh but only add verts that fit.
        setMesh(buildMesh());
        getMesh().getMaterial().setDiffuseColor(new Vector4f(1, 1, 1, 1));


        System.out.println(rectTrans.globalRect.xMin + ", " + rectTrans.globalRect.yMin);
//        super.update();

        //    Vector3f textPos = new Vector3f();
          //  textPos.x = rectTrans.globalRect.xMin;
            //textPos.y = rectTrans.globalRect.yMin;
            //textPos.z = 0.0f;
       // }
    }
}
