package com.example.duckduckgrid

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
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
        val camera = processCameraProvider.bindToLifecycle(
            lifecycleOwner, lensFacing, cameraPreviewUseCase, imageCapture
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
 }
