package com.inspection.app.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.inspection.app.data.entity.Address
import com.inspection.app.data.entity.Photo
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {

    private val pageWidth = 595  // A4 寬度 (72 dpi)
    private val pageHeight = 842 // A4 高度 (72 dpi)
    private val margin = 40f

    fun exportPhotos(address: Address, photos: List<Photo>): File {
        val outputDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "鑑定助手"
        )
        if (!outputDir.exists()) outputDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "${address.address}_${timestamp}.pdf")

        val document = PdfDocument()
        var pageNumber = 1
        var currentY = margin

        // 建立第一頁
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // 繪製標題
        val titlePaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
        }
        canvas.drawText("現況鑑定照片紀錄表", margin, currentY + 24f, titlePaint)
        currentY += 50f

        // 繪製地址和日期
        val textPaint = Paint().apply {
            textSize = 14f
            color = Color.BLACK
        }
        canvas.drawText("地址: ${address.address}", margin, currentY, textPaint)
        currentY += 20f
        canvas.drawText("日期: ${SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())}", margin, currentY, textPaint)
        currentY += 30f

        // 照片網格 (2列)
        val photoWidth = (pageWidth - margin * 3) / 2
        val photoHeight = 150f
        var col = 0

        for (photo in photos) {
            // 檢查是否需要換頁
            if (currentY + photoHeight + 30f > pageHeight - margin) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                currentY = margin
            }

            val x = margin + col * (photoWidth + margin)

            // 繪製照片
            val photoFile = File(photo.watermarkedPath)
            if (photoFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                if (bitmap != null) {
                    val scaledBitmap = scaleBitmap(bitmap, photoWidth.toInt(), photoHeight.toInt())
                    canvas.drawBitmap(scaledBitmap, x, currentY, null)
                    scaledBitmap.recycle()
                    bitmap.recycle()
                }
            }

            // 繪製照片資訊
            val infoPaint = Paint().apply {
                textSize = 10f
                color = Color.BLACK
            }
            val infoText = buildString {
                append("#${String.format("%02d", photo.sequence)} ")
                append("${photo.position} ${photo.material}")
                if (photo.crackWidth.isNotEmpty()) append(" 裂縫:${photo.crackWidth}")
                if (photo.peeling) append(" 剝落")
                if (photo.seepage) append(" 滲水")
                if (photo.remark.isNotEmpty()) append(" ${photo.remark}")
            }
            canvas.drawText(infoText, x, currentY + photoHeight + 15f, infoPaint)

            col++
            if (col >= 2) {
                col = 0
                currentY += photoHeight + 30f
            }
        }

        document.finishPage(page)

        // 寫入檔案
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
