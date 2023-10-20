

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import android.util.Pair
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.face.businesscard.ui.face_recognizer.VisionBaseProcessor
import com.google.android.gms.tasks.OnSuccessListener

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.lang.Math.abs
import java.nio.ByteBuffer
import java.time.Instant
import kotlin.math.sqrt

class FaceRecognitionProcessorForRegistration(
    faceNetModelInterpreter: Interpreter,
    private val callback: FaceRecognitionCallback?
) : VisionBaseProcessor<List<Face?>?>() {
    inner class Person(var faceVector: FloatArray,)
    interface FaceRecognitionCallback {
        fun onFaceRecognised(face: Face?, probability: Float, name: String?)
        fun onFaceDetected(floatArray: FloatArray,faceDirection: FaceDirection)
    }

    private val faceNetModelInterpreter: Interpreter
    private val faceNetImageProcessor: ImageProcessor
    var recognisedFaceDirectionsMap: MutableMap<String,FloatArray> = mutableMapOf()

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
    ):Boolean {
        for (face in faces) {
            val faceBitmap = cropToBBox(bitmap, face.boundingBox, 0)
            if (faceBitmap == null) {
                Log.d("GraphicOverlay", "Face bitmap null")
                return false
            }
            val tensorImage: TensorImage = TensorImage.fromBitmap(faceBitmap)
            val faceNetByteBuffer: ByteBuffer =
                faceNetImageProcessor.process(tensorImage).buffer
            val faceOutputArray = Array(1) { FloatArray(192) }
            when (faceDirection!!){
                FaceDirection.FACE_TOP ->{
                    if(face.headEulerAngleX in 25f..40f
                        && kotlin.math.abs(face.headEulerAngleY) in 0f..10f
                        && kotlin.math.abs(face.headEulerAngleZ) in 0f..10f
                        && face.rightEyeOpenProbability!! in 0.8..1.0) {
                        faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                        if (callback != null) {
                            if(faceOutputArray.size>=1) {
                                callback.onFaceDetected(
                                    floatArray = faceOutputArray[0],
                                    FaceDirection.FACE_TOP
                                )
                                registerFace(FaceDirection.FACE_TOP, faceOutputArray.first())
                            }
                        }
                    }
                }
                FaceDirection.FACE_CENTER -> {
                    if(kotlin.math.abs(face.headEulerAngleX) in 0f..10f
                        && abs(face.headEulerAngleY) in 0f..10f
                        && kotlin.math.abs(face.headEulerAngleZ) in 0f..10f
                        && face.rightEyeOpenProbability!! in 0.3..1.0) {
                        faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                        if (callback != null) {
                            if(faceOutputArray.size>=1) {
                                callback.onFaceDetected(
                                    floatArray = faceOutputArray[0],
                                    FaceDirection.FACE_CENTER
                                )
                                registerFace(FaceDirection.FACE_CENTER, faceOutputArray.first())
                            }
                        }
                    }
                }
                FaceDirection.FACE_LEFT -> {
                    if(kotlin.math.abs(face.headEulerAngleX) in 0f..10f
                        && face.headEulerAngleY in 15f..35f
                        && kotlin.math.abs(face.headEulerAngleZ) in 0f..10f
                        && face.rightEyeOpenProbability!! in 0.3..1.0) {
                        faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                        if (callback != null) {
                            if(faceOutputArray.size>=1) {
                                callback.onFaceDetected(
                                    floatArray = faceOutputArray[0],
                                    FaceDirection.FACE_LEFT
                                )
                                registerFace(FaceDirection.FACE_LEFT, faceOutputArray.first())
                            }
                        }
                    }
                }
                FaceDirection.FACE_RIGHT -> {
                    if(kotlin.math.abs(face.headEulerAngleX) in 0f..10f
                        && -face.headEulerAngleY in 15f..35f
                        && kotlin.math.abs(face.headEulerAngleZ) in 0f..10f
                        && face.rightEyeOpenProbability!! in 0.3..1.0) {
                        faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                        if (callback != null) {
                            if(faceOutputArray.size>=1) {
                                callback.onFaceDetected(
                                    floatArray = faceOutputArray[0],
                                    FaceDirection.FACE_RIGHT
                                )
                                registerFace(FaceDirection.FACE_RIGHT, faceOutputArray.first())
                            }
                        }
                    }
                }
                FaceDirection.FACE_BOTTOM -> {
                    if(-face.headEulerAngleX in 12f..30f
                        && kotlin.math.abs(face.headEulerAngleY) in 0f..10f
                        && kotlin.math.abs(face.headEulerAngleZ) in 0f..10f
                        && face.rightEyeOpenProbability!! in 0.3..1.0) {
                        faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                        if (callback != null) {
                            if(faceOutputArray.size>=1) {
                                callback.onFaceDetected(
                                    floatArray = faceOutputArray[0],
                                    FaceDirection.FACE_BOTTOM
                                )
                                registerFace(FaceDirection.FACE_BOTTOM, faceOutputArray.first())
                            }
                        }
                    }
                }
                FaceDirection.FACE_NOT_RECOGNISED ->{
                    if(kotlin.math.abs(face.headEulerAngleX) in 0f..10f
                        && abs(face.headEulerAngleY) in 0f..10f
                        && kotlin.math.abs(face.headEulerAngleZ) in 0f..10f
                        && face.rightEyeOpenProbability!! in 0.3..1.0) {
                        faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                            recognisedFaceDirectionsMap[FaceDirection.FACE_CENTER.name]?.let { floatArray ->
                                if (faceOutputArray.size >= 1) {
                                    val result = findCenterFace(faceOutputArray[0])
                                    if (result!! > 1.08f) {
                                        callback?.onFaceDetected(
                                            floatArray = faceOutputArray[0],
                                            FaceDirection.FACE_NOT_RECOGNISED
                                        )
                                        recognisedFaceDirectionsMap.clear()
                                    } else {
                                        //registerFace(uuid.toString(), faceOutputArray[0], faceBitmap)
                                    }
                                }
                            }
                        }
                }
            }
        }
        return false
    }
    private fun findCenterFace(vector: FloatArray): Float? {
        return listOf(recognisedFaceDirectionsMap[FaceDirection.FACE_CENTER.name]!!)
            .filter { vectorMain ->
                vector.size == vectorMain.size
            }
            .minByOrNull { vectorMain ->
                vector.zip(vectorMain)
                    .map { (a, b) -> (a - b) * (a - b) }
                    .sum()
            }
            ?.let { vectorMain ->
                val squaredDistance = vector.zip(vectorMain)
                    .map { (a, b) -> (a - b) * (a - b) }
                    .sum()
                sqrt(squaredDistance.toDouble()).toFloat()
            }
    }

    // looks for the nearest vector in the dataset (using L2 norm)
    // and returns the pair <name, distance>


    private fun cropToBBox(_image: Bitmap, boundingBox: Rect, rotation: Int): Bitmap? {
        var image = _image
        val shift = 0
        if (rotation != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
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
    fun registerFace(faceDirection: FaceDirection, tempVector: FloatArray) {
        if(!recognisedFaceDirectionsMap.keys.contains(faceDirection.name)){
            recognisedFaceDirectionsMap[faceDirection.name] = tempVector
        }
    }

    companion object {
        private const val TAG = "FaceRecognitionProcessor"

        // Input image size for our facenet model
        private const val FACENET_INPUT_IMAGE_SIZE = 112

    }
}
enum class FaceDirection{
    FACE_TOP,
    FACE_BOTTOM,
    FACE_RIGHT,
    FACE_LEFT,
    FACE_CENTER,
    FACE_NOT_RECOGNISED,
}