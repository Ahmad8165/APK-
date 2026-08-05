package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DolphinBluePrimary
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusUnpaidRed
import com.example.ui.viewmodel.DashboardMetrics
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ReportsScreen(
    viewModel: MainViewModel,
    metrics: DashboardMetrics,
    currency: String
) {
    val context = LocalContext.current
    var selectedReportType by remember { mutableStateOf("Monthly Report") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "📊 Reports & Analytics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Report Type Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Monthly Report", "Pending Customers", "Paid Customers").forEach { type ->
                FilterChip(
                    selected = selectedReportType == type,
                    onClick = { selectedReportType = type },
                    label = { Text(type, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Visual Collection Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Collection Ratio Breakdown",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Canvas Chart
                        val totalAmount = (metrics.monthlyCollection + metrics.pendingAmount).coerceAtLeast(1.0)
                        val paidRatio = (metrics.monthlyCollection / totalAmount).toFloat()
                        val unpaidRatio = (metrics.pendingAmount / totalAmount).toFloat()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(110.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 22f
                                    val radius = (size.minDimension - strokeWidth) / 2
                                    val center = Offset(size.width / 2, size.height / 2)

                                    val paidSweep = paidRatio * 360f
                                    val unpaidSweep = unpaidRatio * 360f

                                    // Paid segment
                                    drawArc(
                                        color = StatusPaidGreen,
                                        startAngle = -90f,
                                        sweepAngle = paidSweep,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                    )

                                    // Unpaid segment
                                    drawArc(
                                        color = StatusUnpaidRed,
                                        startAngle = -90f + paidSweep,
                                        sweepAngle = unpaidSweep,
                                        useCenter = false,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                                    )
                                }
                                Text(
                                    text = "${(paidRatio * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = StatusPaidGreen
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                LegendItem(
                                    color = StatusPaidGreen,
                                    label = "Paid Dues",
                                    value = "$currency ${metrics.monthlyCollection.toInt()}"
                                )
                                LegendItem(
                                    color = StatusUnpaidRed,
                                    label = "Pending Dues",
                                    value = "$currency ${metrics.pendingAmount.toInt()}"
                                )
                                LegendItem(
                                    color = DolphinBluePrimary,
                                    label = "Total Dues Target",
                                    value = "$currency ${totalAmount.toInt()}"
                                )
                            }
                        }
                    }
                }
            }

            // Report Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Summary: $selectedReportType",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val count = when (selectedReportType) {
                            "Paid Customers" -> metrics.paidCustomersCount
                            "Pending Customers" -> metrics.unpaidCustomersCount
                            else -> metrics.totalCustomers
                        }

                        val totalVal = when (selectedReportType) {
                            "Paid Customers" -> metrics.monthlyCollection
                            "Pending Customers" -> metrics.pendingAmount
                            else -> metrics.monthlyCollection + metrics.pendingAmount
                        }

                        ReportDetailLine("Total Customer Count", "$count Customers")
                        ReportDetailLine("Total Revenue Value", "$currency ${totalVal.toInt()}")
                        ReportDetailLine("Collection Rate", "${((metrics.paidCustomersCount.toFloat() / metrics.totalCustomers.coerceAtLeast(1)) * 100).toInt()}%")
                        ReportDetailLine("Due Today Pending", "${metrics.dueTodayCount} Dues")
                        ReportDetailLine("Due Tomorrow Pending", "${metrics.dueTomorrowCount} Dues")
                    }
                }
            }

            // Export Actions Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📄 Export Official Report",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generate printable PDF or Excel (CSV) files to share or print.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val pdfFile = viewModel.exportReportPdf(selectedReportType)
                                    if (pdfFile != null) {
                                        viewModel.shareReportFile(pdfFile, "application/pdf")
                                    } else {
                                        Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_pdf_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export PDF")
                            }

                            Button(
                                onClick = {
                                    val csvFile = viewModel.exportReportCsv(selectedReportType)
                                    if (csvFile != null) {
                                        viewModel.shareReportFile(csvFile, "text/csv")
                                    } else {
                                        Toast.makeText(context, "Failed to generate CSV", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPaidGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_excel_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export Excel")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReportDetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
