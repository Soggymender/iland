package org.engine.core;

import java.nio.IntBuffer;
import java.nio.FloatBuffer;

import org.engine.scene.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import org.engine.renderer.Camera;
import org.engine.renderer.Viewport;

public class Transform {

    private static Matrix4f modelMatrix = new Matrix4f();
    private static Matrix4f modelViewMatrix = new Matrix4f();

    private static Matrix4f orthoModelMatrix = new Matrix4f();

    public static Matrix4f buildModelViewMatrix(Entity entity, Matrix4f viewMatrix) {
        Vector3f rotation = entity.getRotation();

        modelMatrix.identity().translate(entity.getPosition()).
                rotateX(-rotation.x).
                rotateY(-rotation.y).
                rotateZ(-rotation.z).
                scale(entity.getScale());

        modelViewMatrix.set(viewMatrix);
        modelViewMatrix.mul(modelMatrix);

        return modelViewMatrix;
    }

    public static Matrix4f buildOrthoProjectionModelMatrix(Entity entity, Matrix4f orthoMatrix) {
        Vector3f rotation = entity.getRotation();

        modelMatrix.identity().translate(entity.getPosition()).
                rotateX(-rotation.x).
                rotateY(-rotation.y).
                rotateZ(-rotation.z).
                scale(entity.getScale());

        orthoModelMatrix.set(orthoMatrix);
        orthoModelMatrix.mul(modelMatrix);

        return orthoModelMatrix;
    }

    public static Vector3f unproject(Vector3f point, Camera camera) {

        Vector3f result = new Vector3f();

        Viewport viewport = camera.getViewport();

        Matrix4f model = new Matrix4f();
        Matrix4f projection = viewport.getProjectionMatrix();

        Matrix4f temp = new Matrix4f(model);
        temp = temp.mul(projection);
        temp = temp.invert();

        Vector4f vec = new Vector4f();

        vec.x = point.x;
        vec.y = point.y;
        vec.z = point.z;
        vec.w = 1.0f;


		vec.x = (vec.x - viewport.x) / viewport.width;
		vec.y = (vec.y - viewport.y) / viewport.height;

		vec.x = vec.x * 2 - 1;
		vec.y = vec.y * 2 - 1;
		vec.z = vec.z * 2 - 1;

        vec = vec.mul(temp);

        vec.w = 1.0f / vec.w;

        result.x = vec.x * vec.w;
        result.y = vec.y * vec.w;
        result.z = vec.z * vec.w;

        return result;
    }

    public static boolean unprojectInternal(float winx, float winy, float winz, FloatBuffer modelMatrix, FloatBuffer projMatrix, IntBuffer viewport, Vector3f obj_pos) {

//		float[] in = Project.in;
//		float[] out = Project.out;
/*
        

		__gluMultMatricesf(modelMatrix, projMatrix, finalMatrix);



		if (!__gluInvertMatrixf(finalMatrix, finalMatrix))

			return false;



		in[0] = winx;

		in[1] = winy;

		in[2] = winz;

		in[3] = 1.0f;



		// Map x and y from window coordinates

		in[0] = (in[0] - viewport.get(viewport.position() + 0)) / viewport.get(viewport.position() + 2);

		in[1] = (in[1] - viewport.get(viewport.position() + 1)) / viewport.get(viewport.position() + 3);



		// Map to range -1 to 1

		in[0] = in[0] * 2 - 1;

		in[1] = in[1] * 2 - 1;

		in[2] = in[2] * 2 - 1;



		__gluMultMatrixVecf(finalMatrix, in, out);



		if (out[3] == 0.0)

			return false;



		out[3] = 1.0f / out[3];



		obj_pos.put(obj_pos.position() + 0, out[0] * out[3]);

		obj_pos.put(obj_pos.position() + 1, out[1] * out[3]);

		obj_pos.put(obj_pos.position() + 2, out[2] * out[3]);


*/
		return true;

	}
}