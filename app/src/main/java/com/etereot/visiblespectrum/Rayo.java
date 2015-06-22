package com.etereot.visiblespectrum;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.ArrayList;

/**
 * Created by victor on 5/06/15.
 * Clase con la que generalizar el rayo de luz
 * Luz a de ser el conjunto de todos esos rayos
 * ademas de otras propiedades a modificar
 */
public class Rayo {

    //Geometria del rayo
    private Path path;

    //Geometria de su halo
    private Path halopath;

    private Recta izRecta;
    private Recta deRecta;

    private ArrayList<PointF> puntosdechoqueiz;
    private ArrayList<PointF> puntosdechoquede;

    //mitad grosor luz
    private float tluz;

    //Factor de escalado
    private final float scalar = 5f;
    private final float scalad = 20f;

    //La mierda iluminacion que estoy aciendo
    private Bitmap bmp;

    //Para calcular holopath, fumada y lo se
    private boolean salta=false;

    Rayo(){

        puntosdechoquede = new ArrayList<PointF>(10);
        puntosdechoqueiz = new ArrayList<PointF>(10);

        izRecta = new Recta();
        deRecta = new Recta();

        path = new Path();
        halopath = new Path();

        this.bmp = Luz.bmp;


    }

    Rayo(PointF iniz,PointF inid,PointF vertex){

        puntosdechoquede = new ArrayList<PointF>(10);
        puntosdechoqueiz = new ArrayList<PointF>(10);

        puntosdechoqueiz.add(iniz);
        puntosdechoquede.add(inid);

        izRecta = new Recta();
        deRecta = new Recta();

        izRecta.setGrado(iniz,vertex);
        deRecta.setGrado(inid,vertex);

        path = new Path();
        halopath = new Path();

        this.bmp = Luz.bmp;

        setTluz();


    }




    public void addPuntosdechoqueiz(PointF p){puntosdechoqueiz.add(p);}
    public void addPuntosdechoquede(PointF p){puntosdechoquede.add(p);}

    public void setIzRecta(PointF p,PointF v){izRecta.setGrado(p, v);}
    public void setDeRecta(PointF p,PointF v){deRecta.setGrado(p,v);}

    public void setTluz(){ tluz = (float)Mates.distancia(deRecta.getP1(),izRecta)/2;}

    public void setSalta(boolean salta){this.salta=salta;}

    public PointF izChoqueBordes(){return izRecta.ChoqueBordes();}
    public PointF deChoqueBordes(){return deRecta.ChoqueBordes();}

    public Recta getIzRecta(){return izRecta;}
    public Recta getDeRecta(){return deRecta;}

    public Path getPath(){return path;}
    public Path getHalopath(){return halopath;}

    public float getTluz(){return tluz;}

    public ArrayList<PointF> getPuntosdechoqueiz(){return puntosdechoqueiz;}
    public ArrayList<PointF> getPuntosdechoquede(){return puntosdechoquede;}


    //Calcula el path a traves de los arrays
    public void setPath(){

        PointF geo;
        path.reset();

        path.moveTo(puntosdechoqueiz.get(0).x, puntosdechoqueiz.get(0).y);

        for (int j=1;j<puntosdechoqueiz.size();j++){
            geo=puntosdechoqueiz.get(j);
            path.lineTo(geo.x,geo.y);
        }

        for(int j=puntosdechoquede.size()-1;j>=0;j--){
            geo=puntosdechoquede.get(j);
            path.lineTo(geo.x,geo.y);
        }

        path.close();

    }

    //vacia puntos de la geometria y pon denuevo el inicio
    public void clearchoque(){

        puntosdechoqueiz.clear();
        puntosdechoquede.clear();

        puntosdechoqueiz.add(izRecta.getP1());
        puntosdechoquede.add(deRecta.getP1());


    }

