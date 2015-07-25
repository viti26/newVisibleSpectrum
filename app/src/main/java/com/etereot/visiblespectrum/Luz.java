package com.etereot.visiblespectrum;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;



/**
 * Created by Admin on 12/01/2015.
 */
public class Luz extends Mates {

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

    }

    public void setLuz(){

        //angulo inicial
        angle =90;

        iniciog.set(inicio.x,inicio.y-radio);

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
        luz.setTluz();
        updateRay(null, luz);

        createBuffers();
        bindBuffers();

    }


    //dada una lista de objetos y un rayo, calcula su geometria
    //con colisiones y demas
    private void updateRay(Triangle objeto,Rayo rayo){

        Triangle choque;

        //Clear the arrays
        rayo.clearchoque();

        if(luces.size()>10) return;


        ArrayList<Estruct> lista = new ArrayList<Estruct>();



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
    private boolean calcula(Triangle objeto,Rayo rayo,boolean lado){

        ArrayList<Recta> Lados = objeto.getLados();
        ArrayList<PointF> puntosdechoque;
        PointF uno,dos;
        Recta linea,lineao;

        boolean reflec = objeto.getReflec();

        Rayo a;
        int numero;

        PointF vertice; //vertice de un lado

        double distancia,odistancia;

        float tLuz = rayo.getTluz();


        //seleccion del lado del rayo a calcular
        if(lado) {
            linea = rayo.getIzRecta();
            lineao= rayo.getDeRecta();
            puntosdechoque = rayo.getPuntosdechoqueiz();
        }
        else {
            linea = rayo.getDeRecta();
            lineao = rayo.getIzRecta();
            puntosdechoque = rayo.getPuntosdechoquede();
        }



        //Calculo de si choca o no y si lo ace
        //calcula donde y lo mete en "uno"
        //numero es el lado donde a chocado
        uno=linesCollision(linea, Lados.get(0), true);

        numero=0;

        for(int i=1;i<3;i++){
            dos=linesCollision(linea, Lados.get(i), true);

            if(uno==null) {uno=dos;numero=i; continue;}
            if(dos==null) continue;
            if(distancia(uno,linea.getP1())>distancia(dos,linea.getP1())) {uno = dos; numero=i;}
        }

        if(uno==null) return true;

        puntosdechoque.add(uno);


        //Ahora se ve si el rayo se detiene o si continua pero
        //disminuido, para eso vertice es uno de los vertices
        //del lado donde a chocado(Tambien puede ser que este
        //en un vertice del objeto, pero se resuelve despues)
        vertice = Lados.get(numero).getP1();
        distancia = distancia(vertice,linea);

        //Si esta dentro del rayo, la diferencia sera menos que tLuz
        if(distancia<2*tLuz && distancia(vertice, lineao)<2*tLuz){

            puntosdechoque.add(vertice);

            //si el objeto refleja, comenzar a crear el nuevo rayo con el punto
            //de choque y el vertice del rayo que acabamos de calcular
            if(reflec) {
                if (lado) a = new Rayo(vertice,uno,reflect(linea.v,Lados.get(numero).n));
                else a = new Rayo(uno,vertice,reflect(linea.v,Lados.get(numero).n));
                updateRay(objeto, a);
                luces.add(a);
            }

            numero=objeto.getNumero(vertice);

        }
        //No esta el vertice 1, pero podria estar el 2
        else {

            //Lo mismo solo que con P2
            vertice=Lados.get(numero).getP2();
            distancia= distancia(vertice,linea);

            if(distancia<2*tLuz && distancia(vertice,lineao)<2*tLuz) {
                puntosdechoque.add(vertice);


                //si el objeto refleja, comenzar a crear el nuevo rayo con el punto
                //de choque y el vertice del rayo que acabamos de calcular
                if(reflec) {

                    if (lado) a = new Rayo(vertice,uno,reflect(linea.v,Lados.get(numero).n));
                    else a = new Rayo(uno,vertice,reflect(linea.v,Lados.get(numero).n));
                    updateRay(objeto, a);
                    luces.add(a);
                }

                numero=objeto.getNumero(vertice);
            }
            else {
                //es seguro que el otro lado choque,y se calcula siempre iz primero
                PointF b=linesCollision(rayo.getDeRecta(),Lados.get(numero),true);
                rayo.addPuntosdechoquede(b);

                //si el objeto refleja, comenzar a crear el nuevo rayo
                if(reflec) {
                    a = new Rayo(b,uno,reflect(linea.v,Lados.get(numero).n));
                    updateRay(objeto, a);
                    luces.add(a);
                }
                return false;
            }
        }

        //Esto es dado que los lados se definen en contra de las
        //agujas del relog
        if(lado){
            if (numero==2) numero = 0;
            else numero++;
        } else {
            if (numero==0) numero = 2;
            else numero--;
        }

        vertice=objeto.getCoordenadas(numero);

        odistancia=distancia(vertice,linea);



        //calculo si el vertice sigiente esta o no dentro del rayo
        if (odistancia<2*tLuz && distancia(vertice,lineao)<2*tLuz) {

            //El nuevo vertice sobresale, a de ser incluido
            if (distancia < odistancia) {
                puntosdechoque.add(vertice);
                //linea.setOrigen(vertice);


                //reflect aqui tambien
                if(reflec) {
                    if (lado) a = new Rayo(vertice,puntosdechoque.get(puntosdechoque.size()-2),reflect(linea.v,Lados.get(numero).n));
                    else a = new Rayo(puntosdechoque.get(puntosdechoque.size()-2),vertice,reflect(linea.v,Lados.get(numero).n));
                    updateRay(objeto, a);
                    luces.add(a);
                }

            }
            //nuevo vertice esta por detras, el nuevo origen
            //es el anterior vertice
            //else linea.setOrigen(puntosdechoque.get(puntosdechoque.size() - 1));



        }
        //El vertice esta por fuera, ay que mirar si por un lado o por el otro
        else if(distancia(vertice,lineao)<odistancia){
            //Mas cerca del otro lado, es una esquina y lo tipico
            //de que se calcula antes el izquierdo

            //Funciona por como esta definidos los lados y puntos
            if (numero==0) numero = 2;
            else numero--;

            PointF b=linesCollision(rayo.getDeRecta(), Lados.get(numero), true);
            rayo.addPuntosdechoquede(b);

            //si el objeto refleja, comenzar a crear el nuevo rayo con el punto
            //de choque y el vertice del rayo que acabamos de calcular
            if(reflec) {
                a = new Rayo(b,puntosdechoque.get(puntosdechoque.size()-1),reflect(linea.v,Lados.get(numero).n));

                updateRay(objeto, a);
                luces.add(a);
            }

            return false;

        }

        //el punto esta por detras, no cuenta y se ignora
        //else linea.setOrigen(puntosdechoque.get(puntosdechoque.size() - 1));
        PointF normalp = Mates.normalice(new PointF(rayo.getIzRecta().v.y, -rayo.getIzRecta().v.x));
        PointF j = puntosdechoque.get(puntosdechoque.size()-1);
        Recta normal = new Recta(j.x,j.y,j.x+normalp.x,j.y+normalp.y);

        if(lado){
            rayo.addPuntosdechoquede(linesCollision(rayo.getDeRecta(), normal, false));
        } else {
            rayo.addPuntosdechoqueiz(linesCollision(rayo.getIzRecta(), normal, false));
        }

        a = new Rayo(rayo.getPuntosdechoqueiz().get(rayo.getPuntosdechoqueiz().size()-1),rayo.getPuntosdechoquede().get(rayo.getPuntosdechoquede().size()-1),rayo.getIzRecta().v);
        updateRay(objeto, a);
        luces.add(a);

        return false;
    }

    private void createBuffers(){

        float[] rayCoords = luz.getRayCoords();
        for(int i=0;i<luces.size();i++){rayCoords = Mates.concatenate(rayCoords, luces.get(i).getRayCoords());}

        short[] drawOrder = luz.getdrawOrder();
        for(int i=0;i<luces.size();i++){drawOrder = Mates.concatenate(drawOrder, luces.get(i).getdrawOrder());}
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
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,indice_size,GLES20.GL_UNSIGNED_SHORT,0);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);


    }


    //Always for the main ray
    public void setDirection(float angle){

        this.angle= angle % 360;


        iniciog.set((float)(inicio.x+radio*Math.cos(Math.toRadians(angle))),(float)(inicio.y-radio*Math.sin(Math.toRadians(angle))));

        //vector
        PointF vector = new PointF(iniciog.x-inicio.x,iniciog.y-inicio.y);
        //normal del nuevo vector
        PointF normal = new PointF(-vector.y,vector.x);
        normal.set(normal.x/Mates.modulo(normal),normal.y/Mates.modulo(normal));


        iniz.set(iniciog.x-normal.x*tLuz,iniciog.y-normal.y*tLuz);
        inid.set(iniciog.x+normal.x*tLuz,iniciog.y+normal.y*tLuz);

        //cambiar el vector direccion de las rectas
        //creo lo puedo realizar por ser recta un objeto
        //conloque el valor que te pasan es el de un puntero
        luz.getIzRecta().setGrado(iniz,vector);
        luz.getDeRecta().setGrado(inid,vector);
    }

    public float getAngle(){return angle;}

    public void bindBuffers(){

        GLES20.glGenBuffers(1, vbo, 0);
        GLES20.glGenBuffers(1, ibo, 0);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity()
                * 4, vertexBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo[0]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity()
                * 2, indexBuffer, GLES20.GL_STATIC_DRAW);

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
