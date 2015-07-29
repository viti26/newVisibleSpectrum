package com.etereot.visiblespectrum.main;

import android.graphics.PointF;
import android.opengl.GLES20;
import android.util.Log;

import com.etereot.visiblespectrum.geometry.*;
import com.etereot.visiblespectrum.util.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;




/**
 * Created by Admin on 12/01/2015.
 */
public class Luz extends Mates {

    private static final String TAG ="Luz";

    //centro de giro de la luz y centro pantalla
    private PointF inicio;
    //punto de giro de donde parten la luz
    private PointF iniciog;
    //punto de inicio de cada lado de la luz
    private PointF iniz,inid;

    //angle de giro de la luz
    private float angle;

    //mitad grosor de la luz principal
    private final float tLuz=0.01f;

    private final float radio=0.1f;

    //Rayo de luz principal
    private Rayo luz;

    private ArrayList<Rayo> luces;

    // Set color with red, green, blue and alpha (opacity) values
    static float color[] = { 1.0f, 1.0f, 1.0f, 1.0f}; //White

    //distancia segura de que no choca
    final float segdist = 2*tLuz + 0.001f;



    //Used for the shader
    private final int mProgram;

    //Used when drawing
    private int mPositionHandle;
    private int mColorHandle;

    //Coords_per_vertex is common to everything, should not be defined everywhere...
    static final int COORDS_PER_VERTEX = 2;

    private final int vertexStride = 8; //COORDS_PER_VERTEX * 4;  4 bytes per vertex

    //Buffers objects stored here
    final int[] vbo = new int[1];
    final int[] ibo = new int[1];

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private int indice_size;
    private int coords_size;




    public Luz(){

        inicio = new PointF(0,-1);
        iniciog = new PointF(inicio.x,inicio.y-radio);

        iniz = new PointF(iniciog.x-tLuz,iniciog.y);
        inid = new PointF(iniciog.x + tLuz, iniciog.y);

        luz = new Rayo();
        //Initializing geometry
        luz.addPuntosdechoqueiz(iniz);
        luz.addPuntosdechoquede(inid);

        luces = new ArrayList<Rayo>();

        setLuz();
        update();

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

        //No need to recreate them, this gives you a handle to the buffer object
        GLES20.glGenBuffers(1, vbo, 0);
        GLES20.glGenBuffers(1, ibo, 0);

    }

    public void setLuz(){

        //angulo inicial
        angle = 90;

        iniciog.set(inicio.x,inicio.y+radio);

        iniz.set(iniciog.x-tLuz,iniciog.y);
        inid.set(iniciog.x + tLuz, iniciog.y);

        //Initializing geometry
        luz.addPuntosdechoqueiz(iniz);
        luz.addPuntosdechoquede(inid);


        //Initial direction
        luz.setIzRecta(iniz, new PointF(0, 0.5f));
        luz.setDeRecta(inid, new PointF(0, 0.5f));
        //I think this is no longer needed, revise later
        luz.setTluz();


    }

    public void update(){
        //reiniciar array de luces
        luces.clear();
        //Same as above
        luz.clearchoque();
        luz.setTluz();
        updateRay(null, luz);

        createBuffers();
        bindBuffers();

    }


