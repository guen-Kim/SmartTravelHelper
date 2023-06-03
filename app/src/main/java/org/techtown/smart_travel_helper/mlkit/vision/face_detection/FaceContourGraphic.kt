package org.techtown.smart_travel_helper.mlkit.vision.face_detection

import android.graphics.*
import androidx.annotation.ColorInt
import com.google.mlkit.vision.face.Face
import org.techtown.smart_travel_helper.camerax.GraphicOverlay

class FaceContourGraphic(
    overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect,
    private val closedEye: Boolean
) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint: Paint
    private val closedBoxPaint: Paint

    init {
        val selectedColor = Color.WHITE


        // 박스 크레파스
        boxPaint = Paint()
        boxPaint.color = selectedColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = BOX_STROKE_WIDTH

        // 박스 크레파스
        closedBoxPaint = Paint()
        closedBoxPaint.color = Color.RED
        closedBoxPaint.style = Paint.Style.STROKE
        closedBoxPaint.strokeWidth = BOX_STROKE_WIDTH

    }

    /** 영상 데이터 위에 Canvas 그리기 그려질( Canvas 도화지 ) */
    override fun draw(canvas: Canvas?) {
        //TODO: face ROI draw

        // cropRect(탐지된 관심영역) 계산(회전 등)
        val rect = calculateRect(
            imageRect.height().toFloat(),
            imageRect.width().toFloat(),
            face.boundingBox
        )

        if (closedEye) {
            canvas?.drawRect(rect, closedBoxPaint) // 사각형
        } else {
            canvas?.drawRect(rect, boxPaint)

        }
    }

    private fun Canvas.drawFace(facePosition: Int, @ColorInt selectedColor: Int) { //Canvas.확장함수
        val path = Path()

        // path  크레파스
        val paint = Paint().apply {
            color = selectedColor
            style = Paint.Style.STROKE
            strokeWidth = BOX_STROKE_WIDTH
        }
        drawPath(path, paint) //  직선
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }

}