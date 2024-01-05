

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Log
import android.util.Pair
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.ui.graphics.asImageBitmap
import com.face.businessface.ui.face_recognizer.Person
import com.face.businessface.ui.face_recognizer.VisionBaseProcessor

import com.google.mlkit.vision.face.Face
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import kotlin.math.sqrt

class FaceRecognitionProcessor(
    faceNetModelInterpreter: Interpreter,
) : VisionBaseProcessor<List<Face?>?>() {

    private val faceNetModelInterpreter: Interpreter
    private val faceNetImageProcessor: ImageProcessor
    init {
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
        Log.d("GraphicOverlay", "${faceOutputArray[0]}")
        return faceOutputArray[0]
    }


    companion object {
        private const val TAG = "FaceRecognitionProcessor"

        // Input image size for our facenet model
        private const val FACENET_INPUT_IMAGE_SIZE = 112
    }
}