    public void setHalopath(){

        halopath.reset();

        PointF vector = Mates.normalice(izRecta.v);
        PointF normal,izvector,devector;

        //Asi normal siempre es acia la izquierda
        normal = Mates.normalice(new PointF(vector.y,-vector.x));

        izvector = new PointF(vector.x*scalar+normal.x*scalad,vector.y*scalar+normal.y*scalad);
        devector = new PointF(vector.x*scalar-normal.x*scalad,vector.y*scalar-normal.y*scalad);

        halopath.moveTo(puntosdechoqueiz.get(0).x-vector.x*scalar+normal.x*scalad,puntosdechoqueiz.get(0).y-vector.y*scalar+normal.y*scalad);

        for(int i=1;i<puntosdechoqueiz.size();i++){
            if(i==puntosdechoqueiz.size()-1 && salta) halopath.lineTo(puntosdechoqueiz.get(i).x+vector.x,puntosdechoqueiz.get(i).y+vector.y);
            halopath.lineTo(puntosdechoqueiz.get(i).x+izvector.x,puntosdechoqueiz.get(i).y+izvector.y);
        }

        for(int i=puntosdechoquede.size()-1;i>0;i--){
            halopath.lineTo(puntosdechoquede.get(i).x+devector.x,puntosdechoquede.get(i).y+devector.y);
        }

        halopath.lineTo(puntosdechoquede.get(0).x - vector.x * scalar - normal.x * scalad, puntosdechoquede.get(0).y - vector.y * scalar - normal.y * scalad);

        halopath.close();



    }

    public void setBmp(Bitmap bmp) {

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Matrix matrix = new Matrix();

        scaleBmp(width,height,matrix);
        rotateBmp(matrix);
        this.bmp = Bitmap.createBitmap(bmp,0,0,width,height,matrix,false);
    }

    public void rotateBmp(Matrix matrix){ matrix.setRotate((float)Math.toDegrees(Mates.grado(izRecta.v))+ 90); }

    public void scaleBmp(int width,int height,Matrix matrix){

        PointF ini,fin;

        ini = Mates.mid(izRecta.getP1(), deRecta.getP1());
        if(salta){
            Recta recta = new Recta(puntosdechoqueiz.get(puntosdechoqueiz.size() - 2), puntosdechoquede.get(puntosdechoquede.size() - 1));
            fin = recta.ChoqueRectas(new Recta(ini,new PointF(ini.x+izRecta.v.x,ini.y+izRecta.v.y)),true);
        } else fin = puntosdechoqueiz.get(puntosdechoqueiz.size()-1);

        float scaley = (float)Mates.distancia(ini,fin)/height;
        float scalex = tluz*scalad/width;

        matrix.setScale(scalex,scaley);

    }



    public void setPaint(){

        //Pa calcular el punto inicio y final del gradient, lo de salta es por si ay un vertice
        PointF ini = Mates.mid(izRecta.getP1(),deRecta.getP1());
        Recta recta = salta ? new Recta(puntosdechoqueiz.get(puntosdechoqueiz.size() - 1), puntosdechoquede.get(puntosdechoquede.size() - 1)) : new Recta(puntosdechoqueiz.get(puntosdechoqueiz.size() - 1), puntosdechoquede.get(puntosdechoquede.size() - 1));
        PointF fin = recta.ChoqueRectas(new Recta(ini,new PointF(ini.x+izRecta.v.x,ini.y+izRecta.v.y)),true);

        //Shader shader = new LinearGradient(ini.x,ini.y,fin.x,fin.y,)
        //paint


    }

    //public Paint getPaint(){return paint;}

    public void drawRayo(Canvas canvas){
        canvas.drawPath(path,Luz.lPaint);
        canvas.drawPath(halopath,Luz.hPaint);
        //Esto es porque el metodo draw es llamado antes que el update por primera vez con lo que los path vacios no hay problema
        //pero intentar dibujar un bitmap nulo crasea
       //if(bmp!=null) canvas.drawBitmap(bmp, 50, 50,Luz.bPaint);
    }


}
