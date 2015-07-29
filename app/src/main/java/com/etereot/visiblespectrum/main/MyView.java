package com.etereot.visiblespectrum.main;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.etereot.visiblespectrum.R;
import com.etereot.visiblespectrum.geometry.Triangle;

import java.util.ArrayList;


class MyView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;


    private Paint mPaint,cPaint,blurpaint,iluminePaint;

    //coordenadas iniciales de la luz
    private PointF iLuz;
    private double iLuzx,iLuzy;

    //Para dibujar los rayos y la iluminacion
    private Bitmap bmp;
    private Canvas ucanvas;

    //Background
    private Bitmap fracture;

    public static int mCanvasHeight = 1;
    public static int mCanvasWidth = 1;

    //lista de objetos para calcular colisiones
    //debe cambiarse por un arbol separado por cuadrantes, etc
    public static ArrayList<Triangle> Objetos;

    private final float TOUCH_SCALE_FACTOR = 0.4f;
    private float mPreviousX;
    private float mPreviousY;




    public MyView(Context context){

        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }







    private void a(){
            //Imagenes
            Bitmap t;
            Resources r=getResources();
            t = BitmapFactory.decodeResource(r, R.drawable.plano);
            t = getResizedBitmap(t, t.getHeight(), t.getWidth(), 180);

            fracture = BitmapFactory.decodeResource(r, R.drawable.grietas);




            //Orden importante, en contra de las agujas del reloj


            mPaint = new Paint();
            mPaint.setARGB(200, 0, 0, 255);
            mPaint.setStrokeWidth(1);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setAntiAlias(true);


            cPaint = new Paint();
            cPaint.setARGB(200, 255, 0, 0);
            cPaint.setStrokeWidth(1);
            cPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            cPaint.setAntiAlias(true);

            blurpaint = new Paint();
            blurpaint.setAlpha(200);
            blurpaint.setColorFilter(new ColorMatrixColorFilter(contrast(1.75f, -40)));

            iluminePaint = new Paint();
            iluminePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));



            /**Array para guardar todo objeto creado para ver las colisiones,
             * mas tarde debiera de ser un arbol que colocara cada objeto
             * segun el cuadrante que le toque
             */
            Objetos = new ArrayList(6);
            //Objetos.add(mTriangle);






        }











    private void setGeometry(){

        /*mTriangle.setTriangle(new PointF(50,50),true);
        rTriangle.setTriangle(new PointF(350,50),true);
        kTriangle.setTriangle(new PointF(200,100),true);
        oTriangle.setTriangle(new PointF(50,200),true);
        aTriangle.setTriangle(new PointF(350,400),true);
        bTriangle.setTriangle(new PointF(70,350),true);
        */

        fracture = getResizedBitmap(fracture, mCanvasHeight,mCanvasWidth, 0);


        //punto abajo centro, donde sale la luz
        iLuzx = mCanvasWidth/2;
        iLuzy = mCanvasHeight - 10;
        iLuz.set((float) iLuzx, (float) iLuzy);

        //mLuz.setLuz(iLuz);


    }




    public void doDraw(Canvas canvas){

        //if(bmp==null){
        bmp= Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        ucanvas = new Canvas(bmp);
        //}
        //Clear the screen
        canvas.drawColor(0xFF404050);

        //Clear the canvas
        ucanvas.drawColor(0xFF303030);
        //Draw sources of light
        //mLuz.drawLuz(ucanvas);
        //downscale and blur the bitmap
        bmp=downscale(bmp);

        //draw blurred bitmap
        canvas.drawBitmap(bmp, 0, 0, blurpaint);
        //draw in the backgroud
        canvas.drawBitmap(fracture, 0, 0,iluminePaint);

        //draw every object
        for (int i = 0; i < Objetos.size();i++){
            //Objetos.get(i).drawTriangle(canvas, mPaint);
        }
        canvas.drawCircle(mCanvasWidth / 2, mCanvasHeight, 30, cPaint);


        //draw rays
        //mLuz.drawLuz(canvas);


    }

    private Bitmap downscale(Bitmap bmp){
        bmp = Bitmap.createScaledBitmap(bmp,(int)(bmp.getWidth()*0.5),(int)(bmp.getHeight()*0.5),true);
        //around 15 light looks good
        fastblur(bmp, 15);
        bmp = Bitmap.createScaledBitmap(bmp,(int)(bmp.getWidth()*2),(int)(bmp.getHeight()*2), true);
        return bmp;
    }

    private ColorMatrix contrast(float contrast,float brightness){
         ColorMatrix cm = new ColorMatrix(new float[]
                 {
                         contrast, 0, 0, 0, brightness,
                         0, contrast, 0, 0, brightness,
                         0, 0, contrast, 0, brightness,
                         0, 0, 0, 1, 0
                 }
         );

        return cm;
    }

    // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
    public void fastblur(Bitmap bitmap, int radius) {

        //radius better should not be less than 1

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];

        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth, int angle) {

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        //Giralo los grados necesarios
        matrix.postRotate(angle);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }

                float new_angle =mRenderer.getAngle() +
                        ((dx + dy) * TOUCH_SCALE_FACTOR);

                if(new_angle>=175) new_angle = 175;
                if(new_angle<=5) new_angle = 5;

                mRenderer.setAngle(new_angle);
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }




}



