package com.etereot.visiblespectrum.util;

import android.graphics.PointF;
import android.util.Log;

import com.etereot.visiblespectrum.geometry.Recta;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by victor on 4/06/15.
 */
public class Mates {

    public static final Recta left = new Recta(-1f,1f,-1f,-1f);
    public static final Recta right = new Recta(1f,-1f,1f,1f);
    public static final Recta up = new Recta(1f,1f,-1f,1f);
    public static final Recta down = new Recta(-1f,-1f,1f,-1f);


    public static String TAG = "Math";

    public static double distancia(PointF punto,Recta recta) {
        if(punto==null) return -1;
        if(recta==null) return -2;
        float line_coomp[] = recta.getRecta();
        double distance = abs(punto.x * line_coomp[0] + punto.y * line_coomp[1] + line_coomp[2])/Math.sqrt(line_coomp[0]*line_coomp[0]+line_coomp[1]*line_coomp[1]);
        return distance;
    }

    public static double distancia(PointF punto1,PointF punto2){return Math.sqrt(Math.pow((punto2.x-punto1.x),2)+Math.pow((punto2.y-punto1.y),2));}

    public static float modulo(PointF f) {return (float)Math.sqrt(f.x*f.x+f.y*f.y);}

    public static PointF reflect(PointF entry_vector,PointF normal){return new PointF(entry_vector.x-(2*escalar(entry_vector,normal)*normal.x),entry_vector.y-(2*escalar(entry_vector,normal)*normal.y));}

    public static float escalar(PointF a,PointF b){return (a.x*b.x)+(a.y*b.y);}

    public static double grado(PointF vector){return Math.atan(vector.y/vector.x);}

    public static PointF mid(PointF a,PointF b){return new PointF((b.x+a.x)/2,(b.y+a.y)/2);}

    public static PointF normalice(PointF vector){return new PointF(vector.x/modulo(vector),vector.y/modulo(vector));}

    //Return the point of the collision if needed
    //The segment must be "b"
    public static PointF linesCollision(Recta a, Recta b, Boolean isSegment){

        float[] r1=a.getRecta();
        float[] r2=b.getRecta();

        float determinante = (r1[0]*r2[1])-(r1[1]*r2[0]);
        if(determinante==0) return null;

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

    public static PointF normal(PointF v){return normalice(new PointF(-v.y,v.x));}

    public static short[] sum (short[] array,int i){
        for(int e=0;e<array.length;e++) array[e]=(short)(array[e] + i);
        return array;
    }

    public static PointF edgeCollision(Recta line){

        PointF vertex = Mates.linesCollision(line, left, true);
        if(vertex!=null) return vertex;

        vertex = Mates.linesCollision(line, up, true);
        if(vertex!=null) return vertex;

        vertex = Mates.linesCollision(line, right, true);
        if(vertex!=null) return vertex;

        vertex = Mates.linesCollision(line, down, true);
        if(vertex!=null) return vertex;

        if(DebugConfig.ON) Log.e(TAG,"Error in edge collision");
        return null;

    }

    public static <T> T getLast(ArrayList<T> array){return array.get(array.size()-1);}







}
