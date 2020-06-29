package org.engine.scene;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.engine.core.BoundingBox;
import org.engine.Utilities;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Matrix4f;
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
        
        public Entity preLoadEntityEvent(Map<String, String>properties);
        public void postLoadEntityEvent(Entity entity, Map<String, String>properties);
    }

    public static void loadEntities(Entity sceneRoot, String resourcePath, String texturesDir, IEventHandler eventHandler) {

        AIScene aiScene = aiImportFile(resourcePath, aiProcess_JoinIdenticalVertices | aiProcess_Triangulate);
        if (aiScene == null) {
            String error = aiGetErrorString();
            return;
            //throw new Exception(error);
        }

        int numMaterials = aiScene.mNumMaterials();
        PointerBuffer aiMaterials = aiScene.mMaterials();
        List<Material> materials = new ArrayList<>();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiMaterials.get(i));
            processMaterial(aiMaterial, materials, texturesDir);
        }

        processNode(sceneRoot, aiScene.mRootNode(), aiScene, materials, eventHandler);
    }

    public static void processNode(Entity sceneRoot, AINode aiNode, AIScene aiScene, List<Material> materials, IEventHandler eventHandler) {

        if (aiNode.mMetadata() != null) {

            Map<String, String> properties = new HashMap<>();
            properties.clear();

            AIString name = aiNode.mName();           

            // Load all of the "p_*" properties into a map.
            for (int i = 0; i < aiNode.mMetadata().mNumProperties(); i++) {

                AIString key = (aiNode.mMetadata().mKeys().get(i));

                String propName = key.dataString();
                if (propName.contains("p_")) {

                    // Get the value.
                    AIMetaDataEntry value = (aiNode.mMetadata().mValues().get(i));

                    if  (value.mType() == AI_AISTRING) {
              
                        int capacity = value.sizeof();
                        java.nio.ByteBuffer buffer = value.mData(capacity);
     
                        AIString aiString = AIString.create(MemoryUtil.memAddress(buffer));
                        
                        properties.put(propName, aiString.dataString());
                    } else if (value.mType() == AI_FLOAT) {

                        int capacity = value.sizeof();
                        java.nio.ByteBuffer buffer = value.mData(capacity);
      
                        float propValue = MemoryUtil.memGetFloat(MemoryUtil.memAddress(buffer));//     .memASCII(buffer);

                        // TODO: Kind of dumb. Storing this float as a string so I don't have to deal with mapping to multiple types.
                        properties.put(propName, String.valueOf(propValue));
                    }
                }
            }

            // Engine types.

            String p_type = properties.get("p_type");
            if (p_type != null && p_type == "terrain") {

                Entity entity = eventHandler.preLoadEntityEvent(properties);
                entity.setName(name.dataString().toLowerCase());
                entity.setParent(sceneRoot);
                
                Mesh[] meshes = parseMesh(aiScene, aiNode, materials);
                
                entity.setMeshes(meshes);

                eventHandler.postLoadEntityEvent(entity, properties);
            } else {

                // Game types.

                if (aiNode.mNumMeshes() > 0) {
                    Entity entity = eventHandler.preLoadEntityEvent(properties);
                    entity.setName(name.dataString().toLowerCase());
                    entity.setParent(sceneRoot);

                    Mesh[] meshes = parseMesh(aiScene, aiNode, materials);
                    entity.setMeshes(meshes);

                    Matrix4f transform = toMatrix(aiNode.mTransformation());
                    Vector3f position = new Vector3f();
                    
                    transform.getTranslation(position);
                    position.div(100);

                    entity.setPosition(position);

                    eventHandler.postLoadEntityEvent(entity, properties); 
                } else {

                    Entity entity = eventHandler.preLoadEntityEvent(properties);

                    // If etity is null, the game got all it needs out of the properties.
                    
                    //if (entity == null) {
                    //    entity = new Entity();
                    //}

                    if (entity != null) {
                        entity.setName(name.dataString().toLowerCase());
                        entity.setParent(sceneRoot);

                        Matrix4f transform = toMatrix(aiNode.mTransformation());
                        Vector3f position = new Vector3f();
                        
                        transform.getTranslation(position);
                        position.div(100);

                        entity.setPosition(position);

                        eventHandler.postLoadEntityEvent(entity, properties);
                    }
                }
            }
        }

        for (int i = 0; i < aiNode.mNumChildren(); i++) {

            AINode child = AINode.create(aiNode.mChildren().get(i));
            processNode(sceneRoot, child, aiScene, materials, eventHandler);
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

    public static Mesh[] parseMesh(AIScene aiScene, AINode aiNode, List<Material> materials) {

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

    private static void processMaterial(AIMaterial aiMaterial, List<Material> materials, String texturesDir) {
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

    private static Mesh processMesh(AIMesh aiMesh, List<Material> materials) {
        List<Float> vertices = new ArrayList<>();
        List<Float> textures = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        BoundingBox bbox = new BoundingBox();

        processVertices(aiMesh, vertices, bbox);
        processNormals(aiMesh, normals);
        processTextCoords(aiMesh, textures);
        processIndices(aiMesh, indices);

        Mesh mesh = new Mesh(Mesh.TRIANGLES, Utilities.listToArray(vertices),
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

    private static Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4) {

        Matrix4f result = new Matrix4f();

        result.m00(aiMatrix4x4.a1());
        result.m10(aiMatrix4x4.a2());
        result.m20(aiMatrix4x4.a3());
        result.m30(aiMatrix4x4.a4());
        result.m01(aiMatrix4x4.b1());
        result.m11(aiMatrix4x4.b2());
        result.m21(aiMatrix4x4.b3());
        result.m31(aiMatrix4x4.b4());
        result.m02(aiMatrix4x4.c1());
        result.m12(aiMatrix4x4.c2());
        result.m22(aiMatrix4x4.c3());
        result.m32(aiMatrix4x4.c4());
        result.m03(aiMatrix4x4.d1());
        result.m13(aiMatrix4x4.d2());
        result.m23(aiMatrix4x4.d3());
        result.m33(aiMatrix4x4.d4());

        return result;
    }
}