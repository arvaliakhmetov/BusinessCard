

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import com.google.mlkit.vision.face.Face

fun adjustPoint(point: PointF, imageWidth: Int, imageHeight: Int, screenWidth: Int, screenHeight: Int): PointF {
    val x = point.x / imageWidth * screenWidth
    val y = point.y / imageHeight * screenHeight
    return PointF(x, y)
}

fun adjustSize(size: Size, imageWidth: Int, imageHeight: Int, screenWidth: Int, screenHeight: Int): Size {
    val width = size.width / imageWidth * screenWidth
    val height = size.height / imageHeight * screenHeight
    return Size(width, height)
}
fun Bitmap.crop(left: Int, top: Int, width: Int, height: Int): Result<Bitmap> = runCatching {
    Bitmap.createBitmap(this, left, top, width, height)
}.onFailure { Log.d(it.toString(), it.message.toString()) }

fun Bitmap.flip(vertical: Boolean = false, horizontal: Boolean = false): Result<Bitmap> = runCatching {
    val matrix = Matrix()
    if (vertical) matrix.postScale(1f, -1f)
    if (horizontal) matrix.postScale(-1f, 1f)
    Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}.onFailure { Log.d(it.toString(), it.message.toString()) }

fun biggestFace(faces: MutableList<Face>): Face? {
    var biggestFace: Face? = null
    var biggestFaceSize = 0
    for (face in faces) {
        val faceSize = face.boundingBox.height() * face.boundingBox.width()
        if (faceSize > biggestFaceSize) {
            biggestFaceSize = faceSize
            biggestFace = face
        }
    }
    return biggestFace
}
fun Bitmap.rotate(rotation: Float): Result<Bitmap> = runCatching {
    val matrix = Matrix()
    matrix.postRotate(rotation)
    Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}.onFailure { Log.d(it.toString(), it.message.toString()) }
fun processImage(
    lensFacing: Int,
    data: MutableList<Face>,
    bitmap: Bitmap,
    paint: Paint
): Result<Bitmap?> = runCatching {
    paint.style = Paint.Style.STROKE
    val face = biggestFace(data)
    var frame = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
    var faceBitmap = face?.boundingBox?.let { bitmap.crop(it.left, it.top, it.width(), it.height()).getOrNull() }
    val canvas = Canvas(frame)
    canvas.drawBitmap(bitmap, 0f, 0f, null)
    data.forEach { canvas.drawRect(it.boundingBox, paint) }
    if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
        frame = frame.flip(horizontal = true).getOrNull()!!
        faceBitmap = faceBitmap?.flip(horizontal = false)?.getOrNull()
    }

    return@runCatching faceBitmap
}.onFailure {  }