    //dada una lista de objetos y un rayo, calcula su geometria
    //con colisiones y demas
    private void updateRay(Triangle objeto,Rayo rayo){

        Triangle choque;
        ArrayList<Estruct> lista = new ArrayList<Estruct>();

        if(luces.size()>10) return;

        //basicamente ir pasando por to los objetos para mirar si chocan
        //cuando cambies lista de objetos, ay que modificarlo

        afuera:

        if(!MyView.Objetos.isEmpty()){

            //crea una lista con las distancias al origen de la luz de cada objeto
            for(int i=0;i<MyView.Objetos.size();i++){ lista.add(new Estruct(MyView.Objetos.get(i),distancia(rayo.getIzRecta().getP1(), MyView.Objetos.get(i).getCenter()))); }

            Collections.sort(lista);

            for(int j=0;j<lista.size();j++){

                choque = lista.get(j).objeto;

                if(choque==objeto) continue;

                //to debug
                if(DebugConfig.ON){
                    Log.i(TAG, "Angle" + "/n" + angle);
                }

                if(angle>180){
                    if(choque.getCenter().y>rayo.getIzRecta().getP1().y + segdist) continue;
                    if (angle>270) if(choque.getCenter().x<rayo.getIzRecta().getP1().x - segdist) continue;
                    else if(choque.getCenter().x>rayo.getIzRecta().getP1().x + segdist) continue;
                }
                else {
                    if(choque.getCenter().y<rayo.getIzRecta().getP1().y - segdist) continue;
                    if(angle<90){if(choque.getCenter().x<rayo.getIzRecta().getP1().x - segdist) continue;}
                    else if(choque.getCenter().x>rayo.getIzRecta().getP1().x + segdist) continue;
                }


                if(!calcula(choque,rayo,true))break afuera;
                if(!calcula(choque,rayo,false)) break afuera;

            }


            PointF a = rayo.izChoqueBordes();
            PointF b = rayo.deChoqueBordes();

            rayo.addPuntosdechoqueiz(a);
            rayo.addPuntosdechoquede(b);

            /*Top-left corner
            Top-right corner
            Down-left corner
            Down-right corner
            Maybe all this code should be
            inside some function with "choquebordes"
             */
            if(a.x==-1 && b.y==1) {
                rayo.addPuntosdechoqueiz(new PointF(-1,1));
            } else if (a.y==1 && b.x==1){
                rayo.addPuntosdechoquede(new PointF(1,1));
            } else if (a.y==-1 && b.x==-1){
                rayo.addPuntosdechoqueiz(new PointF(-1,-1));
            } else if (a.x==1 && b.y==-1){
                rayo.addPuntosdechoqueiz(new PointF(1,-1));
            }

        }

        rayo.setCoords();

    }



