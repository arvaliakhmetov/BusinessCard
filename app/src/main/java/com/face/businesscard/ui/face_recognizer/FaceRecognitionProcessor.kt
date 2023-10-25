

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import android.util.Pair
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.ui.graphics.asImageBitmap
import com.face.businesscard.database.entity.CardInfo
import com.face.businesscard.ui.face_recognizer.Person
import com.face.businesscard.ui.face_recognizer.VisionBaseProcessor

import com.google.mlkit.vision.face.Face
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import java.time.Instant
import kotlin.math.sqrt

class FaceRecognitionProcessor(
    faceNetModelInterpreter: Interpreter,
    private val callback: FaceRecognitionCallback?,
) : VisionBaseProcessor<List<Face?>?>() {
    interface FaceRecognitionCallback {
        fun onFaceRecognised(face: Face?, probability: Float, name: String?)
        fun onFaceDetected(face: Face?, faceBitmap: Bitmap?, vector: FloatArray?)
    }

    private val faceNetModelInterpreter: Interpreter
    private val faceNetImageProcessor: ImageProcessor
    var recognisedFaceList: MutableList<Person?> = mutableListOf()

    init {
        // initialize processors
        this.faceNetModelInterpreter = faceNetModelInterpreter
        faceNetImageProcessor = ImageProcessor.Builder()
            .add(
                ResizeOp(
                    FACENET_INPUT_IMAGE_SIZE,
                    FACENET_INPUT_IMAGE_SIZE,
                    ResizeOp.ResizeMethod.BILINEAR
                )
            )
            .add(NormalizeOp(0f, 255f))
            .build()
    }

    @OptIn(ExperimentalGetImage::class)
    override fun detectInImage(
        faces: List<Face>,
        bitmap: Bitmap,
        rotation: Float?,
        faceDirection: FaceDirection?
    ):FloatArray? {
        val faceBitmap = bitmap.flip(horizontal = false).getOrNull()
        if (faceBitmap == null) {
            Log.d("GraphicOverlay", "Face bitmap null")
            return null
        }
        val tensorImage: TensorImage = TensorImage.fromBitmap(faceBitmap)
        val faceNetByteBuffer: ByteBuffer =
            faceNetImageProcessor.process(tensorImage).buffer
        val faceOutputArray = Array(1) { FloatArray(192) }
        faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
        return faceOutputArray[0]

    }

    // looks for the nearest vector in the dataset (using L2 norm)
    // and returns the pair <name, distance>
    private fun findNearestFace(vector: FloatArray): Pair<String, Float>? {
        return recognisedFaceList
                .filter { person ->
                    person?.faceVector != null && vector.size == person.faceVector.size
                }
                .minByOrNull { person ->
                    vector.zip(person!!.faceVector)
                        .map { (a, b) -> (a - b) * (a - b) }
                        .sum()
                }
                ?.let { person ->
                    val squaredDistance = vector.zip(person.faceVector)
                        .map { (a, b) -> (a - b) * (a - b) }
                        .sum()
                    Pair(person.id, sqrt(squaredDistance.toDouble()).toFloat())
                }
    }
    /*fun findNearestKnownFace(vector: FloatArray): Pair<String, Float>? {
        return knownFaces
            .filter { person ->
                person?.faceVector != null && vector.size == person.faceVector.size
            }
            .minByOrNull { person ->
                vector.zip(person!!.faceVector)
                    .map { (a, b) -> (a - b) * (a - b) }
                    .sum()
            }
            ?.let { person ->
                val squaredDistance = vector.zip(person.faceVector)
                    .map { (a, b) -> (a - b) * (a - b) }
                    .sum()
                Pair(person.id, sqrt(squaredDistance.toDouble()).toFloat())
            }
    }*/

    fun cropToBBox(_image: Bitmap, boundingBox: Rect, rotation:Float): Bitmap? {
        var image = _image
        val shift = 0
        Log.d("CropToBBox", "BoundingBox: $boundingBox")
        Log.d("CropToBBox", "Image Dimensions: ${image.width} x ${image.height}")
        Log.d("CropToBBox", "Rotation: $rotation")
        if (rotation != 0f) {
            val matrix = Matrix()
            matrix.postRotate(rotation)
            image = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        }
        return if (boundingBox.top >= 0 && boundingBox.bottom <= image.width && boundingBox.top + boundingBox.height() <= image.height && boundingBox.left >= 0 && boundingBox.left + boundingBox.width() <= image.width
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

    // Register a name against the vector
    fun registerFace(input: String, tempVector: FloatArray?,faceBitmap: Bitmap?) {
        faceBitmap?.let {
            recognisedFaceList.add(tempVector?.let { Person(input, it,faceBitmap?.asImageBitmap()) })
        }
    }

    companion object {
        private const val TAG = "FaceRecognitionProcessor"

        // Input image size for our facenet model
        private const val FACENET_INPUT_IMAGE_SIZE = 112
    }
}