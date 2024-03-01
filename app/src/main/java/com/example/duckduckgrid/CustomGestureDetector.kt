package com.example.duckduckgrid

import android.content.Context
import android.util.Log
import android.view.ScaleGestureDetector
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class CustomGestureDetector(context: Context, listener: OnScaleGestureListener) : ScaleGestureDetector(context, listener) {
    private val TRANSITION_BOUNDARY = 1.09f
    private val SMALL_MAX_SCALE_FACTOR = 1.25f
    private val SPAN_SLOP = 7

    private lateinit var smallRecyclerView: RecyclerView
    private lateinit var mediumRecyclerView: RecyclerView

    private var scaleFactor = 0f
    private var scaleFactorMedium = 0f
    //var isInProgress = false


    private fun gestureTolerance(detector: ScaleGestureDetector): Boolean {
        val spanDelta = Math.abs(detector.currentSpan - detector.previousSpan)
        return spanDelta > SPAN_SLOP
    }

    private fun IsScaleInProgress(): Boolean {
        return scaleFactor < SMALL_MAX_SCALE_FACTOR && scaleFactor > 1f
    }

    private fun transitionFromSmallToMedium() {
        Log.d("Scale", "transitionFromSmallToMedium: ")
        mediumRecyclerView.animate().scaleX(1f).scaleY(1f).alpha(1f).withStartAction {
            smallRecyclerView.animate().scaleY(SMALL_MAX_SCALE_FACTOR)
                .scaleX(SMALL_MAX_SCALE_FACTOR)
                .alpha(0f)
                .start()
        }.withEndAction { smallRecyclerView.visibility = View.INVISIBLE }.start()
    }

    private fun transitionFromMediumToSmall() {
        Log.d("Scale", "transitionFromMediumToSmall: ")
        smallRecyclerView.animate().scaleX(1f).scaleY(1f).alpha(1f).withStartAction {
            mediumRecyclerView.animate().scaleX(0.8f).scaleY(0.8f).alpha(0f).start()
        }.withEndAction { mediumRecyclerView.visibility = View.INVISIBLE }.start()
    }
}