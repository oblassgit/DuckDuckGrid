package com.example.duckduckgrid

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.Glide
import com.example.duckduckgrid.ui.theme.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheet(url: URL, context: Context, _isFavourite: Boolean, stateFlow: MutableStateFlow<Boolean>): Boolean {

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val visibilityState = stateFlow.collectAsState(false)
    val showBottomSheet by remember { visibilityState }

    var isFavourite by remember { mutableStateOf(_isFavourite) }



    AppTheme {
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            scope.launch {
                                stateFlow.emit(false)

                            }
                        }
                    }
                },
                sheetState = sheetState
            ) {
                // Sheet content

                Row(verticalAlignment = Alignment.CenterVertically) {



                    Button(onClick = {
                        isFavourite = !isFavourite

                    }, modifier = Modifier
                        .padding(Dp(10f))
                        .weight(2f)
                        .height(Dp(50f))) {
                        if (isFavourite) {
                            Image(painter = painterResource(id = R.drawable.ic_star_on), contentDescription = "star on", Modifier.height(
                                Dp(26f)
                            ))
                        } else {
                            Image(painter = painterResource(id = R.drawable.ic_star_off), contentDescription = "star off", Modifier.height(
                                Dp(26f)
                            ))
                        }

                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Button( onClick = {


                        scope.launch(Dispatchers.IO) {
                            try {

                                var bitmap: Bitmap? = null
                                try {
                                    bitmap = Glide.with(context).asBitmap().load(url).submit().get()
                                    Log.d("Image Bitmap" , bitmap.toString())

                                } catch (e: Exception) {
                                    Log.e("Glide Exception", "failed to fetch Bitmap for URL")
                                    e.printStackTrace()
                                }

                                val uri = bitmap?.let { saveImage(it, context) }

                                val share = Intent(Intent.ACTION_SEND)
                                share.putExtra(Intent.EXTRA_STREAM, uri)
                                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                share.setType("image/png")
                                startActivity(context, Intent.createChooser(share, "Share Image"), Bundle.EMPTY)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }


                    }, modifier = Modifier
                        .padding(Dp(10f))
                        .weight(2f)
                        .height(Dp(50f))) {
                        Image(painter = rememberVectorPainter(image = Icons.Rounded.Share), contentDescription = "Share", Modifier.height(
                            Dp(26f)
                        ), colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary))
                    }
                }


            }
        }

    }
    return isFavourite
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(modifier: Modifier = Modifier, viewModel: CameraPreviewViewModel) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        CameraPreviewContent(viewModel, modifier)
    } else {
        Column(
            modifier = modifier.fillMaxSize().wrapContentSize().widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "Whoops! Looks like we need your camera to work our magic!" +
                        "Don't worry, we just wanna see your pretty face (and maybe some cats).  " +
                        "Grant us permission and let's get this party started!"
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Hi there! We need your camera to work our magic! ✨\n" +
                        "Grant us permission and let's get this party started! \uD83C\uDF89"
            }
            Text(textToShow, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Unleash the Camera!")
            }
        }
    }
}

@Composable
fun CameraPreviewContent(
    viewModel: CameraPreviewViewModel,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier
        )
    }
}

private fun saveImage(bitmap: Bitmap, context: Context): Uri? {
    //TODO - Should be processed in another thread
    val imagesFolder = File(context.filesDir, "images")

    var uri: Uri? = null
    try {
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "shared_image.png")

        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        stream.flush()
        stream.close()
        uri = FileProvider.getUriForFile(context, "com.example.duckduckgrid.fileprovider", file)
    } catch (e: IOException) {
        Log.d(TAG, "IOException while trying to write file for sharing: " + e.message)
    }
    return uri
}