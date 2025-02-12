package com.example.duckduckgrid.viewmodels

import android.app.Application
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class CameraPreviewViewModel(application: Application) : AndroidViewModel(application) {
    // Used to set up a link between the Camera and your UI.
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest
    var imageCapture: ImageCapture? = null
    private val context = application

    private var lensFacing = DEFAULT_BACK_CAMERA

    private var surfaceMeteringPointFactory: SurfaceOrientedMeteringPointFactory? = null
    private var cameraControl: CameraControl? = null

    private val _detectedObjects = MutableStateFlow<List<DetectedObjectData>>(emptyList())
    val detectedObjects: StateFlow<List<DetectedObjectData>> = _detectedObjects

    private val _isDuckDetected = MutableStateFlow(false)
    val isDuckDetected: StateFlow<Boolean> = _isDuckDetected

    private val classLabels = loadLabelsFromAssets()


    val localModel = LocalModel.Builder()
        .setAssetFilePath("refined_quantisized_model.tflite")
        .build()

    val objectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
        .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .setClassificationConfidenceThreshold(0.5f)
        .setMaxPerObjectLabelCount(1)
        .build()

    val objectDetector = ObjectDetection.getClient(objectDetectorOptions)

    /*val objectDetectorOptions = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .build()

    val objectDetector = ObjectDetection.getClient(objectDetectorOptions)*/

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
            surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                newSurfaceRequest.resolution.width.toFloat(),
                newSurfaceRequest.resolution.height.toFloat()
            )
        }
    }


    suspend fun bindToCamera(lifecycleOwner: LifecycleOwner) {
        imageCapture = ImageCapture.Builder().build()

        val processCameraProvider = ProcessCameraProvider.awaitInstance(context)
        processCameraProvider.unbindAll()

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()

        // Call analyzeImage for each frame
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
            analyzeImage(imageProxy) // <-- CALL analyzeImage HERE
        }

        val camera = processCameraProvider.bindToLifecycle(
            lifecycleOwner, lensFacing, cameraPreviewUseCase, imageCapture, imageAnalysis
        )
        cameraControl = camera.cameraControl


        // Cancellation signals we're done with the camera
        try { awaitCancellation() } finally {
            processCameraProvider.unbindAll()
            cameraControl = null
        }
    }

    fun tapToFocus(tapCoords: Offset) {
        val point = surfaceMeteringPointFactory?.createPoint(tapCoords.x, tapCoords.y)
        if (point != null) {
            val meteringAction = FocusMeteringAction.Builder(point).build()
            cameraControl?.startFocusAndMetering(meteringAction)
        }
    }

    fun captureImage() {

        imageCapture?.let { capture ->

            val imagesFolder = File(context.filesDir, "images")
            imagesFolder.mkdir()

            val photoFile = File(
                imagesFolder,
                "photo_${System.currentTimeMillis()}.jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            capture.takePicture(
                outputOptions,
                context.mainExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.d("CameraScreen", "Photo saved to ${photoFile.absolutePath}")
                        // Handle the saved photo (e.g., display it or upload it)

                        viewModelScope.launch(Dispatchers.IO) {
                            try {

                                val share = Intent(Intent.ACTION_SEND)
                                val uri = FileProvider.getUriForFile(context, "com.example.duckduckgrid.fileprovider", photoFile)

                                share.putExtra(Intent.EXTRA_STREAM, uri)
                                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                share.setType("image/jpg")
                                startActivity(context, Intent.createChooser(share, "Share Image").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), Bundle.EMPTY)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
                    }
                }
            )
        }
    }
    fun flipCamera(lifecycleOwner: LifecycleOwner) {
        if (lensFacing == CameraSelector.DEFAULT_FRONT_CAMERA) lensFacing = DEFAULT_BACK_CAMERA
        else if (lensFacing == DEFAULT_BACK_CAMERA) lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
        lifecycleOwner.lifecycleScope.launch {
            bindToCamera(lifecycleOwner)
        }
    }

    fun setZoomLevel(zoomLevel: Float) {
        cameraControl?.setLinearZoom(zoomLevel)
    }

    @OptIn(ExperimentalGetImage::class)
    fun analyzeImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            Log.e("MLKit", "No image received from ImageProxy!")
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        Log.d("MLKit", "Processing image frame...")

        objectDetector.process(image)
            .addOnSuccessListener { objects ->
                Log.d("MLKit", "Detected ${objects.size} objects")

                val filteredObjects: MutableList<DetectedObjectData> = ArrayList()
                var duckDetected = false

                for (detectedObject in objects) {
                    // Get the confidence score

                    for (label in detectedObject.labels) {
                        var labelText = label.text
                        Log.d("labels", "Label: $labelText | Confidence: ${label.confidence} | ${label.index}")


                        // If label text is empty, map it manually using predefined labels
                        if (labelText.isEmpty()) {
                            // Assuming detectedObject.trackingId is the class index (it may not always be, check your model output format)
                            var classIndex = 1
                            if((label.index ?: 1) <= 1) {
                                classIndex = label.index ?: -1
                            }
                            if (classIndex >= 0 && classIndex < classLabels.size) {
                                labelText = classLabels[classIndex]
                            }
                        }

                        if(labelText.equals("duck", ignoreCase = true)) {
                            duckDetected = true
                        }

                        Log.d("labels", "Label: $labelText | Confidence: ${detectedObject.labels.first().confidence} | ${detectedObject.labels.first().index}")
                        filteredObjects.add(
                            DetectedObjectData(
                                boundingBox = detectedObject.boundingBox,
                                label = labelText
                            )
                        )
                        _isDuckDetected.value = duckDetected
                    }
                }

                // Update detected objects with the new list of objects with labels and bounding boxes
                _detectedObjects.value = filteredObjects
            }
            .addOnFailureListener { e ->
                Log.e("MLKit", "Detection error: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }


    private fun loadLabelsFromAssets(): List<String> {
        val labels = mutableListOf<String>()
        try {
            val inputStream = context.assets.open("labels.txt")
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    labels.add(line.trim()) // Add each line (label) to the list
                }
            }
        } catch (e: Exception) {
            Log.e("MLKit", "Error loading labels: ${e.message}")
        }
        return labels
    }

}

data class DetectedObjectData(val boundingBox: Rect, val label: String)
