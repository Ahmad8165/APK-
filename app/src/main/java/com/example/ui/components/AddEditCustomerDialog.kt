package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Customer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerDialog(
    customer: Customer?,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var customerCode by remember { mutableStateOf(customer?.customerCode ?: "") }
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var fatherName by remember { mutableStateOf(customer?.fatherName ?: "") }
    var phone by remember { mutableStateOf(customer?.phoneNumber ?: "") }
    var whatsapp by remember { mutableStateOf(customer?.whatsappNumber ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var area by remember { mutableStateOf(customer?.area ?: "Gulberg") }
    var gpsLocation by remember { mutableStateOf(customer?.gpsLocation ?: "") }
    var internetPackage by remember { mutableStateOf(customer?.internetPackage ?: "20 Mbps Fiber") }
    var monthlyFee by remember { mutableStateOf(customer?.monthlyFee?.toInt()?.toString() ?: "2500") }
    var dueDateDay by remember { mutableStateOf(customer?.dueDateDay?.toString() ?: "5") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }

    var showError by remember { mutableStateOf(false) }

    val packageOptions = listOf("10 Mbps Fiber", "20 Mbps Fiber", "30 Mbps Fiber", "50 Mbps Fiber Ultra", "100 Mbps Corporate", "Custom Fiber")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .testTag("add_edit_customer_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (customer == null) "➕ Add New Customer" else "✏ Edit Customer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (showError) {
                        Text(
                            text = "⚠ Please fill required fields (Name, Phone)",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedTextField(
                        value = customerCode,
                        onValueChange = { customerCode = it },
                        label = { Text("Customer ID (e.g. DF-0001)") },
                        placeholder = { Text("Auto-generated if empty (DF-0001)") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_code"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_name"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("Father Name") },
                        leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; if (whatsapp.isEmpty()) whatsapp = it },
                        label = { Text("Phone Number *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_phone"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp Number") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Full Address") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Area / Sector") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = gpsLocation,
                        onValueChange = { gpsLocation = it },
                        label = { Text("GPS Coordinates (e.g. 31.5204, 74.3587)") },
                        leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Internet Package selection
                    Text(
                        text = "Internet Package",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = internetPackage,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                packageOptions.forEach { pkg ->
                                    DropdownMenuItem(
                                        text = { Text(pkg) },
                                        onClick = {
                                            internetPackage = pkg
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = monthlyFee,
                            onValueChange = { monthlyFee = it.filter { char -> char.isDigit() } },
                            label = { Text("Monthly Fee ($currency)") },
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                            modifier = Modifier.weight(1f).testTag("input_customer_fee"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = dueDateDay,
                            onValueChange = { 
                                val num = it.filter { char -> char.isDigit() }.toIntOrNull()
                                if (num == null || num in 1..31) {
                                    dueDateDay = it
                                }
                            },
                            label = { Text("Due Date (1-31)") },
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Special Instructions") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank()) {
                                showError = true
                                return@Button
                            }
                            val updated = (customer ?: Customer(name = "", phoneNumber = "")).copy(
                                customerCode = customerCode.trim(),
                                name = name.trim(),
                                fatherName = fatherName.trim(),
                                phoneNumber = phone.trim(),
                                whatsappNumber = whatsapp.trim().ifEmpty { phone.trim() },
                                address = address.trim(),
                                area = area.trim(),
                                gpsLocation = gpsLocation.trim(),
                                internetPackage = internetPackage,
                                monthlyFee = monthlyFee.toDoubleOrNull() ?: 2500.0,
                                dueDateDay = dueDateDay.toIntOrNull() ?: 5,
                                notes = notes.trim()
                            )
                            onSave(updated)
                        },
                        modifier = Modifier.testTag("save_customer_button")
                    ) {
                        Text(if (customer == null) "Save Customer" else "Update Customer")
                    }
                }
            }
        }
    }
}
