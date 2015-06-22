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
    public double C;

    private ArrayList recta;

    //Se inicializar todas las variables que se usan
    public Recta(){

        p1 = new PointF();
        p2 = new PointF();

        v = new PointF();
        n = new PointF();

        recta = new ArrayList(3);
        recta.add(n.x);
        recta.add(n.y);
        recta.add(C);

    }



    //debiera de aber constructor a traves de un punto
    //y una direccion na mas
    //metodo para crear la recta directamente, no se que manera es mejor
    public Recta(PointF a,PointF b){

        p1 = new PointF(a.x,a.y);
        p2 = new PointF(b.x,b.y);

        //vector direccion
        v = new PointF(b.x-a.x,b.y-a.y);

        //normal al vector
        double modulo = Math.sqrt(v.y*v.y + v.x * v.x);
        n = new PointF((float)(-v.y/modulo),(float)(v.x/modulo));

        //ecuacion general de la recta Ax+By+C=0
        C = (-n.x*p1.x) - (n.y*p1.y);

        recta = new ArrayList(3);
        recta.add(n.x);
        recta.add(n.y);
        recta.add(C);


    }



    public void setRecta(PointF a,PointF b){

        p1 = a;
        p2 = b;

        //vector direccion
        v.set(b.x - a.x, b.y - a.y);

        //normal al vector
        double modulo = Math.sqrt(v.y*v.y + v.x * v.x);
        n.set((float) (-v.y / modulo), (float) (v.x / modulo));

        //ecuacion general de la recta Ax+By+C=0
        C = (-n.x*p1.x) - (n.y*p1.y);


        recta.set(0, n.x);
        recta.set(1, n.y);
        recta.set(2, C);


    }



    //no mu util
    public ArrayList getRecta() {
        return recta;
    }

    //basicamente a ver si chocan la recta a con el segmente en el cual sea llamado
    public PointF ChoqueRectas(Recta a){

        double determinante = (n.x*a.n.y)-(n.y*a.n.x);
        PointF vuelta = new PointF();


        //el menos es porque cramer usa C al otro lao
        double x = - ((C*a.n.y)-(n.y*a.C)) / determinante;
        double y = - ((n.x*a.C)-(C*a.n.x)) / determinante;

        vuelta.set((float)x,(float)y);

        //para porsi los puntos no estan "ordenados", no es muy eficiente que se diga

        if(min(a.p1.x,a.p2.x)<=x && x<=max(a.p2.x,a.p1.x) && min(a.p2.y,a.p1.y)<=y && y<=max(a.p1.y,a.p2.y)) return vuelta;
        else return null;

    }

    public PointF ChoqueRectas(Recta a,boolean b){

        double determinante = (n.x*a.n.y)-(n.y*a.n.x);
        PointF vuelta = new PointF();


        //el menos es porque cramer usa C al otro lao
        double x = - ((C*a.n.y)-(n.y*a.C)) / determinante;
        double y = - ((n.x*a.C)-(C*a.n.x)) / determinante;

        vuelta.set((float)x,(float)y);

        //para porsi los puntos no estan "ordenados", no es muy eficiente que se diga
        return vuelta;

    }


    //para comprobar cuando da contra la pared, esto seguro va aqui?
    public PointF ChoqueBordes(){


        //pared izquierda, x=0
        double y = -C / n.y;
        if (y>0 && y<MyView.mCanvasHeight && v.x<0) return new PointF(0,(float)y);
        //pared arriva, y=0
        double x = -C / n.x;
        if (x>0 && x<MyView.mCanvasWidth && v.y<0) return  new PointF((float)x,0);
        //pared derecha
        y = (-C - MyView.mCanvasWidth*n.x)/n.y;
        if(v.x>0 && y>0 && y<MyView.mCanvasHeight) return new PointF((float)MyView.mCanvasWidth,(float)y);
        //pared abajo
        x=(-C - MyView.mCanvasHeight*n.y)/n.x;
        return new PointF((float)x,(float)MyView.mCanvasHeight);

        //Error



    }

    /*s
    Solo cuando la recta no es un segmento
    p2 se ignora, quizas abriase de acer un 2 tipo
    de recta
    */
    public void setOrigen(PointF p){

        p1=p;
        p2.set(p1.x+v.x,p1.y+v.y);
        actualizar();

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

        recta.set(0,n.x);
        recta.set(1,n.y);
        recta.set(2,C);




    }

    public void setRecta(){}

    public PointF getP1(){return p1;}
    public PointF getP2(){return p2;}




}


