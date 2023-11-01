

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
    ): FloatArray? {
        for (face in faces) {
            val faceBitmap = cropToBBox(bitmap, face.boundingBox, 0)
            if (faceBitmap == null) {
                Log.d("GraphicOverlay", "Face bitmap null")
                return null
            }

            Log.d("FACECE", "${face.headEulerAngleX}f,${face.headEulerAngleY}f,${face.headEulerAngleZ}")
            if(face.headEulerAngleX in (faceDirection!!.eulerX-10)..(faceDirection.eulerX+10)
                && face.headEulerAngleY in (faceDirection.eulerY-10)..(faceDirection.eulerY+10)
            ) {
                val tensorImage: TensorImage = TensorImage.fromBitmap(faceBitmap)
                val faceNetByteBuffer: ByteBuffer =
                    faceNetImageProcessor.process(tensorImage).buffer
                val faceOutputArray = Array(1) { FloatArray(192) }
                faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                if (faceOutputArray.size >= 1) {
                    callback!!.onFaceDetected(
                        floatArray = faceOutputArray[0],
                        faceDirection
                    )
                    return FloatArray(1)
                }
            }
            if(faceDirection.name.contains(FaceDirection.FACE_EXTRA.name)){
                val tensorImage: TensorImage = TensorImage.fromBitmap(faceBitmap)
                val faceNetByteBuffer: ByteBuffer =
                    faceNetImageProcessor.process(tensorImage).buffer
                val faceOutputArray = Array(1) { FloatArray(192) }
                faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                callback!!.onFaceDetected(
                    floatArray = faceOutputArray[0],
                    faceDirection
                )
            }
        }
        return null
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
enum class FaceDirection(val eulerX:Float,val eulerY: Float){
    FACE_CENTER(0f,0f),
    FACE_EXTRA(0f,0F),
    FACE_TOP(20.955835f,2.4006865f),
    FACE_EXTRA1(0f,0F),
    FACE_BOTTOM(-16.48518f,-0.4272012f),
    FACE_EXTRA2(0f,0F),
    FACE_RIGHT_TOP_1(15.89366f,-26.165407f),
    FACE_EXTRA3(0f,0F),
    FACE_LEFT_TOP_2(24.508915f,29.851017f),
    FACE_EXTRA4(0f,0F),
    FACE_RIGHT_TOP_2(22.18026f,-18.73303f),
    FACE_EXTRA5(0f,0F),
    FACE_LEFT_TOP_1(15.581384f,29.848743f),
    FACE_EXTRA13(0f,0F),
    FACE_RIGHT_BOTTOM_2(-11.100148f,-13.950938f),
    FACE_EXTRA6(0f,0F),
    FACE_LEFT(3.5786512f,37.70429f),
    FACE_EXTRA7(0f,0F),
    FACE_RIGHT_BOTTOM_1(-7.0886707f,-25.108055f),
    FACE_EXTRA8(0f,0F),
    FACE_LEFT_BOTTOM_1(-5.69128f,29.871437f),
    FACE_EXTRA9(0f,0F),
    FACE_RIGHT(3.5786512f,-37.70429f),
    FACE_EXTRA10(0f,0F),
    FACE_LEFT_BOTTOM_2(-9.581064f,15.650003f),
    FACE_EXTRA11(0f,0F),
    FACE_CLOSE(0f,0f),
    FACE_FAR(0f,0f),


}