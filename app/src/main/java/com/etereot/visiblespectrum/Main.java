package com.etereot.visiblespectrum;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class Main extends Activity {

    private MyView.MyThread mThread;
    private MyView mView;

    private static final int MENU_PAUSE = 1;

    private static final int MENU_RESUME = 2;

    private static final int MENU_START = 3;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_START, 0, R.string.menu_start);
        menu.add(0, MENU_PAUSE, 0, R.string.menu_pause);
        menu.add(0, MENU_RESUME, 0, R.string.menu_resume);


        return true;
    }

    /**
     * Invoked when the user selects an item from the Menu.
     *
     * @param item the Menu entry which was selected
     * @return true if the Menu item was legit (and we consumed it), false
     *         otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_START:
                mThread.doStart();
                return true;
            case MENU_PAUSE:
                mThread.pause();
                return true;
            case MENU_RESUME:
                mThread.unpause();
                return true;

        }

        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.mview_layout);

        super.onCreate(savedInstanceState);
        mView = (MyView)findViewById(R.id.mView);
        mThread = mView.getThread();

        // give the View a handle to the TextView used for messages
        mView.setTextView((TextView) findViewById(R.id.text));


        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            mThread.setState(mThread.STATE_READY);
            Log.w(this.getClass().getName(), "SIS is null");
        } else {
            // we are being restored: resume a previous game
            mThread.restoreState(savedInstanceState);
            Log.w(this.getClass().getName(), "SIS is nonnull");
        }



    }





    @Override
    protected void onPause() {
        mView.getThread().pause();
        super.onPause();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState);
        mThread.saveState(outState);
        Log.w(this.getClass().getName(), "SIS called");
    }


}
