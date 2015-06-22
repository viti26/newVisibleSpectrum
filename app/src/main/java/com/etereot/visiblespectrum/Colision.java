package com.etereot.visiblespectrum;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 15/01/2015.
 */
public class Colision<T> {
    private List<Objeto> objetos;

    public Colision(){
        List<Objeto> objetos= new ArrayList<Objeto>();
    }

    private class Objeto<T> {
        T geometria;
        List<Point> vertices;

        public void Objeto(T data){
            this.setGeometria(data);
        }

        public void setGeometria(T data){
           this.geometria = data;
        }

        public void setVertices(Point data){
            vertices.add(data);
        }


    }

    public void addObjeto(T geometria){

         objetos.add(new Objeto<T>());

    }
}
