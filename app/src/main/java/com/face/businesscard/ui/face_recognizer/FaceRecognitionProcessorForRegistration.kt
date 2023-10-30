

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
            val tensorImage: TensorImage = TensorImage.fromBitmap(faceBitmap)
            val faceNetByteBuffer: ByteBuffer =
                faceNetImageProcessor.process(tensorImage).buffer
            val faceOutputArray = Array(1) { FloatArray(192) }
            if(face.headEulerAngleX in (faceDirection!!.eulerX-9)..(faceDirection.eulerX+9)
                && face.headEulerAngleY in (faceDirection.eulerY-9)..(faceDirection.eulerY+9)
            ) {
                faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                Log.d("currentDir_GOOD", faceDirection.name)
                if (faceOutputArray.size >= 1) {
                    callback!!.onFaceDetected(
                        floatArray = faceOutputArray[0],
                        faceDirection
                    )
                }
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
    FACE_BOTTOM1(-16.772182f,1.2777171f),
    FACE_BOTTOM2(-15.331356f,8.203784f),
    FACE_BOTTOM3(-11.995197f,14.251421f),
    FACE_BOTTOM4(-9.267882f,18.591719f),
    FACE_BOTTOM5(-8.347089f,20.83218f),
    FACE_BOTTOM_LEFT1(-7.640806f,17.674784f),
    FACE_BOTTOM_LEFT2(-6.7679276f,21.702019f),
    FACE_BOTTOM_LEFT3(-5.8894515f,22.982796f),
    FACE_BOTTOM_LEFT4(-3.5228975f,27.050762f),
    FACE_BOTTOM_LEFT5(-2.458739f,32.37705f),
    FACE_LEFT1(4.342686f,27.130363f),
    FACE_LEFT2(6.5351496f,27.553864f),
    FACE_LEFT3(9.802652f,26.787552f),
    FACE_LEFT4(10.25518f,27.200367f),
    FACE_LEFT5(11.335956f,22.058292f),
    FACE_LEFT_TOP1(16.904812f,18.682642f),
    FACE_LEFT_TOP2(21.34992f,10.344861f),
    FACE_LEFT_TOP3(22f,12f),
    FACE_LEFT_TOP4(23f,8f),
    FACE_LEFT_TOP5(25f,4f),
    FACE_TOP(29.338478f,-2.2891824f),
    FACE_TOP1(33.419968f,-4.769429f),
    FACE_TOP2(31.742641f,-13.47013f),
    FACE_TOP3(30.369144f,-18.208298f),
    FACE_TOP4(26.87295f,-20.52124f),
    FACE_TOP5(26.058212f,-22.8284f),
    FACE_TOP_RIGHT1(19.242182f,-28.28658f),
    FACE_TOP_RIGHT2(13.546769f,-30.70015f),
    FACE_TOP_RIGHT3(10.390649f,-30.907042f),
    FACE_TOP_RIGHT4(7.710272f,-29.300814f),
    FACE_TOP_RIGHT5(4.904235f,-28.726263f),
    FACE_RIGHT1(-2f,-34f),
    FACE_RIGHT2(-4f,-28f),
    FACE_RIGHT3(-6f,-22f),
    FACE_RIGHT4(-8f,-21f),
    FACE_RIGHT5(-10f,-20f),
    FACE_BOTTOM_RIGHT1(-12f,-14f),
    FACE_BOTTOM_RIGHT2(-14f,-12f),
    FACE_BOTTOM_RIGHT3(-16f,-6f),
    FACE_BOTTOM_RIGHT4(-20f,0f),
}