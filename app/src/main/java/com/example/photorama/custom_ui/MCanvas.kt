package com.example.photorama.custom_ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat

/**
 * @author Sultan
 * handles cropping an image based on given dimensions
 * @param context the app's context
 * @param attributeSet view's attribute set
 * @param bitmap the image we want to crop
 * @param MAX_WIDTH the maximum allowed with for the image
 * @param MAX_HEIGHT the maximum allowed height allowed for the image
 */

class MCanvas(
    context: Context,
    attributeSet: AttributeSet?,
    private val bitmap: Bitmap,
    MAX_WIDTH: Int,
    MAX_HEIGHT: Int
) :
    View(context, attributeSet), GestureDetector.OnGestureListener {
    // left side overlay
    private val leftRect = Rect()
    // right side overlay
    private val rightRect = Rect()
    // top overlay
    private val topRect = Rect()
    // bottom overlay
    private val bottomRect = Rect()
    // the image's current x position
    private var px = 0f
    // the image's y current position
    private var py = 0f
    // the middle x posint of the canvas
    private var rectX = 0f
    // the middle y point of the canvas
    private var rectY = 0f
    // the width of the image
    private var RECT_WIDTH = MAX_WIDTH
    // the height of the image
    private var RECT_HEIGHT = MAX_HEIGHT
    // the colour of the overlays
    private val overlay = Paint()
    // check if the user has touched the canvas
    private var userTouched = false
    private val mDetector: GestureDetectorCompat = GestureDetectorCompat(context, this)

    init {
        // set the colour of the overlays
        overlay.color = Color.argb(150, 0, 0, 0)
        overlay.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // stop the image from going out of the overlay bounds
        if (RECT_WIDTH > width) {
            RECT_WIDTH = width
        }

        if (RECT_HEIGHT > height) {
            RECT_HEIGHT = height
        }

        // get the middle x, and y points
        rectX = ((width / 2) - (RECT_WIDTH / 2)).toFloat()
        rectY = ((height / 2) - (RECT_HEIGHT / 2)).toFloat()

        // update the image's position if the user has touched the canvas
        if (!userTouched) {
            px = ((width / 2) - (bitmap.width / 2)).toFloat()
            py = ((height / 2) - (bitmap.height / 2)).toFloat()
        }

        // display the overlays to cover the sides, and only show a square in the middle,
        // and its dimensions have to equal the max width, and max height
        leftRect.set(0, rectY.toInt(), rectX.toInt(), (rectY + RECT_HEIGHT).toInt())
        rightRect.set(
            (rectX + RECT_WIDTH).toInt(),
            rectY.toInt(),
            width,
            (rectY + RECT_HEIGHT).toInt()
        )
        topRect.set(0, 0, width, rectY.toInt())
        bottomRect.set(
            0,
            (rectY + RECT_HEIGHT).toInt(),
            width,
            height
        )

        // draw the image, and overlays on top of it
        canvas.drawBitmap(bitmap, px, py, null)
        canvas.drawRect(leftRect, overlay)
        canvas.drawRect(rightRect, overlay)
        canvas.drawRect(topRect, overlay)
        canvas.drawRect(bottomRect, overlay)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mDetector.onTouchEvent(event)) {
            invalidate()
            return true
        }

        return super.onTouchEvent(event)
    }

    override fun onScroll(
        oldEvent: MotionEvent?,
        newEvent: MotionEvent?,
        xScrollAmount: Float,
        yScrollAmount: Float
    ): Boolean {
        // if the user has not touched the screen before, then change userTouched to true
        if (!userTouched) {
            userTouched = true
        }

        // calculate the new x, and y positions
        val newX = px - xScrollAmount
        val newY = py - yScrollAmount

        // if the new positions will take the image outside of the canvas return false
        if (withingRectX(newX)) {
            px = newX
        }

        if (withinRectY(newY)) {
            py = newY
        }

        return true
    }

    private fun withingRectX(newX: Float): Boolean {
        return newX <= rectX && (newX + bitmap.width) >= (rectX + RECT_WIDTH)
    }

    private fun withinRectY(newY: Float): Boolean {
        return newY <= rectY && (newY + bitmap.height) >= (rectY + RECT_HEIGHT)
    }

    /**
     * creates a new cropped version of the bitmap based on the user's positioning
     * @return returns a cropped version of the bitmap passed to this view
     */
    fun cropBitmap(): Bitmap {
        // the x position of the image to be cropped
        val xPos: Int
        // the y position of the image to be cropped
        val yPos: Int
        // the cropped image's width
        val imgWidth: Int
        // the cropped image's height
        val imgHeight: Int

        // calculate whether the x position of the image is larger than the overlay bound's x
        // position
        xPos = if (rectX >= px) {
            (rectX - px).toInt()
        } else {
            0
        }

        // calculate whether the y position of the image is larger than the overlay bound's y
        // position
        yPos = if (rectY >= py) {
            (rectY - py).toInt()
        } else {
            0
        }

        // calculate whether the width of the image is larger than the overlay bound's width

        if (bitmap.width > RECT_WIDTH) {
            imgWidth = RECT_WIDTH
        } else {
            imgWidth = bitmap.width
        }

        // calculate whether the height of the image is larger than the overlay bound's height
        if (bitmap.height > RECT_HEIGHT) {
            imgHeight = RECT_HEIGHT
        } else {
            imgHeight = bitmap.height
        }

        return Bitmap.createBitmap(bitmap, xPos, yPos, imgWidth, imgHeight)
    }

    /* required methods from implementing GestureDetector.OnGestureListener */

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent?) {}

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {}
}
