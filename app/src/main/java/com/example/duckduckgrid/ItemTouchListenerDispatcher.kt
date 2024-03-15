package com.example.duckduckgrid

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
import android.widget.Scroller
import androidx.recyclerview.widget.RecyclerView


open class ItemTouchListenerDispatcher(private val customGestureDetector: CustomGestureDetector, private val interceptTouchCallback : () -> Boolean) : RecyclerView.OnItemTouchListener {

    private var lastXEvent: Float = 0F
    private var lastYEvent: Float = 0F


    private var mTouchSlop = 0
    private var mMinimumVelocity = 0
    private var mScale = 1f
    private var mScaleDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null
    private val mEnableScaling = false

    private var mLastScroll: Long = 0
    private var mScroller: Scroller? = null

    /**
     * Position of the last motion event.
     */
    private var mLastMotionY = 0f
    private var mLastMotionX = 0f

    /**
     * True if the user is currently dragging this TwoDScrollView around. This is
     * not the same as 'is being flinged', which can be checked by
     * mScroller.isFinished() (flinging begins when the user lifts his finger).
     */
    private var mIsBeingDragged = false

    /**
     * Determines speed during touch scrolling
     */
    private var mVelocityTracker: VelocityTracker? = null

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        return interceptTouchCallback()
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        if (customGestureDetector.isInProgress) {
            var currentSpan = getSpan(e)
            when (rv.id) {
                R.id.recycler_view_small -> {
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
    }

    /*override fun onTouchEvent(rv: RecyclerView, ev: MotionEvent) {
        if (ev.action == MotionEvent.ACTION_DOWN && ev.edgeFlags != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
            //return false
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain()
            }
            mVelocityTracker!!.addMovement(ev)
            val action = ev.action
            val y = ev.y
            val x = ev.x
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    /*
                * If being flinged and user touches, stop the fling. isFinished
                * will be false if being flinged.
                */if (!mScroller!!.isFinished) {
                        mScroller!!.abortAnimation()
                    }

                    // Remember where the motion event started
                    mLastMotionY = y
                    mLastMotionX = x
                }

                MotionEvent.ACTION_MOVE -> {
                    // Scroll to follow the motion event
                    var deltaX = (mLastMotionX - x).toInt()
                    var deltaY = (mLastMotionY - y).toInt()
                    mLastMotionX = x
                    mLastMotionY = y
                    if (deltaX < 0) {
                        if (rv.scrollX < 0) {
                            deltaX = 0
                        }
                    } else if (deltaX > 0) {
                        val rightEdge = rv.width - rv.paddingRight
                        val availableToScroll = rv.getChildAt(0).right - rv.scrollX - rightEdge
                        deltaX = if (availableToScroll > 0) {
                            Math.min(availableToScroll, deltaX)
                        } else {
                            0
                        }
                    }
                    if (deltaY < 0) {
                        if (rv.scrollY < 0) {
                            deltaY = 0
                        }
                    } else if (deltaY > 0) {
                        val bottomEdge = rv.height - rv.paddingBottom
                        val availableToScroll = rv.getChildAt(0).bottom - rv.scrollY - bottomEdge
                        deltaY = if (availableToScroll > 0) {
                            Math.min(availableToScroll, deltaY)
                        } else {
                            0
                        }
                    }
                    if (deltaY != 0 || deltaX != 0) rv.scrollBy(deltaX, deltaY)
                }

                MotionEvent.ACTION_UP -> {
                    val velocityTracker = mVelocityTracker
                    velocityTracker!!.computeCurrentVelocity(1000)
                    val initialXVelocity = velocityTracker.xVelocity.toInt()
                    val initialYVelocity = velocityTracker.yVelocity.toInt()
                    if (Math.abs(initialXVelocity) + Math.abs(initialYVelocity) > mMinimumVelocity && rv.childCount > 0) {
                        rv.fling(-initialXVelocity, -initialYVelocity)
                    }
                    if (mVelocityTracker != null) {
                        mVelocityTracker!!.recycle()
                        mVelocityTracker = null
                    }
                }
            }
        }
    }*/

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