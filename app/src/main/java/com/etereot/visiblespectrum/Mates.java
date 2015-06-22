package com.etereot.visiblespectrum;

import android.graphics.PointF;

import java.util.ArrayList;

import static java.lang.Math.abs;

/**
 * Created by victor on 4/06/15.
 */
public class Mates {

    public static double distancia(PointF punto,Recta recta) {return abs(punto.x * recta.n.x + punto.y * recta.n.y + recta.C)/Math.sqrt(recta.n.x*recta.n.x+recta.n.y*recta.n.y);}

    public static double distancia(PointF punto1,PointF punto2){return Math.sqrt(Math.pow((punto2.x-punto1.x),2)+Math.pow((punto2.y-punto1.y),2));}

    public static float modulo(PointF f) {return (float)Math.sqrt(f.x*f.x+f.y*f.y);}

    //n a de ser normalizada
    public PointF reflect(PointF en,PointF n){return new PointF(en.x-(2*escalar(en,n)*n.x),en.y-(2*escalar(en,n)*n.y));}

    public float escalar(PointF a,PointF b){return a.x*b.x+a.y*b.y;}

    public static double grado(PointF vector){return Math.atan(vector.y/vector.x);}

    public static PointF mid(PointF a,PointF b){return new PointF((b.x+a.x)/2,(b.y+a.y)/2);}

    public static PointF normalice(PointF vector){return new PointF(vector.x/modulo(vector),vector.y/modulo(vector));}





}
