import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log

fun Bitmap.flip(vertical: Boolean = false, horizontal: Boolean = false): Result<Bitmap> = runCatching {
    val matrix = Matrix()
    if (vertical) matrix.postScale(1f, -1f)
    if (horizontal) matrix.postScale(-1f, 1f)
    Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}.onFailure { Log.d(it.toString(), it.message.toString()) }

fun Bitmap.rotate(rotation: Float): Result<Bitmap> = runCatching {
    val matrix = Matrix()
    matrix.postRotate(rotation)
    Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}.onFailure { Log.d(it.toString(), it.message.toString()) }

fun cropToBBox(_image: Bitmap, boundingBox: android.graphics.Rect, rotation: Float): Bitmap? {
    var image = _image
    val shift = 2
    if (rotation != 0f) {
        val matrix = Matrix()
        matrix.postRotate(rotation)
        image = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
    }
    return if (
        boundingBox.top >= 0 &&
        boundingBox.bottom <= image.height &&
        boundingBox.left >= 0 &&
        boundingBox.left + boundingBox.width() <= image.width
    ) {
        Bitmap.createBitmap(
            image,
            boundingBox.left,
            boundingBox.top + shift,
            boundingBox.width(),
            boundingBox.height()
        )
    } else null
}