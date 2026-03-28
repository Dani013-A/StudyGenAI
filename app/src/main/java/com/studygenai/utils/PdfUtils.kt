package com.studygenai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PdfUtils {

    suspend fun getBitmapsFromPdfUri(context: Context, uri: Uri, maxPages: Int = 3): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        var pdfRenderer: PdfRenderer? = null
        var tempFile: File? = null

        try {
            // Copy URI content to a temporary file since PdfRenderer requires a FileDescriptor
            tempFile = File.createTempFile("temp_pdf", ".pdf", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            val fileDescriptor = android.os.ParcelFileDescriptor.open(tempFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)

            val pageCount = minOf(pdfRenderer.pageCount, maxPages)
            for (i in 0 until pageCount) {
                val page = pdfRenderer.openPage(i)
                // Render with a decent resolution (e.g., 2048x2048 or scaled)
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                // Fill with white background
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfRenderer?.close()
            tempFile?.delete()
        }
        
        bitmaps
    }
}
