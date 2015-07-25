package com.etereot.visiblespectrum;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by Admin on 15/01/2015.
 */


public class Recta{


    private PointF p1;
    private PointF p2;

    //debieran de ser privadas y crear un metodo para accederlas
    public PointF v;
    public PointF n;
    public float C;

    private float[] recta;

    //Initializing variables
    public Recta(){

        p1 = new PointF();
        p2 = new PointF();
        v = new PointF();
        n = new PointF();

        recta = new float[3];

    }


    public Recta(float x1,float y1,float x2,float y2){

        p1 = new PointF(x1,y1);
        p2 = new PointF(x2,y2);

        //vector direccion
        v = new PointF(x2-x1,y2-y1);

        //normal al vector
        double modulo = Math.sqrt(v.y*v.y + v.x * v.x);
        n = new PointF((float)(-v.y/modulo),(float)(v.x/modulo));

        //ecuacion general de la recta Ax+By+C=0
        C = (-n.x*p1.x) - (n.y*p1.y);

        recta = new float[3];
        recta[0] =n.x;
        recta[1] =n.y;
        recta[2] =C;


    }


    //para comprobar cuando da contra la pared, esto seguro va aqui?
    public PointF ChoqueBordes(){

        //Bad done, it could be a single calculation with a general equation
        //pared izquierda, x=-1
        double y = (-C+n.x) / n.y;
        if (y>-1 && y<1 && v.x<0) return new PointF(-1,(float)y);
        //pared arriva, y=1
        double x = (-C-n.y) / n.x;
        if (x>-1 && x<1 && v.y>0) return  new PointF((float)x,1);
        //pared derecha x=1
        y = (-C - n.x)/n.y;
        if(v.x>0 && y>-1 && y<1) return new PointF(1,(float)y);
        //pared abajo y=-1
        x=(-C + n.y)/n.x;
        return new PointF((float)x,-1);

        //Error
    }



    //nuevo origen, nuevo segundo punto y vector(ineficiente como siempre)
    public void setGrado(PointF p,PointF vector){

        p1=p;
        p2.set(p1.x+vector.x,p2.y+vector.y);
        v.set(vector.x,vector.y);

        actualizar();

    }

    //actualizas normal y C, abrias de aver cambiado los puntos y el vector ya
    private void actualizar(){

        //normal al vector
        double modulo = Math.sqrt(v.y*v.y + v.x * v.x);
        n.set((float)(-v.y/modulo),(float)(v.x/modulo));

        //ecuacion general de la recta
        C = (-n.x*p1.x) - (n.y*p1.y);

        recta[0] =n.x;
        recta[1] =n.y;
        recta[2] =C;




    }

    public float[] getRecta(){return recta;}

    public PointF getP1(){return p1;}
    public PointF getP2(){return p2;}

    public boolean isContained(PointF v){

        //This could be easier if the vertex were ordered
        if(min(p1.x,p2.x)<=v.x && v.x<=max(p2.x,p1.x) && min(p2.y,p1.y)<=v.y && v.y<=max(p1.y,p2.y)) return true;
        return false;

    }




}


