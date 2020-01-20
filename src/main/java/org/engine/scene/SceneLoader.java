package org.engine.scene;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.engine.core.BoundingBox;
import org.engine.Utilities;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.assimp.*;

import static org.lwjgl.assimp.Assimp.*;

import org.engine.renderer.Material;
import org.engine.renderer.Mesh;
import org.engine.renderer.Texture;
import org.engine.renderer.TextureCache;

public class SceneLoader {

    public interface IEventHandler {

        public Entity preLoadEntityEvent(String type) throws Exception;
        public void postLoadEntityEvent(Entity entity) throws Exception;
    }

    public static void loadEntities(String resourcePath, String texturesDir, IEventHandler eventHandler) throws Exception {

        AIScene aiScene = aiImportFile(resourcePath, aiProcess_JoinIdenticalVertices | aiProcess_Triangulate);
        if (aiScene == null) {
            String error = aiGetErrorString();
            throw new Exception(error);
        }

        int numMaterials = aiScene.mNumMaterials();
        PointerBuffer aiMaterials = aiScene.mMaterials();
        List<Material> materials = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            processMaterial(aiMaterial, materials, texturesDir);
        }

        processNode(aiScene.mRootNode(), aiScene, materials, eventHandler);
    }

    public static void processNode(AINode aiNode, AIScene aiScene, List<Material> materials, IEventHandler eventHandler) throws Exception {

        if (aiNode.mMetadata() != null) {

            for (int i = 0; i < aiNode.mMetadata().mNumProperties(); i++) {

                AIString blah = (aiNode.mMetadata().mKeys().get(i));

                if (blah.dataString().equalsIgnoreCase("p_type")) {

                    System.out.println(blah.dataString());

                    // Get the value.
                    AIMetaDataEntry entry = (aiNode.mMetadata().mValues().get(i));

                    if  (entry.mType() == AI_AISTRING) {

                        int capacity = entry.sizeof();
                        java.nio.ByteBuffer buffer = entry.mData(capacity);

                        // I don't know the correct way to use ASSIMP to cast this data to appropriate types, but this works for now.
                        String valueString = MemoryUtil.memASCII(buffer);

                        // Theres a bunch of nasty whitespace leading the valid text. I'm assuming it's some sort of type header data.
                        valueString = valueString.trim();

                        if (valueString.compareTo("terrain") == 0) {

                            Entity entity = eventHandler.preLoadEntityEvent(valueString);

                            Mesh[] meshes = parseMesh(aiScene, aiNode, materials);
                            entity.setMeshes(meshes);

                            eventHandler.postLoadEntityEvent(entity);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < aiNode.mNumChildren(); i++) {

            AINode child = AINode.create(aiNode.mChildren().get(i));
            processNode(child, aiScene, materials, eventHandler);
        }
    }

    public static Mesh[] loadMesh(String resourcePath, String texturesDir) throws Exception {

        AIScene aiScene = aiImportFile(resourcePath, aiProcess_JoinIdenticalVertices | aiProcess_Triangulate);
        if (aiScene == null) {
            String error = aiGetErrorString();
            throw new Exception(error);
        }

        return parseMesh(aiScene, texturesDir);
    }

    public static Mesh[] parseMesh(AIScene aiScene, AINode aiNode, List<Material> materials) throws Exception {

        int numMeshes = aiNode.mNumMeshes();
        IntBuffer aiMeshes = aiNode.mMeshes();
        Mesh[] meshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; i++) {

            int meshIndex = aiMeshes.get(i);
            AIMesh aiMesh = AIMesh.create(aiScene.mMeshes().get(meshIndex));
            Mesh mesh = processMesh(aiMesh, materials);
            meshes[i] = mesh;
        }

        return meshes;
    }


    public static Mesh[] parseMesh(AIScene aiScene, String texturesDir) throws Exception {

        int numMaterials = aiScene.mNumMaterials();
        PointerBuffer aiMaterials = aiScene.mMaterials();
        List<Material> materials = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            processMaterial(aiMaterial, materials, texturesDir);
        }

        int numMeshes = aiScene.mNumMeshes();
        PointerBuffer aiMeshes = aiScene.mMeshes();
        Mesh[] meshes = new Mesh[numMeshes];
        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
            Mesh mesh = processMesh(aiMesh, materials);
            meshes[i] = mesh;
        }

        return meshes;
    }

    private static void processMaterial(AIMaterial aiMaterial, List<Material> materials, String texturesDir) throws Exception {
        AIColor4D colour = AIColor4D.create();

        AIString path = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, path, (IntBuffer) null, null, null, null, null, null);
        String textPath = path.dataString();
        Texture texture = null;
        if (textPath != null && textPath.length() > 0) {
            TextureCache textCache = TextureCache.getInstance();
            String textureFile = "";
            if ( texturesDir != null && texturesDir.length() > 0 ) {
                textureFile += texturesDir + "/";
            }
            textureFile += textPath;
            textureFile = textureFile.replace("//", "/");
            textureFile = textureFile.replace( "\\", "/");
            texture = textCache.getTexture(textureFile);
        }

        Vector4f ambient = Material.DEFAULT_COLOR;
        int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, colour);
        if (result == 0) {
            ambient = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
        }

        Vector4f diffuse = Material.DEFAULT_COLOR;
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, colour);
        if (result == 0) {
            diffuse = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
        }

        Vector4f specular = Material.DEFAULT_COLOR;
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, colour);
        if (result == 0) {
            specular = new Vector4f(colour.r(), colour.g(), colour.b(), colour.a());
        }

        Material material = new Material(ambient, diffuse, specular, 1.0f);
        material.setTexture(texture);
        materials.add(material);
    }

    private static Mesh processMesh(AIMesh aiMesh, List<Material> materials) throws Exception {
        List<Float> vertices = new ArrayList<>();
        List<Float> textures = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        BoundingBox bbox = new BoundingBox();

        processVertices(aiMesh, vertices, bbox);
        processNormals(aiMesh, normals);
        processTextCoords(aiMesh, textures);
        processIndices(aiMesh, indices);

        Mesh mesh = new Mesh(Utilities.listToArray(vertices),
                Utilities.listToArray(textures),
                Utilities.listToArray(normals),
                Utilities.listIntToArray(indices),
                bbox
        );
        Material material;
        int materialIdx = aiMesh.mMaterialIndex();
        if (materialIdx >= 0 && materialIdx < materials.size()) {
            material = materials.get(materialIdx);
        } else {
            material = new Material();
        }
        mesh.setMaterial(material);

        return mesh;
    }

    private static void processVertices(AIMesh aiMesh, List<Float> vertices, BoundingBox bbox) {

        bbox.min.set( 9999,  9999,  9999);
        bbox.max.set(-9999, -9999, -9999);

        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        while (aiVertices.remaining() > 0) {
            AIVector3D aiVertex = aiVertices.get();

            float x =  aiVertex.x();
            float y =  aiVertex.z();
            float z = -aiVertex.y();

            vertices.add(x);
            vertices.add(y);
            vertices.add(z);

            bbox.min.x = Math.min(bbox.min.x, x);
            bbox.min.y = Math.min(bbox.min.y, y);
            bbox.min.z = Math.min(bbox.min.z, z);

            bbox.max.x = Math.max(bbox.max.x, x);
            bbox.max.y = Math.max(bbox.max.y, y);
            bbox.max.z = Math.max(bbox.max.z, z);
        }
    }

    private static void processNormals(AIMesh aiMesh, List<Float> normals) {
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        while (aiNormals != null && aiNormals.remaining() > 0) {
            AIVector3D aiNormal = aiNormals.get();
            normals.add(aiNormal.x());
            normals.add(aiNormal.z());
            normals.add(-aiNormal.y());
        }
    }

    private static void processTextCoords(AIMesh aiMesh, List<Float> textures) {
        AIVector3D.Buffer textCoords = aiMesh.mTextureCoords(0);
        int numTextCoords = textCoords != null ? textCoords.remaining() : 0;
        for (int i = 0; i < numTextCoords; i++) {
            AIVector3D textCoord = textCoords.get();
            textures.add(textCoord.x());
            textures.add(1 - textCoord.y());
        }
    }

    private static void processIndices(AIMesh aiMesh, List<Integer> indices) {
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                int index = buffer.get();
                indices.add(index);
            }
        }
    }
}