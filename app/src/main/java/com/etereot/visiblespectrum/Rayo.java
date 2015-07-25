package com.etereot.visiblespectrum;

import android.graphics.PointF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Created by victor on 5/06/15.
 * Clase con la que generalizar el rayo de luz
 * Luz a de ser el conjunto de todos esos rayos
 * ademas de otras propiedades a modificar
 */
public class Rayo {

    private Recta izRecta;
    private Recta deRecta;

    private ArrayList<PointF> puntosdechoqueiz;
    private ArrayList<PointF> puntosdechoquede;

    //mitad grosor luz
    private float tluz;

    private float rayCoords[];
    private short drawOrder[];

    Rayo() {

        puntosdechoquede = new ArrayList<PointF>(10);
        puntosdechoqueiz = new ArrayList<PointF>(10);

        izRecta = new Recta();
        deRecta = new Recta();

    }

    Rayo(PointF iniz, PointF inid, PointF vertex) {

        puntosdechoquede = new ArrayList<PointF>(10);
        puntosdechoqueiz = new ArrayList<PointF>(10);

        puntosdechoqueiz.add(iniz);
        puntosdechoquede.add(inid);

        izRecta = new Recta();
        deRecta = new Recta();

        izRecta.setGrado(iniz, vertex);
        deRecta.setGrado(inid, vertex);

        setTluz();


    }


    public void addPuntosdechoqueiz(PointF p) {
        puntosdechoqueiz.add(p);
    }

    public void addPuntosdechoquede(PointF p) {
        puntosdechoquede.add(p);
    }

    public void setIzRecta(PointF p, PointF v) {
        izRecta.setGrado(p, v);
    }

    public void setDeRecta(PointF p, PointF v) {
        deRecta.setGrado(p, v);
    }

    public void setTluz() {
        tluz = (float) Mates.distancia(deRecta.getP1(), izRecta) / 2;
    }

    public PointF izChoqueBordes() {
        return izRecta.ChoqueBordes();
    }

    public PointF deChoqueBordes() {
        return deRecta.ChoqueBordes();
    }

    public Recta getIzRecta() {
        return izRecta;
    }

    public Recta getDeRecta() {
        return deRecta;
    }

    public float getTluz() {
        return tluz;
    }

    public ArrayList<PointF> getPuntosdechoqueiz() {
        return puntosdechoqueiz;
    }

    public ArrayList<PointF> getPuntosdechoquede() {
        return puntosdechoquede;
    }

    //Cool for every ray except for the first one, it is nor deleted so
    //creating new buffers every time is not efficient or maybe it is
    //because before you do not knot their size
    public void setCoords(){

        int size_iz = puntosdechoqueiz.size();
        int size_de = puntosdechoquede.size();
        int sum_size = size_de + size_iz;

        rayCoords= new float[sum_size*2];

        //this sets the coordinates of the ray
        for(int i=0;i<size_de;i++){
            rayCoords[i*2]=puntosdechoquede.get(i).x;
            rayCoords[(i*2)+1]=puntosdechoquede.get(i).y;
        }
        for(int j=0;j<size_iz;j++){
            rayCoords[(j+size_de)*2]=puntosdechoqueiz.get(size_iz-j-1).x;
            rayCoords[(j+size_de)*2+1]=puntosdechoqueiz.get(size_iz-j-1).y;
        }

        //this sets the drawing order, its easier this way
        if(sum_size==4) drawOrder = new short[]{0,1,2,0,2,3};
        else if(sum_size==5) drawOrder = new short[]{0,1,2,0,2,4,2,3,4};
        else if(size_de==2) drawOrder = new short[]{0,1,3,1,2,3,0,3,5,3,4,5};
        else drawOrder = new short[]{0,1,2,2,3,4,0,2,5,2,4,5};
    }



    //vacia puntos de la geometria y pon denuevo el inicio
    public void clearchoque() {

        puntosdechoqueiz.clear();
        puntosdechoquede.clear();
        puntosdechoqueiz.add(izRecta.getP1());
        puntosdechoquede.add(deRecta.getP1());

    }


    public short[] getdrawOrder(){return drawOrder;}
    public float[] getRayCoords(){return rayCoords;}



}


