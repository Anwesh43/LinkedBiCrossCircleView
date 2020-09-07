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
val scGap : Float = 0.02f / parts
val strokeFactor : Int = 90
val sizeFactor : Float = 8.9f
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val lineFactor : Float = 0.75f
val lineColor : Int = Color.parseColor("#212121")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBiCrossCircle(scale : Float, w : Float, h : Float, currColor : Int, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val sf4 : Float = sf.divideScale(3, parts)
    val r : Float = Math.min(w, h) / sizeFactor
    val x : Float = (w / 2 - r)
    val lineSize : Float = r * lineFactor * sf3
    save()
    translate(w / 2, h / 2)
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
        paint.color = currColor
        drawCircle(x * sf2, 0f, r * sf1, paint)
        paint.color = lineColor
        for (i in 0..1) {
            save()
            translate(x, 0f)
            rotate(45f * (1f - 2 * i) * sf4)
            drawLine(0f, -lineSize, 0f, lineSize, paint)
            restore()
        }
        restore()
    }
    restore()
}

fun Canvas.drawBCCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor

    drawBiCrossCircle(scale, w, h, colors[i], paint)
}

class BiCrossCircleView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BCCNode(var i : Int, val state : State = State()) {

        private var next : BCCNode? = null
        private var prev : BCCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = BCCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBCCNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BCCNode {
            var curr : BCCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BiCrossCircle(var i : Int) {

        private var curr : BCCNode = BCCNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BiCrossCircleView) {

        private val animator : Animator = Animator(view)
        private val bcc : BiCrossCircle = BiCrossCircle(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            bcc.draw(canvas, paint)
            animator.animate {
                bcc.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bcc.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : BiCrossCircleView {
            val view : BiCrossCircleView = BiCrossCircleView(activity)
            activity.setContentView(view)
            return view
        }
    }
}