package com.inspection.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.inspection.app.data.entity.Address
import com.inspection.app.data.entity.Photo
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PdfExporter(private val context: Context) {

    fun exportPhotos(address: Address, photos: List<Photo>): File {
        val outputDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "鑑定助手"
        )
        if (!outputDir.exists()) outputDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(outputDir, "${address.address}_${timestamp}.pdf")

        val writer = PdfWriter(outputFile)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc, PageSize.A4)

        // 設定邊距
        document.setMargins(36f, 36f, 36f, 36f)

        // 標題
        document.add(
            Paragraph("現況鑑定照片紀錄表")
                .setFontSize(18f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
        )

        // 地址資訊
        document.add(
            Paragraph("地址: ${address.address}")
                .setFontSize(12f)
                .setMarginTop(10f)
        )

        document.add(
            Paragraph("日期: ${SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())}")
                .setFontSize(12f)
        )

        // 照片表格 (2列)
        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
            .useAllAvailableWidth()
            .setMarginTop(20f)

        for (photo in photos) {
            val cell = createPhotoCell(photo)
            table.addCell(cell)
        }

        // 如果是奇數張照片，補一個空白格
        if (photos.size % 2 != 0) {
            table.addCell(Cell().setBorder(null))
        }

        document.add(table)

        document.close()
        return outputFile
    }

    private fun createPhotoCell(photo: Photo): Cell {
        val cell = Cell()
            .setPadding(5f)
            .setBorder(null)

        // 讀取照片
        val photoFile = File(photo.watermarkedPath)
        if (photoFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            val scaledBitmap = scaleBitmap(bitmap, 400)

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val imageData = ImageDataFactory.create(outputStream.toByteArray())

            val image = Image(imageData)
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setMaxWidth(UnitValue.createPercentValue(95f))

            cell.add(image)

            bitmap.recycle()
            scaledBitmap.recycle()
        }

        // 照片資訊
        val infoText = StringBuilder()
        infoText.append("#${String.format("%02d", photo.sequence)} ")
        infoText.append("${photo.position} ${photo.material}")

        if (!photo.crackWidth.isNullOrEmpty()) {
            infoText.append(" 裂縫:${photo.crackWidth}")
        }
        if (photo.peeling) infoText.append(" 剝落")
        if (photo.seepage) infoText.append(" 滲水")
        if (!photo.remark.isNullOrEmpty()) {
            infoText.append(" ${photo.remark}")
        }

        cell.add(
            Paragraph(infoText.toString())
                .setFontSize(9f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5f)
        )

        return cell
    }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val ratio = minOf(
            maxSize.toFloat() / bitmap.width,
            maxSize.toFloat() / bitmap.height
        )
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
