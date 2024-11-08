package com.demeth.massaudioplayer.frontend.components;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class create a button component that always stay square and that can load an image.
 */
public class SquareImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    public SquareImageButton(@NonNull Context context) {
        super(context);
    }

    public SquareImageButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * resize the button to square view
     * @param widthMeasureSpec width measured
     * @param heightMeasureSpec height measured
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxes = Math.max(widthMeasureSpec,heightMeasureSpec);

        //super.onMeasure(maxes,maxes);
        setMeasuredDimension(maxes, maxes);
    }
}
