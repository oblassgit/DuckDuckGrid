package com.example.duckduckgrid.compose

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.duckduckgrid.R
import com.example.duckduckgrid.ui.theme.AppTheme
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