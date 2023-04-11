package com.demeth.massaudioplayer.ui.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * classe utilisée pour détecter les mouvements de doigt gauche droite
 * source: https://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
 */
public abstract class OnSwipeTouchDetector implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    /**
     * @param ctx context of the app
     */
    public OnSwipeTouchDetector (Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    /**
     * when user touch the screen
     * @return if touch event consumed
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        /**
         * détermine le mouvement effectuer
         * @return false if touch event consumed
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    /**
     * évènement glissement vers la droite
     */
    public abstract void onSwipeRight() ;

    /**
     * évènement glissement vers la gauche
     */
    public abstract void onSwipeLeft() ;
}

