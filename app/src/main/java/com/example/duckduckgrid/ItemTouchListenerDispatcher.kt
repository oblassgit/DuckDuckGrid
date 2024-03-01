package com.example.duckduckgrid

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView


open class ItemTouchListenerDispatcher(private val customGestureDetector: CustomGestureDetector, private val interceptTouchCallback : () -> Boolean) : RecyclerView.OnItemTouchListener {

    private var lastXEvent: Float = 0F
    private var lastYEvent: Float = 0F

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return interceptTouchCallback()
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        var currentSpan = getSpan(e)
        when (rv.id) {
            R.id.recycler_view_fat -> {
                if (currentSpan < 0) {
                    customGestureDetector.onTouchEvent(e)
                } else if (currentSpan == 0F) {
                    val childViewUnder = rv.findChildViewUnder(e.x, e.y)
                    childViewUnder?.performClick()
                }
            }

            R.id.recycler_view -> {
                customGestureDetector.onTouchEvent(e)
            }

            else -> {}
        }
    }

    private fun getSpan(e: MotionEvent): Float {
        if (e.action == MotionEvent.ACTION_DOWN) {
            lastXEvent = e.x
            lastYEvent = e.y
        } else if (e.action == MotionEvent.ACTION_UP) {
            val finalx = e.x - lastXEvent
            val finaly = e.y - lastYEvent
            return finalx/finaly
        }
        return 0F
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }


}