package com.etereot.visiblespectrum;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

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

    //grado de giro de la luz
    private double grado;

    //mitad grosor de la luz principal
    private final int tLuz=5;

    private final int radio=30;

    //Rayo de luz principal
    private Rayo luz;

    private ArrayList<Rayo> luces;

    public static Paint lPaint,hPaint,bPaint;

    public static Bitmap bmp;



    public Luz(Bitmap bmp){

        iniz = new PointF();
        inid = new PointF();

        iniciog = new PointF();

        luz = new Rayo();

        luces = new ArrayList<Rayo>();

        this.bmp = bmp;

        lPaint = new Paint();
        lPaint.setARGB(255, 255, 255, 255);
        lPaint.setStrokeWidth(1);
        lPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        lPaint.setAntiAlias(true);
        //lPaint.setShader(shader);

        //pintura del halo
        hPaint = new Paint();
        hPaint.setARGB(50, 255, 255, 255);
        hPaint.setStrokeWidth(1);
        hPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        hPaint.setAntiAlias(true);

        bPaint = new Paint();
        bPaint.setColorFilter(new ColorMatrixColorFilter(getColorMatrix()));




    }

    public void setLuz(PointF origen){

        inicio = origen;

        //angulo inicial
        grado=90;

        iniciog.set(inicio.x,inicio.y-radio);

        iniz.set(iniciog.x-tLuz,iniciog.y);
        inid.set(iniciog.x + tLuz, iniciog.y);

        //Inicio de la geometria de la luz
        luz.addPuntosdechoqueiz(iniz);
        luz.addPuntosdechoquede(inid);


        //dando una direccion inicial
        luz.setIzRecta(iniz, new PointF(0, -50));
        luz.setDeRecta(inid, new PointF(0, -50));

        luz.setTluz();


    }

    public void updateLuz(){
        //reiniciar array de luces
        luces.clear();
        luz.setTluz();
        luz.setSalta(false);
        updateRayo(null, luz);

    }


    //dada una lista de objetos y un rayo, calcula su geometria
    //con colisiones y demas
    private void updateRayo(Triangle objeto,Rayo rayo){

        Triangle choque;

        //Reiniciar puntosdechoquede/iz
        rayo.clearchoque();

        float tLuz = rayo.getTluz();

        //Caso segun arriba-derecha, abajo-derecha, abajo-izquierda y arriva-izquierda
        int caso;

        //distancia segura de que no choca
        float segdist = 2*tLuz + 60;

        if(luces.size()>10) return;


        ArrayList<Estruct> lista = new ArrayList<Estruct>();

        PointF vector = rayo.getDeRecta().v;

        if(vector.y<0){
            if (vector.x>0) caso=1;
            else caso=4;
        }
        else if(vector.x>0){caso=2;} else caso=3;


        //basicamente ir pasando por to los objetos para mirar si chocan
        //cuando cambies lista de objetos, ay que modificarlo

        afuera:

        if(!MyView.Objetos.isEmpty()){

            //crea una lista con las distancias al origen de la luz de cada objeto
            for(int i=0;i<MyView.Objetos.size();i++){ lista.add(new Estruct(MyView.Objetos.get(i),distancia(rayo.getIzRecta().getP1(), MyView.Objetos.get(i).getCentro()))); }

            Collections.sort(lista);

            for(int j=0;j<lista.size();j++){

                choque = lista.get(j).objeto;

                if(choque==objeto) continue;

                //Para no realizar calculos si el objeto esta detras del rayo
                if( (caso==1 || caso == 4) && (choque.getCentro().y>rayo.getIzRecta().getP1().y + segdist)  ) continue;
                if( (caso==2 || caso == 3) && (choque.getCentro().y<rayo.getIzRecta().getP1().y - segdist)  ) continue;
                if( (caso==3 || caso == 4) && (choque.getCentro().x>rayo.getIzRecta().getP1().x + segdist)  ) continue;
                if( (caso==1 || caso == 2) && (choque.getCentro().x<rayo.getIzRecta().getP1().x - segdist)  ) continue;


                //seria mejor calcular si choca uno mirar
                //el otro que es probable que tambien
                //true iz--false de
                //lo ago pero solo amedia creo que podria
                //hacerse mas
                if(!calcula(choque,rayo,true))break afuera;
                if(!calcula(choque,rayo,false)) break afuera;

            }


            PointF a = rayo.izChoqueBordes();
            PointF b = rayo.deChoqueBordes();

            rayo.addPuntosdechoqueiz(a);
            rayo.addPuntosdechoquede(b);


            //se rompe como mueva el inicio de la luz principal
            if(a.x==0 && b.y==0) {
                rayo.addPuntosdechoqueiz(new PointF(0, 0));
            } else if (a.y==0 && b.x==inicio.x*2){
                rayo.addPuntosdechoquede(new PointF(inicio.x * 2, 0));
            }

        }

        rayo.setPath();
        rayo.setHalopath();
        rayo.setBmp(bmp);

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
        uno=linea.ChoqueRectas(Lados.get(0));
        numero=0;

        for(int i=1;i<3;i++){
            dos=linea.ChoqueRectas(Lados.get(i));

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
                updateRayo(objeto,a);
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
                    updateRayo(objeto,a);
                    luces.add(a);
                }

                numero=objeto.getNumero(vertice);
            }
            else {
                //es seguro que el otro lado choque,y se calcula siempre iz primero
                PointF b=rayo.getDeRecta().ChoqueRectas(Lados.get(numero));
                rayo.addPuntosdechoquede(b);

                //si el objeto refleja, comenzar a crear el nuevo rayo
                if(reflec) {
                    a = new Rayo(b,uno,reflect(linea.v,Lados.get(numero).n));
                    updateRayo(objeto,a);
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
                    updateRayo(objeto,a);
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

            PointF b=rayo.getDeRecta().ChoqueRectas(Lados.get(numero));
            rayo.addPuntosdechoquede(b);

            //si el objeto refleja, comenzar a crear el nuevo rayo con el punto
            //de choque y el vertice del rayo que acabamos de calcular
            if(reflec) {
                a = new Rayo(b,puntosdechoque.get(puntosdechoque.size()-1),reflect(linea.v,Lados.get(numero).n));

                updateRayo(objeto,a);
                luces.add(a);
            }

            rayo.setSalta(true);
            return false;

        }

        //el punto esta por detras, no cuenta y se ignora
        //else linea.setOrigen(puntosdechoque.get(puntosdechoque.size() - 1));
        PointF normalp = Mates.normalice(new PointF(rayo.getIzRecta().v.y, -rayo.getIzRecta().v.x));
        PointF j = puntosdechoque.get(puntosdechoque.size()-1);
        Recta normal = new Recta(j, new PointF(j.x+normalp.x,j.y+normalp.y));

        if(lado){
            rayo.addPuntosdechoquede(rayo.getDeRecta().ChoqueRectas(normal,true));
        } else {
            rayo.addPuntosdechoqueiz(rayo.getIzRecta().ChoqueRectas(normal,true));
        }

        a = new Rayo(rayo.getPuntosdechoqueiz().get(rayo.getPuntosdechoqueiz().size()-1),rayo.getPuntosdechoquede().get(rayo.getPuntosdechoquede().size()-1),rayo.getIzRecta().v);
        updateRayo(objeto, a);
        luces.add(a);

        return false;
    }


    //dibuja las luces con su halo
    public void drawLuz(Canvas canvas){
        luz.drawRayo(canvas);
        for(int i=0;i<luces.size();i++){luces.get(i).drawRayo(canvas);}
    }


    //siempre se aplica al rayo principal
    public void setDirection(double cambio){


        grado= grado + cambio;

        iniciog.set((float)(inicio.x+radio*Math.cos(Math.toRadians(grado))),(float)(inicio.y-radio*Math.sin(Math.toRadians(grado))));

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

    private ColorMatrix getColorMatrix(){
        //Disminuye la trasparencia
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
                1, 0, 0, 0,0,
                0, 1, 0, 0,0,
                0, 0, 1, 0,0,
                0, 0, 0, 1,-50,
        });

        return colorMatrix;
    }





}
