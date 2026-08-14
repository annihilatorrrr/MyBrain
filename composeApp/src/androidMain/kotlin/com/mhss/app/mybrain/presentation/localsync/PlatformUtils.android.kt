package com.mhss.app.mybrain.presentation.localsync

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

actual class KmpBitmap(val bitmap: Bitmap)

@Composable
actual fun KmpImage(
    bitmap: KmpBitmap,
    contentDescription: String?,
    modifier: Modifier
) {
    Image(
        bitmap = bitmap.bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier
    )
}

@Composable
actual fun rememberQrScanLauncher(onResult: (KmpBitmap?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val tempPhotoFile = remember {
        File(context.cacheDir, "temp_qr_code_snap.jpg")
    }
    val tempPhotoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempPhotoFile
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565
                inSampleSize = 2
            }
            val bitmap = BitmapFactory.decodeFile(tempPhotoFile.absolutePath, options)
            if (bitmap != null) {
                onResult(KmpBitmap(bitmap))
            } else {
                onResult(null)
            }
            tempPhotoFile.delete()
        }
    }

    return {
        tempPhotoFile.delete()
        takePictureLauncher.launch(tempPhotoUri)
    }
}
