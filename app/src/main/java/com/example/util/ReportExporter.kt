package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Customer
import com.example.data.model.PaymentRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun generatePdfReport(
        context: Context,
        title: String,
        customers: List<Customer>,
        payments: List<PaymentRecord>,
        currency: String = "PKR"
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint().apply {
                isAntiAlias = true
            }

            // Header Background
            paint.color = Color.parseColor("#0066FF")
            canvas.drawRect(0f, 0f, 595f, 100f, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("🐬 Dolphin Fiber Network", 30f, 45f, paint)

            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(title, 30f, 75f, paint)

            // Date
            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            paint.textSize = 10f
            canvas.drawText("Generated: $dateStr", 400f, 75f, paint)

            // Table Headers
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            var yPos = 130f
            canvas.drawText("ID / Code", 30f, yPos, paint)
            canvas.drawText("Name", 110f, yPos, paint)
            canvas.drawText("Phone", 240f, yPos, paint)
            canvas.drawText("Package", 350f, yPos, paint)
            canvas.drawText("Fee ($currency)", 450f, yPos, paint)
            canvas.drawText("Status", 530f, yPos, paint)

            paint.color = Color.parseColor("#CBD5E1")
            canvas.drawLine(30f, yPos + 8f, 565f, yPos + 8f, paint)

            yPos += 28f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            paint.color = Color.parseColor("#334155")

            var totalAmount = 0.0

            for (c in customers.take(25)) {
                totalAmount += c.monthlyFee
                val statusStr = if (c.isPaidThisMonth) "PAID" else "UNPAID"
                
                canvas.drawText(c.customerCode.ifEmpty { "DFN-${c.id}" }, 30f, yPos, paint)
                canvas.drawText(c.name.take(18), 110f, yPos, paint)
                canvas.drawText(c.phoneNumber.take(14), 240f, yPos, paint)
                canvas.drawText(c.internetPackage.take(15), 350f, yPos, paint)
                canvas.drawText("%.0f".format(c.monthlyFee), 450f, yPos, paint)

                // Status color
                val statusPaint = Paint(paint).apply {
                    color = if (c.isPaidThisMonth) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(statusStr, 530f, yPos, statusPaint)

                yPos += 22f
            }

            // Summary Footer
            yPos += 15f
            paint.color = Color.parseColor("#0066FF")
            canvas.drawRect(30f, yPos, 565f, yPos + 35f, paint)

            paint.color = Color.WHITE
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Total Count: ${customers.size}", 45f, yPos + 22f, paint)
            canvas.drawText("Total Value: $currency ${"%.0f".format(totalAmount)}", 360f, yPos + 22f, paint)

            pdfDocument.finishPage(page)

            val pdfFile = File(context.cacheDir, "Dolphin_Report_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun generateCsvReport(
        context: Context,
        title: String,
        customers: List<Customer>,
        currency: String = "PKR"
    ): File? {
        try {
            val csvFile = File(context.cacheDir, "Dolphin_Report_${System.currentTimeMillis()}.csv")
            val writer = csvFile.bufferedWriter()

            writer.write("Dolphin Fiber Network - $title\n")
            writer.write("Generated Date: " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()) + "\n\n")
            writer.write("Customer Code,Name,Father Name,Phone,WhatsApp,Address,Area,Package,Monthly Fee ($currency),Due Date Day,Status,Notes\n")

            for (c in customers) {
                val code = c.customerCode.ifEmpty { "DFN-${c.id}" }
                val status = if (c.isPaidThisMonth) "PAID" else "UNPAID"
                writer.write("\"$code\",\"${c.name}\",\"${c.fatherName}\",\"${c.phoneNumber}\",\"${c.whatsappNumber}\",\"${c.address}\",\"${c.area}\",\"${c.internetPackage}\",${c.monthlyFee},${c.dueDateDay},\"$status\",\"${c.notes}\"\n")
            }

            writer.flush()
            writer.close()
            return csvFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Report File")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
