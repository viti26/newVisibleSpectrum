package com.etereot.visiblespectrum;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by victor on 8/06/15.
 */
public class Estruct implements Comparable<Estruct>{

    public Triangle objeto;
    public double dist;

    Estruct(Triangle objeto,double dist){

        this.objeto = objeto;
        this.dist = dist;
    }

    @Override
    public int compareTo(Estruct other) {
        return this.dist<other.dist ? -1 : 1;
    }





}
