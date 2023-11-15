

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.face.businessface.ui.face_recognizer.VisionBaseProcessor

import com.google.mlkit.vision.face.Face
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer

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
            if(faceDirection!!.name.contains(FaceDirection.FACE_EXTRA.name)){
                val tensorImage: TensorImage = TensorImage.fromBitmap(faceBitmap)
                val faceNetByteBuffer: ByteBuffer =
                    faceNetImageProcessor.process(tensorImage).buffer
                val faceOutputArray = Array(1) { FloatArray(192) }
                faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                callback!!.onFaceDetected(
                    floatArray = faceOutputArray[0],
                    faceDirection
                )
                return FloatArray(1)
            }
            else if (faceDirection.name.contains(FaceDirection.ANGLE_SMILE.name)){
                if(face.smilingProbability!! in (faceDirection.eulerY-0.1f)..(faceDirection.eulerY+0.7f)) {
                    val tensorImage: TensorImage = TensorImage.fromBitmap(faceBitmap)
                    val faceNetByteBuffer: ByteBuffer =
                        faceNetImageProcessor.process(tensorImage).buffer
                    val faceOutputArray = Array(1) { FloatArray(192) }
                    faceNetModelInterpreter.run(faceNetByteBuffer, faceOutputArray)
                    callback!!.onFaceDetected(
                        floatArray = faceOutputArray[0],
                        faceDirection
                    )
                    return FloatArray(1)
                }
            }
            else if(face.headEulerAngleX in (faceDirection.eulerX-11)..(faceDirection.eulerX+11)
                && face.headEulerAngleY in (faceDirection.eulerY-11)..(faceDirection.eulerY+11)
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
    FACE_EXTRA8s(0f,0F),
    FACE_EXTRA1734s(0f,0F),
    FACE_EXTRA(0f,0F),
    FACE_TOP(28.955835f,2.4006865f),
    FACE_EXTRA8a(0f,0F),
    FACE_EXTRA1734a(0f,0F),
    FACE_EXTRA1(0f,0F),
    FACE_EXTRA15(0f,0F),
    FACE_BOTTOM(-16.48518f,-0.4272012f),
    FACE_EXTRA8f(0f,0F),
    FACE_EXTRA1734f(0f,0F),
    FACE_EXTRA2(0f,0F),
    FACE_EXTRA16(0f,0F),
    FACE_RIGHT_TOP_1(20.89366f,-20.165407f),
    FACE_EXTRA8ds(0f,0F),
    FACE_EXTRA1734sg(0f,0F),
    FACE_EXTRA3(0f,0F),
    FACE_EXTRA17(0f,0F),
    FACE_LEFT_TOP_2(20.508915f,20.851017f),
    FACE_EXTRA8df(0f,0F),
    FACE_EXTRA1734asd(0f,0F),
    FACE_EXTRA4(0f,0F),
    FACE_EXTRA167(0f,0F),
    FACE_RIGHT_TOP_2(20.18026f,-18.73303f),
    FACE_EXTRA8sd(0f,0F),
    FACE_EXTRA1734jh(0f,0F),
    FACE_EXTRA5(0f,0F),
    FACE_EXTRA1764(0f,0F),
    FACE_LEFT_TOP_1(18.581384f,25.848743f),
    FACE_EXTRA8hj(0f,0F),
    FACE_EXTRA1734hj(0f,0F),
    FACE_EXTRA13(0f,0F),
    FACE_EXTRA173(0f,0F),
    FACE_RIGHT_BOTTOM_2(-13.100148f,-17.950938f),
    FACE_EXTRA6(0f,0F),
    FACE_EXTRA145(0f,0F),
    FACE_EXTRA8ui(0f,0F),
    FACE_EXTRA1734ui(0f,0F),
    FACE_LEFT(3.5786512f,37.70429f),
    FACE_EXTRA7(0f,0F),
    FACE_EXTRA17234(0f,0F),
    FACE_RIGHT_BOTTOM_1(-5.0886707f,-17.108055f),
    FACE_EXTRA8(0f,0F),
    FACE_EXTRA1734(0f,0F),
    FACE_LEFT_BOTTOM_1(-5.69128f,17.871437f),
    FACE_EXTRA9(0f,0F),
    FACE_RIGHT(3.5786512f,-27.70429f),
    FACE_EXTRA10(0f,0F),
    FACE_LEFT_BOTTOM_2(-5.581064f,17.650003f),
    FACE_EXTRA11(0f,0F),
    FACE_EXTRA12(0f,0F),
    ANGLE_SMILE(0f,0.3F),
    FACE_CLOSE(0f,0f),
    FACE_FAR(0f,0f),


}