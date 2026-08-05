package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Customer
import com.example.ui.theme.StatusPaidGreen
import com.example.ui.theme.StatusUnpaidRed
import com.example.util.QuickActionsHelper

@Composable
fun CustomerDetailDialog(
    customer: Customer,
    currency: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePayment: () -> Unit,
    onToggleSuspend: () -> Unit,
    onViewHistory: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .testTag("customer_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with code & close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = customer.customerCode.ifEmpty { "DFN-${customer.id}" },
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 12.sp
                            )
                        }
                        if (customer.isSuspended) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StatusUnpaidRed.copy(alpha = 0.15f))
                                    .border(1.dp, StatusUnpaidRed, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SUSPENDED",
                                    fontWeight = FontWeight.Bold,
                                    color = StatusUnpaidRed,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusUnpaidRed)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Profile Avatar & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (customer.fatherName.isNotBlank()) {
                            Text(
                                text = "S/O ${customer.fatherName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action Bar
                Text(
                    text = "Quick Actions",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 📞 Call
                    OutlinedButton(
                        onClick = { QuickActionsHelper.makeCall(context, customer.phoneNumber) },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp)
                    }

                    // 💬 WhatsApp
                    OutlinedButton(
                        onClick = { 
                            val msg = "Hello ${customer.name}, your Dolphin Fiber internet fee for ${customer.internetPackage} ($currency ${customer.monthlyFee.toInt()}) is due."
                            QuickActionsHelper.openWhatsApp(context, customer.whatsappNumber.ifEmpty { customer.phoneNumber }, msg)
                        },
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp)
                    }

                    // 📍 Maps
                    OutlinedButton(
                        onClick = { QuickActionsHelper.openGoogleMaps(context, customer.gpsLocation, customer.address) },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Maps", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Status Toggle & History Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTogglePayment,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (customer.isPaidThisMonth) StatusPaidGreen else StatusUnpaidRed
                        ),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(
                            imageVector = if (customer.isPaidThisMonth) Icons.Default.CheckCircle else Icons.Default.Pending,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (customer.isPaidThisMonth) "Mark Unpaid" else "✔ Mark Paid", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onViewHistory,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("History", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Admin Account Status Action (Suspend / Restore)
                OutlinedButton(
                    onClick = onToggleSuspend,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (customer.isSuspended) StatusPaidGreen else StatusUnpaidRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (customer.isSuspended) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (customer.isSuspended) "✔ Activate / Restore Customer" else "⛔ Suspend Customer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                // Detailed Info List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow(icon = Icons.Default.Wifi, label = "Internet Package", value = customer.internetPackage)
                    DetailRow(icon = Icons.Default.Payments, label = "Monthly Fee", value = "$currency ${customer.monthlyFee.toInt()}")
                    DetailRow(icon = Icons.Default.CalendarToday, label = "Due Date", value = "Every month on day ${customer.dueDateDay}")
                    DetailRow(icon = Icons.Default.Phone, label = "Phone Number", value = customer.phoneNumber)
                    DetailRow(icon = Icons.Default.Share, label = "WhatsApp", value = customer.whatsappNumber.ifEmpty { customer.phoneNumber })
                    DetailRow(icon = Icons.Default.Home, label = "Address", value = customer.address.ifEmpty { "Not specified" })
                    DetailRow(icon = Icons.Default.LocationCity, label = "Area", value = customer.area.ifEmpty { "Not specified" })
                    DetailRow(icon = Icons.Default.MyLocation, label = "GPS Location", value = customer.gpsLocation.ifEmpty { "Not set" })
                    DetailRow(icon = Icons.Default.Build, label = "Installation Date", value = customer.installationDate)
                    if (customer.notes.isNotBlank()) {
                        DetailRow(icon = Icons.Default.Notes, label = "Notes", value = customer.notes)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
