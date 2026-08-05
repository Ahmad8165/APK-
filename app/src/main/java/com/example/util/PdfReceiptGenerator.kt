package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.PaymentRecord
import java.io.File
import java.io.FileOutputStream

object PdfReceiptGenerator {

    fun generatePdfReceipt(context: Context, receipt: PaymentRecord, currency: String): File {
        val pdfDocument = PdfDocument()

        // Standard A4 dimensions (595 x 842 points at 72 dpi)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Paints
        val paint = Paint().apply { isAntiAlias = true }
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#E0E6ED")
            textSize = 12f
        }
        val headerLabelPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1E293B")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#334155")
            textSize = 11f
        }
        val boldBodyPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0F172A")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val greenPaidPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#16A34A")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // 1. Top Header Banner Background (Navy Blue)
        paint.color = Color.parseColor("#0F4C81") // Dolphin Blue
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 110f, paint)

        // Header Title
        canvas.drawText("DOLPHIN FIBER NETWORK", 40f, 45f, titlePaint)
        canvas.drawText("OFFICIAL PAYMENT RECEIPT & INVOICE", 40f, 68f, subtitlePaint)
        canvas.drawText("High-Speed Fiber Broadband Solutions", 40f, 86f, subtitlePaint)

        // Top-right Header Accent Box
        paint.color = Color.parseColor("#1E3A8A")
        val rcpHeaderBox = RectF(380f, 25f, 555f, 85f)
        canvas.drawRoundRect(rcpHeaderBox, 10f, 10f, paint)

        val rcpNumText = receipt.receiptNumber.ifEmpty { "RCP-${receipt.id}" }
        val whiteTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("RECEIPT NO:", 392f, 45f, subtitlePaint)
        canvas.drawText(rcpNumText, 392f, 65f, whiteTextPaint)

        // 2. Verified Paid Status Badge
        paint.color = Color.parseColor("#DCFCE7") // Light green
        val statusRect = RectF(40f, 125f, 555f, 175f)
        canvas.drawRoundRect(statusRect, 12f, 12f, paint)

        paint.color = Color.parseColor("#16A34A")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(statusRect, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        canvas.drawText("✔ PAYMENT RECEIVED & VERIFIED", 60f, 155f, greenPaidPaint)

        val dateStr = "Date: ${receipt.paymentDate}"
        canvas.drawText(dateStr, 400f, 155f, boldBodyPaint)

        // 3. Customer & Service Details Section
        val startY = 200f

        // Card Container Background
        paint.color = Color.parseColor("#F8FAFC")
        val cardRect = RectF(40f, startY, 555f, startY + 160f)
        canvas.drawRoundRect(cardRect, 12f, 12f, paint)

        paint.color = Color.parseColor("#E2E8F0")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(cardRect, 12f, 12f, paint)
        paint.style = Paint.Style.FILL

        canvas.drawText("CUSTOMER & SUBSCRIPTION INFORMATION", 55f, startY + 25f, headerLabelPaint)

        // Divider
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawLine(55f, startY + 35f, 540f, startY + 35f, paint)

        // Details Grid
        val col1X = 55f
        val col1ValX = 180f
        val col2X = 320f
        val col2ValX = 430f

        var currentY = startY + 58f
        val lineSpacing = 22f

        // Row 1
        canvas.drawText("Customer Name:", col1X, currentY, bodyPaint)
        canvas.drawText(receipt.customerName, col1ValX, currentY, boldBodyPaint)

        canvas.drawText("Customer ID:", col2X, currentY, bodyPaint)
        canvas.drawText(receipt.customerCode.ifEmpty { "DF-${receipt.customerId}" }, col2ValX, currentY, boldBodyPaint)

        // Row 2
        currentY += lineSpacing
        canvas.drawText("Package:", col1X, currentY, bodyPaint)
        canvas.drawText(receipt.internetPackage.ifEmpty { "Fiber Broadband" }, col1ValX, currentY, boldBodyPaint)

        canvas.drawText("Billing Month:", col2X, currentY, bodyPaint)
        canvas.drawText(receipt.monthYear, col2ValX, currentY, boldBodyPaint)

        // Row 3
        currentY += lineSpacing
        canvas.drawText("Payment Method:", col1X, currentY, bodyPaint)
        canvas.drawText(receipt.paymentMethod, col1ValX, currentY, boldBodyPaint)

        canvas.drawText("Collected By:", col2X, currentY, bodyPaint)
        canvas.drawText(receipt.collectedBy, col2ValX, currentY, boldBodyPaint)

        // Row 4
        currentY += lineSpacing
        canvas.drawText("Payment Date:", col1X, currentY, bodyPaint)
        canvas.drawText(receipt.paymentDate, col1ValX, currentY, boldBodyPaint)

        if (receipt.nextDueDate.isNotBlank()) {
            canvas.drawText("Next Due Date:", col2X, currentY, bodyPaint)
            canvas.drawText(receipt.nextDueDate, col2ValX, currentY, boldBodyPaint)
        }

        // 4. Invoice Items Table Header
        val tableStartY = startY + 185f
        paint.color = Color.parseColor("#0F4C81")
        val tableHeaderRect = RectF(40f, tableStartY, 555f, tableStartY + 30f)
        canvas.drawRoundRect(tableHeaderRect, 6f, 6f, paint)

        val tableHeaderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("DESCRIPTION", 55f, tableStartY + 20f, tableHeaderPaint)
        canvas.drawText("BILLING PERIOD", 280f, tableStartY + 20f, tableHeaderPaint)
        canvas.drawText("AMOUNT ($currency)", 430f, tableStartY + 20f, tableHeaderPaint)

        // Table Row Content
        val rowY = tableStartY + 55f
        canvas.drawText("Monthly Fiber Internet Fee (${receipt.internetPackage})", 55f, rowY, bodyPaint)
        canvas.drawText(receipt.monthYear, 280f, rowY, bodyPaint)
        canvas.drawText("$currency ${receipt.amount.toInt()}", 430f, rowY, boldBodyPaint)

        // Horizontal Line
        paint.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(40f, rowY + 15f, 555f, rowY + 15f, paint)

        // Total Summary Box
        val totalBoxY = rowY + 30f
        paint.color = Color.parseColor("#F1F5F9")
        val totalRect = RectF(280f, totalBoxY, 555f, totalBoxY + 45f)
        canvas.drawRoundRect(totalRect, 8f, 8f, paint)

        canvas.drawText("TOTAL PAID AMOUNT:", 295f, totalBoxY + 28f, boldBodyPaint)

        val bigAmountPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#16A34A")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("$currency ${receipt.amount.toInt()}", 430f, totalBoxY + 28f, bigAmountPaint)

        // Notes Section
        if (receipt.notes.isNotBlank()) {
            val notesY = totalBoxY + 70f
            canvas.drawText("Note:", 40f, notesY, headerLabelPaint)
            canvas.drawText(receipt.notes, 80f, notesY, bodyPaint)
        }

        // 5. Official Footer & Stamp Line
        val footerY = 740f

        paint.color = Color.parseColor("#94A3B8")
        canvas.drawLine(40f, footerY, 555f, footerY, paint)

        val footerTextPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#64748B")
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("Thank you for choosing Dolphin Fiber Network for your high-speed internet!", 297f, footerY + 20f, footerTextPaint)
        canvas.drawText("This is a system-generated official payment receipt. No physical signature required.", 297f, footerY + 35f, footerTextPaint)
        canvas.drawText("For Customer Support & Inquiries: +92 300 1234567 | support@dolphinfiber.net", 297f, footerY + 50f, footerTextPaint)

        pdfDocument.finishPage(page)

        // Write to Cache PDF File
        val pdfFile = File(context.cacheDir, "Receipt_${receipt.receiptNumber.ifEmpty { "RCP-${receipt.id}" }}.pdf")
        try {
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        } finally {
            pdfDocument.close()
        }

        return pdfFile
    }

    fun openPdfFile(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(viewIntent, "Open Payment Receipt PDF"))
    }

    fun sharePdfFile(context: Context, pdfFile: File, customerName: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Dolphin Fiber Network Payment Receipt - $customerName")
            putExtra(Intent.EXTRA_TEXT, "Please find attached the official payment receipt for $customerName from Dolphin Fiber Network.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Receipt PDF"))
    }
}
