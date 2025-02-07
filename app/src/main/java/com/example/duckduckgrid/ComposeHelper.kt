package com.example.duckduckgrid

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.takeOrElse
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.UUID

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
fun CameraPreviewScreen(modifier: Modifier = Modifier, viewModel: CameraPreviewViewModel, context: Context) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val imagePermissionState = rememberPermissionState(android.Manifest.permission.READ_MEDIA_IMAGES)
    val scope = rememberCoroutineScope()


    AppTheme {

        if (cameraPermissionState.status.isGranted) {
            Box {

                val pickMedia = rememberLauncherForActivityResult (ActivityResultContracts.PickVisualMedia()) { uri ->
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("PhotoPicker", "Selected URI: $uri")
                    } else {
                        Log.d("PhotoPicker", "No media selected")
                    }
                }

                CameraPreviewContent(viewModel, modifier)

                //Add from gallery button

                /*Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Start) {
                    ExtendedFloatingActionButton ( onClick = {

                        if(imagePermissionState.status.isGranted) {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            imagePermissionState.launchPermissionRequest()
                        }



                    }, icon = {Icon(Icons.Rounded.Add, "FAB")}, text = { Text("Pick from Gallery")}, modifier = Modifier.wrapContentSize()
                        .padding(vertical = 30.dp, horizontal = 8.dp))
                }*/

                Box(Modifier.align(Alignment.BottomCenter)) {

                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

                            FlipCameraButton(viewModel)
                            CaptureButton(viewModel, Modifier.weight(3f, true))
                        }
                        ZoomButtons(viewModel)

                    }
                }

            }
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
}

@Composable
fun CameraPreviewContent(
    viewModel: CameraPreviewViewModel,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()

    val detectedObjects by viewModel.detectedObjects.collectAsState()

    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(lifecycleOwner)
    }

    var autofocusRequest by remember { mutableStateOf(UUID.randomUUID() to Offset.Unspecified) }

    val autofocusRequestId = autofocusRequest.first
    // Show the autofocus indicator if the offset is specified
    val showAutofocusIndicator = autofocusRequest.second.isSpecified
    // Cache the initial coords for each autofocus request
    val autofocusCoords = remember(autofocusRequestId) { autofocusRequest.second }

    // Queue hiding the request for each unique autofocus tap
    if (showAutofocusIndicator) {
        LaunchedEffect(autofocusRequestId) {
            delay(1000)
            // Clear the offset to finish the request and hide the indicator
            autofocusRequest = autofocusRequestId to Offset.Unspecified
        }
    }

    surfaceRequest?.let { request ->
        val coordinateTransformer = remember { MutableCoordinateTransformer() }
        CameraXViewfinder(
            surfaceRequest = request,
            coordinateTransformer = coordinateTransformer,
            modifier = modifier.pointerInput(viewModel, coordinateTransformer) {
                detectTapGestures { tapCoords ->
                    with(coordinateTransformer) {
                        viewModel.tapToFocus(tapCoords.transform())
                    }
                    autofocusRequest = UUID.randomUUID() to tapCoords
                }
            }
        )

        detectedObjects.forEach { obj ->
            Log.d("detected objects", obj.toString())
            Box(
                Modifier
                    .offset { IntOffset(obj.boundingBox.left, obj.boundingBox.top) }
                    .size(obj.boundingBox.width().dp, obj.boundingBox.height().dp)
                    .border(2.dp, Color.Red, shape = RoundedCornerShape(4.dp))
            ) {
                Text(obj.label, color = Color.White, modifier = Modifier.padding(4.dp))
            }
        }

        AnimatedVisibility(
            visible = showAutofocusIndicator,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .offset { autofocusCoords.takeOrElse { Offset.Zero } .round() }
                .offset((-24).dp, (-24).dp)
        ) {
            Spacer(Modifier.border(2.dp, Color.White, CircleShape).size(48.dp))
        }
    }
}

fun saveImage(bitmap: Bitmap, context: Context): Uri? {
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

suspend fun getImagesFromGallery(contentResolver: ContentResolver): List<Media> = withContext(Dispatchers.IO) {
    val projection = arrayOf(
        Images.Media._ID,
        Images.Media.DISPLAY_NAME,
        Images.Media.SIZE,
        Images.Media.MIME_TYPE,
    )

    val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Query all the device storage volumes instead of the primary only
        Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        Images.Media.EXTERNAL_CONTENT_URI
    }

    val images = mutableListOf<Media>()

    contentResolver.query(
        collectionUri,
        projection,
        null,
        null,
        "${Images.Media.DATE_ADDED} DESC"
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(Images.Media._ID)
        val displayNameColumn = cursor.getColumnIndexOrThrow(Images.Media.DISPLAY_NAME)
        val sizeColumn = cursor.getColumnIndexOrThrow(Images.Media.SIZE)
        val mimeTypeColumn = cursor.getColumnIndexOrThrow(Images.Media.MIME_TYPE)

        while (cursor.moveToNext()) {
            val uri = ContentUris.withAppendedId(collectionUri, cursor.getLong(idColumn))
            val name = cursor.getString(displayNameColumn)
            val size = cursor.getLong(sizeColumn)
            val mimeType = cursor.getString(mimeTypeColumn)

            val image = Media(uri, name, size, mimeType)
            images.add(image)
        }
    }

    return@withContext images
}

@Composable
fun CaptureButton(viewModel: CameraPreviewViewModel, modifier: Modifier = Modifier) {


    Box {
        FloatingActionButton(
            onClick = {

                viewModel.captureImage()
            },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(Icons.Rounded.Share, "Share")
        }

    }

}

@Composable
fun FlipCameraButton(viewModel: CameraPreviewViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    Button(onClick = {
        viewModel.flipCamera(lifecycleOwner)
    }) {
        Icon(Icons.Rounded.Refresh, "flip camera")
    }
}

@Composable
fun ZoomButtons(viewModel: CameraPreviewViewModel, modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("1x", "2x", "3x")

    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { selectedIndex = index

                          when(selectedIndex) {
                              0 -> viewModel.setZoomLevel(0f)
                              1 -> viewModel.setZoomLevel(0.5f)
                              2 -> viewModel.setZoomLevel(1f)
                          }

                          },
                selected = index == selectedIndex,
                label = { Text(label) }
            )
        }
    }
}

data class Media(
    val uri: Uri,
    val name: String,
    val size: Long,
    val mimeType: String,
)