package com.example.sickTrees

import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils


/*
Class implementing classification functionality.
It loads the model, does query image preprocessing and predicts its class.
 */
class Classifier(modelPath: String) {
    // Neural network
    val model = Module.load(modelPath)

    // ImageNet normalization statistics
    var mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    var std = floatArrayOf(0.229f, 0.224f, 0.225f)

    val CLASSES = arrayOf("healthy", "multiple_diseases", "rust", "scab")

    fun preprocess(bitmap: Bitmap?, size: Int): Tensor? {
        // Get the image, scale it to the size our network expects, return a tensor
        var bitmap = bitmap
        bitmap = Bitmap.createScaledBitmap(bitmap!!, size, size, false)

        return TensorImageUtils.bitmapToFloat32Tensor(bitmap, mean, std)
    }

    fun argMax(inputs: FloatArray): Int {
        // Return index of the biggest element in float array
        var maxIndex = -1
        var maxvalue = 0.0f

        for (i in inputs.indices) {
            if (inputs[i] > maxvalue) {
                maxIndex = i
                maxvalue = inputs[i]
            }
        }
        return maxIndex
    }

    fun predict(bitmap: Bitmap?): String? {
        // Predict image class

        // Preprocess
        val tensor = preprocess(bitmap, 500)
        val inputs = IValue.from(tensor)

        // Pass through the network
        val outputs = model.forward(inputs).toTensor()

        // Get the outputs from tensor, find most likely class
        val scores = outputs.dataAsFloatArray
        val classIndex = argMax(scores)

        return CLASSES[classIndex]
    }
}