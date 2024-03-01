package com.example.duckduckgrid

import android.view.ScaleGestureDetector




interface OnScaleGestureListener {
    fun onScale(detector: ScaleGestureDetector?): Boolean

    fun onScaleBegin(detector: ScaleGestureDetector?): Boolean

    fun onScaleEnd(detector: ScaleGestureDetector?)
}