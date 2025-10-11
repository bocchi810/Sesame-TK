package fansirsqi.xposed.sesame.newui

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withRotation
import fansirsqi.xposed.sesame.data.ViewAppInfo.verifuids
import fansirsqi.xposed.sesame.util.TimeUtil
import kotlin.random.Random

class WatermarkView(context: Context) : View(context) {

    private val paint = Paint().apply {
        color = "#273f47".toColorInt()
        textSize = 46f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    private var textLines: List<String> = emptyList()

    var watermarkText: String? = null
        set(_) {
            // 固定前缀
            val prefixLines = mutableListOf(
                "免费模块仅供学习,勿在国内平台传播!!"
            )
            // 当前时间
            val suffix = "${TimeUtil.getFormatDateTime()}"
            // UID 列表，如果为空就显示“未载入账号”，否则带索引显示
            val uidLines = if (verifuids.isEmpty()) {
                listOf(
                    "未载入账号",
                    "请启用模块后重启一次支付宝",
                    "确保模块生成对应账号配置"
                )
            } else {
                verifuids.mapIndexed { index, uid ->
                    "UID${index + 1}: $uid"
                }
            }
            val combinedLines = prefixLines + uidLines + suffix
            field = combinedLines.joinToString("\n")
            textLines = combinedLines
            invalidate()
        }


    /** 控制整体水印稀疏度（越大越密集，越小越稀疏） */
    private var densityFactor: Float = 0.1f

    /** 旋转角度 */
    var rotationAngle: Float = -30f

    init {
        watermarkText = watermarkText
    }

    fun setInfo(tags: List<String>) {
        watermarkText = buildString {
            appendLine(tags.joinToString(" | "))
        }
    }

    fun setWatermarkStyle(color: Int, size: Float) {
        paint.color = color
        paint.textSize = size
        invalidate()
    }

    /** 调整整体密度，默认 1f = 正常，0.5f = 稀疏，2f = 更密 */
    fun setDensity(density: Float = 1f) {
        densityFactor = density.coerceAtLeast(0.1f) // 防止过稀
        invalidate()
    }


    private val offsetX = Random.nextInt(-200, 200) // 根据需要调整最大偏移量
    private val offsetY = Random.nextInt(-200, 200)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0 || textLines.isEmpty()) return

        val maxLineWidth = textLines.maxOfOrNull { paint.measureText(it) } ?: 0f
        val lineHeight = paint.fontSpacing
        val totalTextHeight = lineHeight * textLines.size

        var horizontalSpacing = (maxLineWidth * 1.3f / densityFactor).toInt()
        var verticalSpacing = (totalTextHeight * 2.3f / densityFactor).toInt()

        horizontalSpacing = horizontalSpacing.coerceAtMost(width)
        verticalSpacing = verticalSpacing.coerceAtMost(height)

        var yIndex = 0
        canvas.withRotation(rotationAngle) {
            var y = -height.toFloat() + offsetY
            while (y < height * 2) {
                var x = -width.toFloat() + offsetX
                if (yIndex % 2 == 1) x += horizontalSpacing / 2

                while (x < width * 2) {
                    val baseY = y - totalTextHeight / 2
                    for ((i, line) in textLines.withIndex()) {
                        drawText(line, x, baseY + i * lineHeight, paint)
                    }
                    x += horizontalSpacing
                }
                y += verticalSpacing
                yIndex++
            }
        }
    }




    companion object {
        @JvmStatic
        @JvmOverloads
        fun install(
            activity: Activity,
            text: String = "",
            color: Int = "#27273f47".toColorInt(),
            fontSize: Float = 42f,
            density: Float = 0.9f
        ): WatermarkView {
            val watermarkView = WatermarkView(activity).apply {
                watermarkText = text
                setWatermarkStyle(color, fontSize)
                setDensity(density)
            }
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(
                watermarkView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            return watermarkView
        }
    }
}