    //dado un objeto y un lado del rayo, mirar si/no choca y donde
    //lo de lado esta mal echo, estaria mejor aciendo los dos a la vez
    private boolean calcula(Triangle objeto,Rayo rayo,boolean lado/*We are talking about the left or right line?*/){


        ArrayList<PointF> puntosdechoque;
        PointF uno,dos;
        Recta actual_line,oposite_line;
        Rayo a;
        PointF possible_vertex; //vertice de un lado

        boolean reflec = objeto.getReflec();
        ArrayList<Recta> Lados = objeto.getLados();
        float tLuz = rayo.getTluz();
        int vertex_number,side_number =0;


        /* line is going to be the current line to calculate the collisions
         * line_O is the another side of the ray, the other line
         */
        if(lado) {
            actual_line = rayo.getIzRecta();
            oposite_line= rayo.getDeRecta();
            puntosdechoque = rayo.getLeft_collision_vertex();
        }
        else {
            actual_line = rayo.getDeRecta();
            oposite_line = rayo.getIzRecta();
            puntosdechoque = rayo.getRight_collision_vertex();
        }


        /* You go through every side of the object to see
         * if it collides with the ray, if not you keep looking.
         * If there is more than one collision, you take the nearest
         * to the origin of the ray
         */
        uno=linesCollision(actual_line, Lados.get(0), true);

        for(int i=1;i<3;i++){
            dos=linesCollision(actual_line, Lados.get(i), true);
            if(uno==null) {uno=dos;side_number=i; continue;}
            if(dos==null) continue;
            if(distancia(uno,actual_line.getP1())>distancia(dos,actual_line.getP1())) {uno = dos; side_number=i;}
        }

        if(uno==null) return true;

        if(DebugConfig.ON) Log.i(TAG, "Collision found");

        //First collision point
        puntosdechoque.add(uno);


        /* Now you look for the next possible point, the ray could be totally stopped
         * so no more point should be added to this line unless it has hit a corner, or
         * it could had been only partially stopped. Both two last cases need determine
         * what is the next object's vertex to add.
         */

        possible_vertex = Lados.get(side_number).getP1();
        if(DebugConfig.ON) if(possible_vertex==null) Log.e(TAG,"Null vertex" +"\n"+ side_number);

        //Stored for future use, for usage is call previous_distance, now is not important
        double previous_distance = distancia(possible_vertex,actual_line);

        //With this we will know if possible_vertex is in between the lines hence inside the ray
        if(previous_distance<2*tLuz && distancia(possible_vertex,oposite_line)<2*tLuz){

            //Second collision point
            puntosdechoque.add(possible_vertex);

            //If the object reflects the ray is created a new one and stored in luces.
            if(reflec) {
                if(DebugConfig.ON) {
                    if(possible_vertex==null) Log.e(TAG,"Null possible_vertex 1");
                    if(uno==null) Log.e(TAG,"Null uno 1");
                }
                if (lado) a = new Rayo(possible_vertex,uno,reflect(actual_line.getVector(),Lados.get(side_number).getNormal()));
                else a = new Rayo(uno,possible_vertex,reflect(actual_line.getVector(), Lados.get(side_number).getNormal()));
                updateRay(objeto, a);
                luces.add(a);
            }

            //Here you store what vertex collided with the ray for future use.
            vertex_number=objeto.getNumber(possible_vertex);
            if(DebugConfig.ON) if(vertex_number==-1) Log.e(TAG,"Wrong vertex 1" +"\n"+ vertex_number + "\n"+possible_vertex.x + possible_vertex.y);

        }
        //The first vertex is not colliding with the ray, but maybe is the second one.
        else {

            possible_vertex=Lados.get(side_number).getP2();
            if(DebugConfig.ON) if(possible_vertex==null) Log.e(TAG,"Null vertex" +"\n"+ side_number);

            //Stored for future use
            previous_distance= distancia(possible_vertex,actual_line);

            if(previous_distance<2*tLuz && distancia(possible_vertex,oposite_line)<2*tLuz) {

                //Second collision point.
                puntosdechoque.add(possible_vertex);

                if(reflec) {
                    if(DebugConfig.ON) {
                        if(possible_vertex==null) Log.e(TAG,"Null possible_vertex 2");
                        if(uno==null) Log.e(TAG,"Null uno 2");
                    }
                    if (lado) a = new Rayo(possible_vertex,uno,reflect(actual_line.getVector(),Lados.get(side_number).getNormal()));
                    else a = new Rayo(uno,possible_vertex,reflect(actual_line.getVector(), Lados.get(side_number).getNormal()));
                    updateRay(objeto, a);
                    luces.add(a);
                }

                vertex_number=objeto.getNumber(possible_vertex);
                if(DebugConfig.ON) if(vertex_number==-1) Log.e(TAG,"Wrong vertex 2" +"\n"+ vertex_number);
            }
            else {
                //No vertex was found inside the ray, so it must have stopped completely, the first case.
                PointF b=linesCollision(oposite_line,Lados.get(side_number),true);
                rayo.addPuntosdechoquede(b);

                if(reflec) {
                    if(DebugConfig.ON) {
                        if(b==null) Log.e(TAG,"Null b 3");
                        if(uno==null) Log.e(TAG,"Null uno 3");
                    }
                    a = new Rayo(b,uno,reflect(actual_line.getVector(), Lados.get(side_number).getNormal()));
                    updateRay(objeto, a);
                    luces.add(a);
                }
                return false;
            }
        }

        //We recall the vertex founded inside the ray and try to catch the next one
        if(lado){
            if (vertex_number==2) vertex_number = 0;
            else vertex_number++;

            if (side_number==2) side_number = 0;
            else side_number++;
        } else {
            if (vertex_number==0) vertex_number = 2;
            else vertex_number--;

            if (side_number==0) side_number = 2;
            else side_number--;
        }

        possible_vertex=objeto.getCoordinates(vertex_number);
        if(DebugConfig.ON) if(possible_vertex==null) Log.e(TAG, "Null vertex");

        double distance = distancia(possible_vertex,actual_line);

        //Is the new possible_vertex inside the ray?
        if (distance<2*tLuz && distancia(possible_vertex,oposite_line)<2*tLuz) {

            //The new vertex
            if (previous_distance < distance) {

                if(reflec) {

                    if(DebugConfig.ON) {
                        if(possible_vertex==null) Log.e(TAG,"Null possible_vertex 4");
                        if(getLast(puntosdechoque)==null) Log.e(TAG,"Null estrange thing 4");
                    }

                    if (lado) a = new Rayo(possible_vertex,getLast(puntosdechoque),reflect(actual_line.getVector(),Lados.get(side_number).getNormal()));
                    else a = new Rayo(getLast(puntosdechoque),possible_vertex,reflect(actual_line.getVector(), Lados.get(side_number).getNormal()));
                    updateRay(objeto, a);
                    luces.add(a);
                }

                puntosdechoque.add(possible_vertex);

            }
            //New vertex is behind so is not considered

        }
        //The new vertex is not inside the ray, we have to see if it is in one side or the other.
        else if(distancia(possible_vertex,oposite_line)<distance){

            //Is closer to the opposite line, is a corner so you have to calculate the collision point

            PointF b=linesCollision(rayo.getDeRecta(), Lados.get(side_number), true);

            if(reflec) {

                if(DebugConfig.ON) {
                    if(b==null) Log.e(TAG,"Null b 5");
                    if(getLast(puntosdechoque)==null) Log.e(TAG,"Null estrange thing 5");
                }

                a = new Rayo(b,getLast(puntosdechoque),reflect(actual_line.getVector(), Lados.get(side_number).getNormal()));

                updateRay(objeto, a);
                luces.add(a);
            }

            rayo.addPuntosdechoquede(b);

            return false;

        }

        //The new vertex is behind son is not considered.

        //This is so no ray change her size, if it does it is created a new ray stopping the old
        //one in it's last point through a normal.
        PointF normal_vertex = rayo.getIzRecta().getNormal();
        PointF j = getLast(puntosdechoque);
        Recta normal = new Recta(j.x,j.y,j.x+normal_vertex.x,j.y+normal_vertex.y);

        if(lado){
            rayo.addPuntosdechoquede(linesCollision(rayo.getDeRecta(), normal, false));
        } else {
            rayo.addPuntosdechoqueiz(linesCollision(rayo.getIzRecta(), normal, false));
        }

        if(DebugConfig.ON) {
            if(getLast(rayo.getLeft_collision_vertex())==null) Log.e(TAG,"Null estrange thing 6");
            if(getLast(rayo.getRight_collision_vertex())==null) Log.e(TAG,"Null estrange thing 7");
        }

        a = new Rayo(getLast(rayo.getLeft_collision_vertex()),getLast(rayo.getRight_collision_vertex()),rayo.getIzRecta().getVector());
        updateRay(objeto, a);
        luces.add(a);

        return false;
    }


