package com.demeth.massaudioplayer.frontend.components

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton


/**
 * This class create a button component that always stay square and that can load an image.
 */
class SquareImageButton(context: Context, attrs: AttributeSet?) : AppCompatImageButton(context,attrs) {


    /**
     * resize the button to square view
     * @param widthMeasureSpec width measured
     * @param heightMeasureSpec height measured
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxes = widthMeasureSpec.coerceAtLeast(heightMeasureSpec)

        //super.onMeasure(maxes,maxes);
        setMeasuredDimension(maxes, maxes)
    }
}
