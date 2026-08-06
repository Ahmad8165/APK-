package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PaymentRecord
import com.example.ui.theme.DolphinBluePrimary
import com.example.ui.theme.StatusPaidGreen

import com.example.util.PdfReceiptGenerator

@Composable
fun PaymentReceiptDialog(
    receipt: PaymentRecord,
    currency: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .testTag("payment_receipt_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Branding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DolphinBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Dolphin Fiber Network",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DolphinBluePrimary
                            )
                            Text(
                                text = "Official Payment Receipt",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                // Receipt Status & Number
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(StatusPaidGreen.copy(alpha = 0.12f))
                        .border(1.dp, StatusPaidGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusPaidGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PAYMENT RECEIVED",
                            fontWeight = FontWeight.Bold,
                            color = StatusPaidGreen,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Receipt #: ${receipt.receiptNumber.ifEmpty { "RCP-${receipt.id}" }}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Receipt Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReceiptRow("Customer Name", receipt.customerName)
                        ReceiptRow("Customer ID", receipt.customerCode.ifEmpty { "DF-${receipt.customerId}" })
                        ReceiptRow("Package", receipt.internetPackage.ifEmpty { "Fiber Broadband" })
                        ReceiptRow("Billing Month", receipt.monthYear)
                        ReceiptRow("Payment Method", receipt.paymentMethod)
                        ReceiptRow("Collected By", receipt.collectedBy)
                        ReceiptRow("Payment Date", receipt.paymentDate)
                        if (receipt.nextDueDate.isNotBlank()) {
                            ReceiptRow("Next Due Date", receipt.nextDueDate)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Paid Amount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currency ${receipt.amount.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = StatusPaidGreen
                            )
                        }
                    }
                }

                if (receipt.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Note: ${receipt.notes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // PDF Export & Open Button
                        OutlinedButton(
                            onClick = {
                                val pdfFile = PdfReceiptGenerator.generatePdfReceipt(context, receipt, currency)
                                PdfReceiptGenerator.openPdfFile(context, pdfFile)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DolphinBluePrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF Receipt", fontWeight = FontWeight.Bold)
                        }

                        // PDF Share Button
                        Button(
                            onClick = {
                                val pdfFile = PdfReceiptGenerator.generatePdfReceipt(context, receipt, currency)
                                PdfReceiptGenerator.sharePdfFile(context, pdfFile, receipt.customerName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DolphinBluePrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_pdf_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF", fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close")
                        }

                        Button(
                            onClick = { shareReceipt(context, receipt, currency) },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPaidGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_receipt_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TextSnippet, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Text", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun shareReceipt(context: Context, receipt: PaymentRecord, currency: String) {
    val message = """
        🧾 *Dolphin Fiber Network - Payment Receipt*
        ------------------------------------------
        *Receipt #:* ${receipt.receiptNumber.ifEmpty { "RCP-${receipt.id}" }}
        *Customer Name:* ${receipt.customerName}
        *Customer ID:* ${receipt.customerCode.ifEmpty { "DF-${receipt.customerId}" }}
        *Package:* ${receipt.internetPackage}
        *Billing Month:* ${receipt.monthYear}
        *Amount Paid:* $currency ${receipt.amount.toInt()}
        *Payment Method:* ${receipt.paymentMethod}
        *Payment Date:* ${receipt.paymentDate}
        *Next Due Date:* ${receipt.nextDueDate}
        ------------------------------------------
        Thank you for choosing Dolphin Fiber Network!
        For support: +92 300 1234567
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Receipt Via"))
}
