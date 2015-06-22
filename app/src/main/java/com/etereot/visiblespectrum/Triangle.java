package com.etereot.visiblespectrum;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by Admin on 11/01/2015.
 */
public class Triangle {

    //El pading a cada lado en bmp es 8 izquierda y derecha y 18 arriba y abajo en imagen de 144-144

    private float x,y;

    private final Path path;

    private PointF v1,v2,v3,centro;

    private Recta Lado1,Lado2,Lado3;

    private ArrayList Lados;

    private boolean reflec;

    private Bitmap bmp;

    private float radio,peq;

    private final float cos30= 0.86025403f;
    private final float tg30= 0.577350269f;


    //Todos equilateros, acer un metodo para allar coordenadas a partir de un centro


    //inicializar vertices, la geometria(path) y el array de lados
    public Triangle(Bitmap bmp){

        path = new Path();
        v1 = new PointF();
        v2 = new PointF();
        v3 = new PointF();
        centro = new PointF();

        Lados = new ArrayList(3);

        Lado1 = new Recta();
        Lado2 = new Recta();
        Lado3 = new Recta();

        this.bmp = bmp;

    }

    //dar valor a vertices, lados y la geometria
    public void setTriangle(PointF centro,boolean r){

        float lado=bmp.getWidth()*0.8888f;

        calculatePoints(centro,lado);

        this.centro=centro;


        reflec=r;

        Lado1.setRecta(v1,v2);
        Lado2.setRecta(v3,v2);
        Lado3.setRecta(v3,v1);

        Lados.add(Lado1);
        Lados.add(Lado2);
        Lados.add(Lado3);


        path.moveTo(v1.x, v1.y);
        path.lineTo(v2.x, v2.y);
        path.lineTo(v3.x, v3.y);

        path.close();

        x=centro.x - bmp.getWidth()/ 2;
        y=centro.y - peq - bmp.getHeight()*0.125f;


    }


    //para el on draw
    public void drawTriangle(Canvas canvas,Paint paint) {

        canvas.drawPath(path,paint);
        canvas.drawBitmap(bmp, x,y, null);
    }


    //Devuelve un vertice, segun el int que pases
    public PointF getCoordenadas(int i){
        if (i==0) return v1;
        else if (i==1) return v2;
        else if (i==2) return v3;
        else return null;

    }

    //Devuelve su numero en la lista, para ver su orden respectivo
    public int getNumero(PointF punto) {
        if (punto == v1) return 0;
        else if (punto == v2) return 1;
        else if (punto == v3) return 2;
        else return -1;
    }


    //devuelve el array que contiene los lados
    public ArrayList getLados(){

        return Lados;

    }

    public boolean getReflec(){return reflec;}

    public PointF getCentro(){return centro;}

    private void calculatePoints(PointF centre,float lado){

        float milado = lado/2;
        float peq = tg30 * milado;
        float gran = milado/cos30;

        this.radio = gran;
        this.peq = peq;

        v1.set(centre.x-milado,centre.y-peq);
        v2.set(centre.x,centre.y+gran);
        v3.set(centre.x+milado,centre.y-peq);

    }








}


