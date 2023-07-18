package com.dat.graphic2d

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import kotlin.math.cos
import kotlin.math.sin


@RequiresApi(Build.VERSION_CODES.Q)
class MyView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet?) : super(context, attributes)
    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributes,
        defStyleAttr
    )

    private val linegraph: Path
    private var redPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var viewHeight: Int
    private var viewWidth: Int
    private var rectBound = RectF()
//    private var bluePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        redPaint.style = Paint.Style.STROKE
        redPaint.color = Color.RED
        redPaint.strokeWidth = 5f

        viewHeight = resources.displayMetrics.heightPixels - 70
        viewWidth = resources.displayMetrics.widthPixels
        val size = viewWidth / 2
        val space = viewHeight.toFloat() / 2 - size

        rectBound = RectF(0f, space, viewWidth.toFloat(), space + size * 2)
        val plotData = listOf(20, 15, 34, 19, 38)
//        linegraph = createLineGraph(plotData, viewWidth, viewHeight)
        linegraph = createPieGraph(plotData, viewWidth, viewHeight)
    }

    private fun createPieGraph(input: List<Int>, width: Int, height: Int): Path {
        val total = 360f
        val sum = input.sum()
        val split = total / sum
        val path = Path()
        val newInput = input.toTypedArray()

        newInput.forEachIndexed { index, _ ->
            val prevItem = newInput.getOrNull(index - 1) ?: 0
            val newValue = prevItem + newInput[index]
            newInput[index] = newValue
            println("start " + prevItem * split + 10f)
            println("an " + (newValue * split - 10f))
            path.moveTo((width / 2).toFloat(), (height / 2).toFloat())
            path.arcTo(rectBound, prevItem * split, newValue * split)

        }
        path.close()
        return path
    }

    private fun createLineGraph(input: List<Int>, width: Int, height: Int): Path {
        var ptArray = input.mapIndexed { index, i ->
            Point(index, i)
        }.toTypedArray()
        val minValue = input.min()
        val maxValue = input.max()

        ptArray = translate(ptArray, 0, -minValue)
        val yScale = height / (maxValue - minValue).toDouble()
        val xScale = width / (input.size - 1).toDouble()
        ptArray = scale(ptArray, xScale, yScale)
        val result = Path()
        result.moveTo(ptArray[0].x.toFloat(), ptArray[0].y.toFloat())
        for (i in 1 until ptArray.size) {
            result.lineTo(ptArray[i].x.toFloat(), ptArray[i].y.toFloat())
        }
        return result
    }

    private fun affineTransformation(vertices: Array<Point>, matrix: List<Array<Double>>): Array<Point> {
        val result = Array(vertices.size) { Point() }
        for (i in result.indices) {
            val t = matrix[0][0] * vertices[i].x + matrix[0][1] * vertices[i].y + matrix[0][2]
            val u = matrix[1][0] * vertices[i].x + matrix[1][1] * vertices[i].y + matrix[1][2]
            result[i] = Point(t.toInt(), u.toInt())
        }
        return result
    }

    fun translate(input: Array<Point>, px: Int, py: Int): Array<Point> {
        val matrix = List(3) { Array(3) { 0.0 } }
        matrix[0][0] = 1.0
        matrix[0][1] = 0.0
        matrix[0][2] = px.toDouble()

        matrix[1][0] = 0.0
        matrix[1][1] = 1.0
        matrix[1][2] = py.toDouble()

        matrix[2][0] = 0.0
        matrix[2][1] = 0.0
        matrix[2][2] = 1.0

        return affineTransformation(input, matrix)
    }

    fun rotation(input: Array<Point>, rotate: Float): Array<Point> {
        val matrix = List(3) { Array(3) { 0.0 } }
        val angleInRadians = rotate * Math.PI / 180
        var input1 = input
        val central = Point(input1.map { it.x }.average().toInt(), input1.map { it.y }.average().toInt())
        input1 = input1.map {
            Point(it.x - central.x, it.y - central.y)
        }.toTypedArray()

        matrix[0][0] = cos(angleInRadians)
        matrix[0][1] = -sin(angleInRadians)
        matrix[0][2] = 0.0
        matrix[1][0] = sin(angleInRadians)
        matrix[1][1] = cos(angleInRadians)
        matrix[1][2] = 0.0

        matrix[2][0] = 0.0
        matrix[2][1] = 0.0
        matrix[2][2] = 1.0

        val result = affineTransformation(input1, matrix)
        input1 = result.map {
            Point(it.x + central.x, it.y + central.y)
        }.toTypedArray()
        return input1
    }

    fun scale(input: Array<Point>, scaleX: Double, scaleY: Double): Array<Point> {
        val matrix = List(3) { Array(3) { 0.0 } }
        matrix[0][0] = scaleX
        matrix[0][1] = 0.0
        matrix[0][2] = 0.0

        matrix[1][0] = 0.0
        matrix[1][1] = scaleY
        matrix[1][2] = 0.0

        matrix[2][0] = 0.0
        matrix[2][1] = 0.0
        matrix[2][2] = 1.0


        return affineTransformation(input, matrix)
    }

    fun shear(input: Array<Point>, f: Int = 1, e: Int = 1): Array<Point> {
        val matrix = List(3) { Array(3) { 0.0 } }
        matrix[0][0] = 1.0
        matrix[0][1] = e.toDouble()
        matrix[0][2] = 0.0

        matrix[1][0] = f.toDouble()
        matrix[1][1] = 1.0
        matrix[1][2] = 0.0

        matrix[2][0] = 0.0
        matrix[2][1] = 0.0
        matrix[2][2] = 1.0

        return affineTransformation(input, matrix)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawPath(linegraph, redPaint)
        val x = 40
        val points = arrayListOf<Point>()

        for (i in 0..x) {
            points.add(Point(i, (10 * sin(i.toDouble())).toInt()))
        }

        val xScale = width / x
        var ptArray = scale(points.toTypedArray(), xScale.toDouble(), 4.0)
        ptArray = translate(ptArray, 0, 100)
        val result = Path()
        result.moveTo(ptArray[0].x.toFloat(), ptArray[0].y.toFloat())
        for (i in 1 until ptArray.size) {
            result.lineTo(ptArray[i].x.toFloat(), ptArray[i].y.toFloat())
        }
        canvas?.drawPath(result, redPaint)
    }
}