package com.etereot.visiblespectrum.geometry;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;

import com.etereot.visiblespectrum.util.Mates;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by Admin on 15/01/2015.
 */


public class Recta{

    //Ax+By+C=0---A= n.x, B= n.y

    private PointF p1;
    private PointF p2;

    //debieran de ser privadas y crear un metodo para accederlas
    private PointF v;

    private float[] recta;

    //Initializing variables
    public Recta(){

        p1 = new PointF();
        p2 = new PointF();
        v = new PointF();

        recta = new float[3];

    }



    public Recta(float x1,float y1,float x2,float y2){

        p1 = new PointF(x1,y1);
        p2 = new PointF(x2,y2);

        //vector direccion
        v = new PointF(x2-x1,y2-y1);

        PointF n = Mates.normal(v);
        n.set(-n.x,-n.y);

        //ecuacion general de la recta Ax+By+C=0
        float C = (-n.x*p1.x) - (n.y*p1.y);

        recta = new float[3];
        recta[0] =n.x;
        recta[1] =n.y;
        recta[2] =C;


    }



    public void setGrado(PointF p,PointF vector){

        p1=p;
        p2.set(p1.x+vector.x,p2.y+vector.y);
        v = vector;

        actualizar();

    }

    //actualizas normal y C, abrias de aver cambiado los puntos y el vector ya
    private void actualizar(){

        PointF n = Mates.normal(v);

        //ecuacion general de la recta
        float C = (-n.x*p1.x) - (n.y*p1.y);

        recta[0] =n.x;
        recta[1] =n.y;
        recta[2] =C;
    }

    public float[] getRecta(){return recta;}

    public PointF getP1(){return p1;}
    public PointF getP2(){return p2;}

    public PointF getVector(){return v;}
    public PointF getNormal(){return new PointF(recta[0],recta[1]);}

    public boolean isContained(PointF v){

        //This could be easier if the vertex were ordered
        if(min(p1.x,p2.x)<=v.x && v.x<=max(p2.x,p1.x) && min(p2.y,p1.y)<=v.y && v.y<=max(p1.y,p2.y)) return true;
        return false;

    }




}


