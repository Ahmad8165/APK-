package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.notification.DailyDueReceiver
import com.example.notification.DuePaymentWorker
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    settingsManager: SettingsManager
) {
    val context = LocalContext.current

    val companyName by settingsManager.companyName.collectAsState()
    val currency by settingsManager.currency.collectAsState()
    val isDarkMode by settingsManager.isDarkMode.collectAsState()
    val notificationTime by settingsManager.notificationTime.collectAsState()
    val isAppLockEnabled by settingsManager.isAppLockEnabled.collectAsState()
    val isBiometricEnabled by settingsManager.isBiometricEnabled.collectAsState()

    var editableCompanyName by remember(companyName) { mutableStateOf(companyName) }
    var editableCurrency by remember(currency) { mutableStateOf(currency) }
    var pinInput by remember { mutableStateOf(settingsManager.getAppPin()) }
    var showPinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⚙ App Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // General ISP Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Company Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = editableCompanyName,
                    onValueChange = {
                        editableCompanyName = it
                        settingsManager.setCompanyName(it)
                    },
                    label = { Text("Company Name") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_company_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = editableCurrency,
                    onValueChange = {
                        editableCurrency = it
                        settingsManager.setCurrency(it)
                    },
                    label = { Text("Currency Symbol") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_currency"),
                    singleLine = true
                )
            }
        }

        // Appearance & Preferences
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Appearance & Notifications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Dark Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = if (isDarkMode) "Enabled" else "Disabled",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { settingsManager.setDarkMode(it) },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "WorkManager Due Fee Check", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    text = "Periodic background task (24h interval)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                DuePaymentWorker.triggerImmediateCheck(context)
                                Toast.makeText(context, "WorkManager due payment check triggered!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Check Now", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                DuePaymentWorker.schedulePeriodicCheck(context)
                                DailyDueReceiver.scheduleDailyNotification(context, 9, 0)
                                Toast.makeText(context, "WorkManager periodic task scheduled!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reschedule", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Security Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Security & App Lock",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "App Lock (4-Digit PIN)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = if (isAppLockEnabled) "PIN Lock Active" else "No Lock Set",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isAppLockEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                showPinDialog = true
                            } else {
                                settingsManager.setAppLockEnabled(false)
                            }
                        },
                        modifier = Modifier.testTag("app_lock_switch")
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Fingerprint Unlock", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = "Use device biometric unlock",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { settingsManager.setBiometricEnabled(it) },
                        modifier = Modifier.testTag("fingerprint_switch")
                    )
                }
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit Security PIN") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 4) pinInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Enter 4-Digit PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            settingsManager.setAppPin(pinInput)
                            settingsManager.setAppLockEnabled(true)
                            showPinDialog = false
                            Toast.makeText(context, "App PIN Lock Enabled!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
