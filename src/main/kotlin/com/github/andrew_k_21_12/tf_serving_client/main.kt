package com.github.andrew_k_21_12.tf_serving_client

import io.grpc.ManagedChannelBuilder
import tensorflow.serving.PredictionServiceGrpc
import tensorflow.serving.Model.ModelSpec
import org.tensorflow.framework.TensorProto
import org.tensorflow.framework.DataType
import org.tensorflow.framework.TensorShapeProto
import tensorflow.serving.Predict
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * To store configurable values.
 */
private class Configs {

    // Static.
    companion object {
        const val host = "yourhost.com"
        const val port = 9000
        const val modelName = "default"
        const val modelSignatureName = "predict"
        const val modelInput = "images"
        const val modelOutput = "scores"
    }

}

/**
 * The main entry point of the app.
 *
 * @param args Command line arguments.
 * */
fun main(args: Array<String>) {
    // Checking args.
    if (args.size == 0) {
        System.err.println("Please provide a path to the image to be recognized via TensorFlow server")
        return
    }

    // Opening an image.
    val imagePath = args[0]
    val image: BufferedImage
    try {
        image = ImageIO.read(File(imagePath))
    }
    catch (e: Exception) {
        System.err.println("Could not open provided image: " + imagePath)
        return
    }



    // Preparing a channel for the request.
    val channel = ManagedChannelBuilder.forAddress(Configs.host, Configs.port)
            .usePlaintext(true)
            .build()
    val stub = PredictionServiceGrpc.newBlockingStub(channel)



    // Describing model's signatures.
    val modelSpec = ModelSpec.newBuilder()
            .setName(Configs.modelName)
            .setSignatureName(Configs.modelSignatureName)



    // Describing model's input.
    val dimImagesCount = TensorShapeProto.Dim.newBuilder().setSize(1)
    val dimImageWidth = TensorShapeProto.Dim.newBuilder().setSize(image.width.toLong())
    val dimImageHeight = TensorShapeProto.Dim.newBuilder().setSize(image.height.toLong())
    val dimImageDepth = TensorShapeProto.Dim.newBuilder().setSize(3)
    val shape = TensorShapeProto.newBuilder()
            .addDim(dimImagesCount)
            .addDim(dimImageWidth)
            .addDim(dimImageHeight)
            .addDim(dimImageDepth)



    // Loading opened image.
    val builder = TensorProto.newBuilder()
            .setDtype(DataType.DT_FLOAT)
            .setTensorShape(shape)

        for (x in 0 until image.width)
            for (y in 0 until image.height)
                for (c in 0..2) {
                    val bytesShift = c * 8
                    val pixelRGB = image.getRGB(y, x)
                    val pixelIntInChannel = ((pixelRGB shr bytesShift) and 0x000000FF).toFloat()
                    builder.addFloatVal(pixelIntInChannel)
                }



    // Requesting prediction from server with provided image.
    val requestBuilder = Predict.PredictRequest.newBuilder()
            .setModelSpec(modelSpec)
            .putInputs(Configs.modelInput, builder.build())

    // Parsing response.
    val response: Predict.PredictResponse
    try {
        response = stub.predict(requestBuilder.build())
    }
    catch (e: Exception) {
        System.err.println("No connection to the TensorFlow Serving,\n" +
                           "please check your connection or server's availability:\n" + e.toString())
        return
    }
    val scores = response.getOutputsOrThrow(Configs.modelOutput).getFloatValList()
    val max = scores.max()
    val maxIndex = scores.indexOf(max)
    System.out.println(
            String.format("Result class is %s with response %s", maxIndex, max))
}