package com.etereot.visiblespectrum;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by Admin on 11/01/2015.
 */
public class Triangle {

    //El pading a cada lado en bmp es 8 izquierda y derecha y 18 arriba y abajo en imagen de 144-144
    //x,y is the center of the bitmap
    private float x,y,triangleCoords[];

    private FloatBuffer vertexBuffer;

    private ArrayList Lados;

    private PointF center;
    private boolean reflec;

    private float peq;

    static final int COORDS_PER_VERTEX = 2;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    //Used for the shader
    private final int mProgram;

    //Used when drawing
    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = 3;
    private final int vertexStride = 8; //COORDS_PER_VERTEX * 4;  4 bytes per vertex




    public Triangle(PointF center,boolean reflec){

        this.reflec = reflec;
        //this.bmp = bmp;
        this.center = center;

        computePoints(center, /*bmp.getWidth() * 0.8888f fix it later */ 0.2f);

        Lados = new ArrayList();
        //In counterclockwise order v1,v2; v3,v2; v3,v1
        Lados.add(new Recta(triangleCoords[0],triangleCoords[1],triangleCoords[2],triangleCoords[3]));
        Lados.add(new Recta(triangleCoords[4],triangleCoords[5],triangleCoords[2], triangleCoords[3]));
        Lados.add(new Recta(triangleCoords[4], triangleCoords[5],triangleCoords[0],triangleCoords[1]));


        //x=center.x - bmp.getWidth()/ 2;
        //y=center.y + peq + bmp.getHeight()*0.125f;


        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);


        //To render vertices
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        //To render faces
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);

    }



    //para el on draw
    public void draw() {

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data, defining the attribute
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }



    //Devuelve un vertice, segun el int que pases
    //This is horrible, fix it later
    public PointF getCoordenadas(int i){
        if (i==0) return new PointF(triangleCoords[0],triangleCoords[1]);
        else if (i==1) return new PointF(triangleCoords[2],triangleCoords[3]);
        else if (i==2) return new PointF(triangleCoords[4],triangleCoords[5]);
        else return null;

    }

    //Devuelve su numero en la lista, para ver su orden respectivo
    //Same as above
    public int getNumero(PointF punto) {
        if (punto == new PointF(triangleCoords[0],triangleCoords[1])) return 0;
        else if (punto == new PointF(triangleCoords[0],triangleCoords[1])) return 1;
        else if (punto == new PointF(triangleCoords[0],triangleCoords[1])) return 2;
        else return -1;
    }


    //devuelve el array que contiene los lados
    public ArrayList getLados(){return Lados;}

    public boolean getReflec(){return reflec;}

    public PointF getCenter(){return center;}

    private void computePoints(PointF centre,float lado){

        float mySide = lado/2;
        float peq = 0.57735f * mySide;
        float gran = mySide/0.86025f;

        this.peq = peq;

        triangleCoords = new float[]{
                centre.x - mySide, centre.y + peq,
                centre.x, centre.y - gran,
                centre.x + mySide, centre.y + peq
        };

    }

    /*The shader,i have to investigate how they work*/
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";









}