    private void createBuffers(){

        float[] rayCoords = luz.getRayCoords();
        short[] drawOrder = luz.getdrawOrder();

        for(int i=0;i<luces.size();i++){
            drawOrder = Mates.concatenate(drawOrder,sum(luces.get(i).getdrawOrder(),rayCoords.length/2 ));
            rayCoords = Mates.concatenate(rayCoords, luces.get(i).getRayCoords());
        }

        coords_size = rayCoords.length;
        indice_size = drawOrder.length;


        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                rayCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(rayCoords);
        vertexBuffer.position(0);


        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        indexBuffer = dlb.asShortBuffer();
        indexBuffer.put(drawOrder);
        indexBuffer.position(0);

    }


    //Draw the light
    public void draw(){

        if(DebugConfig.ON) Log.i(TAG,"The number of lights actually rendered is "+ luces.size());

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, Luz.color, 0);

        // Prepare the object coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indice_size, GLES20.GL_UNSIGNED_SHORT, 0);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);



    }


    public void setDirection(float angle){

        iniciog.set((float)(inicio.x+radio*Math.cos(Math.toRadians(angle))),(float)(inicio.y+radio*Math.sin(Math.toRadians(angle))));

        PointF vector = new PointF(iniciog.x-inicio.x,iniciog.y-inicio.y);
        PointF normal = normal(vector);

        iniz.set(iniciog.x+normal.x*tLuz,iniciog.y+normal.y*tLuz);
        inid.set(iniciog.x-normal.x*tLuz,iniciog.y-normal.y*tLuz);

        luz.setIzRecta(iniz,vector);
        luz.setDeRecta(inid,vector);

        this.angle = angle;
    }

    public float getAngle(){return angle;}

    public void bindBuffers(){

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, coords_size
                * 4, vertexBuffer, GLES20.GL_STREAM_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indice_size
                * 2, indexBuffer, GLES20.GL_STREAM_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

    }


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
