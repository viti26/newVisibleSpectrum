package com.etereot.visiblespectrum;

import android.graphics.PointF;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by victor on 4/06/15.
 */
public class Mates {

    public static double distancia(PointF punto,Recta recta) {return abs(punto.x * recta.n.x + punto.y * recta.n.y + recta.C)/Math.sqrt(recta.n.x*recta.n.x+recta.n.y*recta.n.y);}

    public static double distancia(PointF punto1,PointF punto2){return Math.sqrt(Math.pow((punto2.x-punto1.x),2)+Math.pow((punto2.y-punto1.y),2));}

    public static float modulo(PointF f) {return (float)Math.sqrt(f.x*f.x+f.y*f.y);}

    //n a de ser normalizada
    public static PointF reflect(PointF en,PointF n){return new PointF(en.x-(2*escalar(en,n)*n.x),en.y-(2*escalar(en,n)*n.y));}

    public static float escalar(PointF a,PointF b){return a.x*b.x+a.y*b.y;}

    public static double grado(PointF vector){return Math.atan(vector.y/vector.x);}

    public static PointF mid(PointF a,PointF b){return new PointF((b.x+a.x)/2,(b.y+a.y)/2);}

    public static PointF normalice(PointF vector){return new PointF(vector.x/modulo(vector),vector.y/modulo(vector));}

    //Return the point of the collision if needed
    //The segment must be "b"
    public static PointF linesCollision(Recta a, Recta b, Boolean isSegment){

        float[] r1=a.getRecta();
        float[] r2=b.getRecta();

        float determinante = (r1[0]*r2[1])-(r1[1]*r2[0]);

        //el menos es porque cramer usa C al otro lao
        float x = - ((r1[2]*r2[1])-(r1[1]*r2[2])) / determinante;
        float y = - ((r1[0]*r2[2])-(r1[2]*r2[0])) / determinante;

        if(isSegment && !b.isContained(new PointF(x,y)) )return null;
        return new PointF(x,y);

    }

    public static  float[] concatenate (float[] a, float[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        float[] c = (float[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public static  short[] concatenate (short[] a, short[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        short[] c = (short[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }







}
