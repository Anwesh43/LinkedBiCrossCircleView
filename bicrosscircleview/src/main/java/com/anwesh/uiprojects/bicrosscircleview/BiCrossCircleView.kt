package com.anwesh.uiprojects.bicrosscircleview

/**
 * Created by anweshmishra on 08/09/20.
 */

import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.app.Activity
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val colors : Array<Int> = arrayOf(
        "#F44336",
        "#03A9F4",
        "#4CAF50",
        "#3F51B5",
        "#FF9800"
).map({Color.parseColor(it)}).toTypedArray()
val parts : Int =  5
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val sizeFactor : Float = 3.2f
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val lineFactor : Float = 0.75f
