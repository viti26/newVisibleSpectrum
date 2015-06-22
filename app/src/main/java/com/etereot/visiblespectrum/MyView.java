package com.etereot.visiblespectrum;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.lang.Object;
import java.util.ArrayList;


class MyView extends SurfaceView implements SurfaceHolder.Callback {
    class MyThread extends Thread {




        private Handler mHandler;

        private SurfaceHolder mSurfaceHolder;

        /*
         * State-tracking constants
         */

        public static final int STATE_PAUSE = 1;
        public static final int STATE_READY =2;
        public static final int STATE_RUNNING = 3;


        private int mMode;

        private boolean mRun = false;

        private final Object mRunLock = new Object();



        private Paint mPaint,cPaint;


        private Triangle mTriangle,rTriangle,kTriangle,oTriangle;


        private Luz mLuz;



        //coordenadas iniciales de la luz

        private PointF iLuz;

        private double iLuzx,iLuzy;


        private float lastx,lasty;


        //sensibilidad del control
        private final float escala=0.1f;

        //para ver si es necesario recalcular o no
        private boolean change;









        public MyThread(SurfaceHolder surfaceHolder, Context context,
                        Handler handler) {

            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;


            //Imagenes
            Bitmap t,l;
            Resources r=getResources();
            t = BitmapFactory.decodeResource(r,R.drawable.plano);
            l = BitmapFactory.decodeResource(r,R.drawable.halo);
            t = getResizedBitmap(t, t.getHeight(), t.getWidth(), 180);


            //Orden importante, en contra de las agujas del reloj
            mTriangle = new Triangle(t);
            rTriangle = new Triangle(t);
            kTriangle = new Triangle(t);
            oTriangle = new Triangle(t);


            iLuz = new PointF();

            mLuz = new Luz(l);


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







            /**Array para guardar todo objeto creado para ver las colisiones,
             * mas tarde debiera de ser un arbol que colocara cada objeto
             * segun el cuadrante que le toque
             */
            Objetos = new ArrayList(4);
            Objetos.add(mTriangle);
            Objetos.add(rTriangle);
            Objetos.add(kTriangle);
            Objetos.add(oTriangle);


            change=true;





        }



        public void doStart(){
            synchronized (mSurfaceHolder) {

                setState(STATE_RUNNING);

            }

        }

        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
            }
        }

        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         *
         * @param savedState Bundle containing the game state
         */
        public synchronized void restoreState(Bundle savedState) {


        }

        public Bundle saveState(Bundle map){
            synchronized (mSurfaceHolder) {

            }

            return map;

        }

        @Override
        public void run() {

            /*para instaurar por primera vez la geometria antes
            de que se llame a onDraw, sino se generaria un error
             */
            this.setGeometry();

            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        if (mMode == STATE_RUNNING) updatePhysics();
                        //  Do not allow mRun to be set false until
                        // we are sure all canvas draw operations are complete.
                        //
                        // If mRun has been toggled false, inhibit canvas operations.
                        synchronized (mRunLock) {
                            if (mRun) doDraw(c);
                        }
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        private void setGeometry(){

            mTriangle.setTriangle(new PointF(50,50),true);
            rTriangle.setTriangle(new PointF(350,50),true);
            kTriangle.setTriangle(new PointF(200,100),true);
            oTriangle.setTriangle(new PointF(50,200),true);


            //punto abajo centro, donde sale la luz
            iLuzx = mCanvasWidth/2;
            iLuzy = mCanvasHeight - 10;
            iLuz.set((float)iLuzx,(float)iLuzy);

            mLuz.setLuz(iLuz);


        }



        private void setDirection(double grado){
            mLuz.setDirection(grado);
            change=true;
        }



        /**
         * Sets the game mode.
         */
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        public void setState(int mode, CharSequence message) {
            /*
             *  We cant touch the view, so
             * Instead we use a Message + Handler to relay commands to the main
             * thread, which updates the user-text View.
             */
            synchronized (mSurfaceHolder) {
                mMode = mode;

                if (mMode == STATE_RUNNING) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", "");
                    b.putInt("viz", View.INVISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                } else {


                    Resources res = mContext.getResources();
                    CharSequence str = "";
                    if (mMode == STATE_READY)
                        str = res.getText(R.string.mode_ready);
                    else if (mMode == STATE_PAUSE)
                        str = res.getText(R.string.mode_pause);
                    if (message != null) {
                        str = message + "\n" + str;
                    }



                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", str.toString());
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
        }



        public void setRunning(boolean b) {
            // Do not allow mRun to be modified while any canvas operations
            // are potentially in-flight..
            synchronized (mRunLock) {
                mRun = b;
            }
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;



            }
        }



        public void unpause(){

        }


        public void doDraw(Canvas canvas){


            canvas.drawColor(0xFF404050);

            for(int i=0;i<Objetos.size();i++){
                Objetos.get(i).drawTriangle(canvas, mPaint);
            }

            mLuz.drawLuz(canvas);
            canvas.drawCircle(mCanvasWidth / 2, mCanvasHeight, 30, cPaint);

        }

        public void updatePhysics(){

            if(change) mLuz.updateLuz();
            change = false;

        }


        public boolean onTouchEvent(MotionEvent e) {

            float x=e.getX();
            float y=e.getY();

            float dx=-(x-lastx);
            float dy=y-lasty;

            // reverse direction of rotation to left of the mid-line
            //if (x > mCanvasWidth / 2) {
            //    dy = dy * -1 ;
            //}

            //setDirection((dx+dy)*escala);

            //sencillo para pruebas
            if(x>mCanvasWidth/2) setDirection(-0.2);
            else setDirection(0.2);

            lastx = x;
            lasty = y;
            return true;
        }
    }

    public static int mCanvasHeight = 1;

    public static int mCanvasWidth = 1;

    //lista de objetos para calcular colisiones
    //debe cambiarse por un arbol separado por cuadrantes, etc
    public static ArrayList<Triangle> Objetos;
    public static TreeNode<Triangle> objetos;


    private Context mContext;

    /** Pointer to the text view to display "Paused.." etc. */
    private TextView mStatusText;

    /** The thread that actually draws the animation */
    private MyThread thread;

    public MyView(Context context, AttributeSet attrs){

        super(context,attrs);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new MyThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                //Aunque lo parezca no es un error
                mStatusText.setVisibility(m.getData().getInt("viz"));
                mStatusText.setText(m.getData().getString("text"));
            }
        });

        setFocusable(true);

    }

    public MyThread getThread() {
        return thread;
    }


    /**
     * Standard override to get key-press events.
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return thread.onTouchEvent(e);
    }


    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
    }

    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView) {
        mStatusText = textView;
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        thread.setSurfaceSize(width, height);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        thread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
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




    //public void orden(ArrayList<Triangle> objetos){

    //    this.objetos = new TreeNode<Triangle>(objetos.get(0));


    //    for(int i=1;i<objetos.size();i++){
    //        busca(objetos.get(i));
    //    }



    //}

    //private void busca(Triangle objeto){

    //    if(objetos.)

    //}






}



