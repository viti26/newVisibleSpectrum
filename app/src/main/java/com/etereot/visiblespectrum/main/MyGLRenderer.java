package com.etereot.visiblespectrum.main;

import javax.microedition.khronos.egl.EGLConfig;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.etereot.visiblespectrum.geometry.Triangle;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by victor on 13/07/15.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Triangle mTriangle, rTriangle, kTriangle, oTriangle, aTriangle, bTriangle;
    private Luz mLuz;

    /* Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private float[] mProjectionMatrix = new float[16];


    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTriangle = new Triangle(new PointF(0.5f, 0), true);
        //rTriangle = new Triangle();
        //kTriangle = new Triangle();
        //oTriangle = new Triangle();
        //aTriangle = new Triangle();
        //bTriangle = new Triangle();

        MyView.Objetos = new ArrayList<Triangle>(5);
        MyView.Objetos.add(mTriangle);

        mLuz = new Luz();

    }

    public void onDrawFrame(GL10 unused) {

        mLuz.update();

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        mTriangle.draw();
        mLuz.draw();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {

        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

    }

    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }



    public void setAngle(float angle) {mLuz.setDirection(angle); }
    public float getAngle(){return mLuz.getAngle();}


}