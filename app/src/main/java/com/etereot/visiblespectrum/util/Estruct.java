package com.etereot.visiblespectrum.util;

import com.etereot.visiblespectrum.geometry.Triangle;

/**
 * Created by victor on 8/06/15.
 */
public class Estruct implements Comparable<Estruct>{

    public Triangle objeto;
    public double dist;

    public Estruct(Triangle objeto,double dist){

        this.objeto = objeto;
        this.dist = dist;
    }

    @Override
    public int compareTo(Estruct other) {
        return this.dist<other.dist ? -1 : 1;
    }





}
