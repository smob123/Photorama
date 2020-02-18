package com.example.photorama.custom_ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * @author Sultan
 * an image view that its width is equal to the height.
 */

class SquareImage : ImageView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
