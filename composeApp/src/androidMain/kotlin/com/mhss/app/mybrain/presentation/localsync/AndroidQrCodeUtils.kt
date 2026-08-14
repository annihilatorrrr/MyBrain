package com.mhss.app.mybrain.presentation.localsync

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import org.koin.core.annotation.Single

@Single
class AndroidQrCodeUtils : QrCodeUtils {

    override fun generateQrCode(content: String, size: Int): KmpBitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        return KmpBitmap(bitmap)
    }

    override fun decodeQrFromBitmap(bitmap: KmpBitmap): String? {
        val rawBitmap = bitmap.bitmap
        val width = rawBitmap.width
        val height = rawBitmap.height
        val pixels = IntArray(width * height)
        rawBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        return try {
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = MultiFormatReader().decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
