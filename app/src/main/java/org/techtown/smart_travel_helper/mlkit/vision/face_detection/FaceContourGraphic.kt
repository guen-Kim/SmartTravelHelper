package org.techtown.smart_travel_helper.mlkit.vision.face_detection

import android.graphics.*
import androidx.annotation.ColorInt
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import org.techtown.smart_travel_helper.camerax.GraphicOverlay

class FaceContourGraphic(
    overlay: GraphicOverlay,
    private val face: Face,
    private val imageRect: Rect,
    private val closedEye: Boolean
) : GraphicOverlay.Graphic(overlay) {

    private val facePositionPaint: Paint // Paint: 크레파스
    private val boxPaint: Paint
    private val closedBoxPaint: Paint
    private val textPaint: Paint

    init {
        val selectedColor = Color.WHITE

        //얼굴 크레파스
        facePositionPaint = Paint()
        facePositionPaint.color = selectedColor

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



        // Text 크레파스
        textPaint = Paint()
        textPaint.color = selectedColor
        textPaint.textSize = 40f
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

        if(closedEye){
            canvas?.drawRect(rect, closedBoxPaint) // 사각형
        }else{
            canvas?.drawRect(rect, boxPaint)

        }
        canvas?.drawText("운전자", rect.width() + 270, rect.height() - 170, textPaint)


        //TODO: face contours(윤관선) 그리기
//        val contours = face.allContours // face data 반환
//        contours.forEach {
//            it.points.forEach { point ->
//                val px = translateX(point.x)
//                val py = translateY(point.y)
//                //canvas?.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint) // 원형
//            }
//        }


        //TODO: 특징점 그리기
        canvas?.drawFace(FaceContour.FACE, Color.GRAY)

//        // left eye
//        canvas?.drawFace(FaceContour.LEFT_EYE, Color.GRAY)
//
//        // right eye
//        canvas?.drawFace(FaceContour.RIGHT_EYE, Color.GRAY)

    }

    private fun Canvas.drawFace(facePosition: Int, @ColorInt selectedColor: Int) { //Canvas.확장함수
        val contour = face.getContour(facePosition)
        val path = Path()
        contour?.points?.forEachIndexed { index, pointF ->
            if (index == 0) {
                path.moveTo( // 현재의 포인터를 옮긴다.
                    translateX(pointF.x),
                    translateY(pointF.y)
                )
            }
            path.lineTo( // 현재 포인터로부터 주어진 좌표로 선을 긋는다.
                translateX(pointF.x),
                translateY(pointF.y)
            )
        }
        // path  크레파스
        val paint = Paint().apply {
            color = selectedColor
            style = Paint.Style.STROKE
            strokeWidth = BOX_STROKE_WIDTH
        }
        drawPath(path, paint) //  직선
    }

    companion object {
        private const val FACE_POSITION_RADIUS = 4.0f
        private const val ID_TEXT_SIZE = 30.0f
        private const val BOX_STROKE_WIDTH = 5.0f
    }

}