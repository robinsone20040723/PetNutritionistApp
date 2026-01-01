package com.example.petnutritionistapp.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BCSClassifier(private val context: Context) {

    companion object {
        private const val TAG = "BCS_DEBUG"
        private const val MODEL_NAME = "bcs_shiba_classifier.tflite"
        private const val IMAGE_SIZE = 224
    }

    private var interpreter: Interpreter? = null

    fun classify(bitmap: Bitmap): Int {
        try {
            Log.e(TAG, "🚀 classify() called")

            if (interpreter == null) {
                Log.e(TAG, "📦 loading model from assets: $MODEL_NAME")

                val modelBuffer = loadModelFromAssets()

                val options = Interpreter.Options().apply {
                    setNumThreads(4)
                }

                interpreter = Interpreter(modelBuffer, options)

                val inShape = interpreter!!.getInputTensor(0).shape().contentToString()
                val outShape = interpreter!!.getOutputTensor(0).shape().contentToString()
                Log.e(TAG, "✅ Interpreter created")
                Log.e(TAG, "Input shape = $inShape")
                Log.e(TAG, "Output shape = $outShape")
            }

            val input = preprocess(bitmap)

            // [1,9]
            val output = Array(1) { FloatArray(9) }
            interpreter!!.run(input, output)

            val probs = output[0]
            Log.e(TAG, "📊 probs = ${probs.contentToString()}")

            var maxIdx = 0
            for (i in 1 until probs.size) {
                if (probs[i] > probs[maxIdx]) maxIdx = i
            }

            val bcs = maxIdx + 1
            Log.e(TAG, "✅ predicted BCS = $bcs")
            return bcs

        } catch (t: Throwable) {
            Log.e(TAG, "💥 classify() failed", t)
            throw t
        }
    }

    private fun loadModelFromAssets(): ByteBuffer {
        try {
            val bytes = context.assets.open(MODEL_NAME).readBytes()
            Log.e(TAG, "✅ model bytes loaded, size=${bytes.size}")

            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.order(ByteOrder.nativeOrder())
            buffer.put(bytes)
            buffer.rewind()
            return buffer

        } catch (t: Throwable) {
            Log.e(TAG, "❌ loadModelFromAssets failed (check assets filename/path)", t)
            throw t
        }
    }

    /**
     * ✅ 對齊模型 input: [1,224,224,3]
     */
    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true)

        // ⭐ 一定要有 batch = 1
        val buffer = ByteBuffer.allocateDirect(4 * 1 * IMAGE_SIZE * IMAGE_SIZE * 3)
        buffer.order(ByteOrder.nativeOrder())

        for (y in 0 until IMAGE_SIZE) {
            for (x in 0 until IMAGE_SIZE) {
                val pixel = resized.getPixel(x, y)
                buffer.putFloat(((pixel shr 16) and 0xFF) / 255f) // R
                buffer.putFloat(((pixel shr 8) and 0xFF) / 255f)  // G
                buffer.putFloat((pixel and 0xFF) / 255f)          // B
            }
        }

        buffer.rewind()
        return buffer
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
