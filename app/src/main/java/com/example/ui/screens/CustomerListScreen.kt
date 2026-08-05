package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.Customer
import com.example.ui.theme.*
import com.example.ui.viewmodel.CustomerFilter
import com.example.ui.viewmodel.MainViewModel
import com.example.util.QuickActionsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    viewModel: MainViewModel,
    currency: String,
    onAddCustomer: () -> Unit
) {
    val customers by viewModel.filteredCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCustomer,
                containerColor = DolphinBluePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_customer_list")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar with Frosted Glass background
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search Name, Phone, ID, Area...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DolphinBluePrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = DolphinBluePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_search_input"),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.ALL,
                        onClick = { viewModel.setFilter(CustomerFilter.ALL) },
                        label = { Text("All (${customers.size})") },
                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DolphinBluePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.DUE_TODAY,
                        onClick = { viewModel.setFilter(CustomerFilter.DUE_TODAY) },
                        label = { Text("🔴 Due Today") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusDueTodayOrange,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.DUE_TOMORROW,
                        onClick = { viewModel.setFilter(CustomerFilter.DUE_TOMORROW) },
                        label = { Text("🟡 Due Tomorrow") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusDueTomorrowYellow,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.PAID,
                        onClick = { viewModel.setFilter(CustomerFilter.PAID) },
                        label = { Text("🟢 Paid") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusPaidGreen,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.UNPAID,
                        onClick = { viewModel.setFilter(CustomerFilter.UNPAID) },
                        label = { Text("⚪ Unpaid") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusUnpaidRed,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.SUSPENDED,
                        onClick = { viewModel.setFilter(CustomerFilter.SUSPENDED) },
                        label = { Text("🟠 Suspended (${dashboardMetrics.suspendedCustomersCount})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusDueTodayOrange,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Customers Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try clearing search or filters.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(customers) { customer ->
                        CustomerCard(
                            customer = customer,
                            currency = currency,
                            onCall = { QuickActionsHelper.makeCall(context, customer.phoneNumber) },
                            onWhatsApp = {
                                val msg = "Hello ${customer.name}, your Dolphin Fiber internet fee for ${customer.internetPackage} ($currency ${customer.monthlyFee.toInt()}) is due."
                                QuickActionsHelper.openWhatsApp(context, customer.whatsappNumber.ifEmpty { customer.phoneNumber }, msg)
                            },
                            onMaps = { QuickActionsHelper.openGoogleMaps(context, customer.gpsLocation, customer.address) },
                            onTogglePaid = { viewModel.togglePaymentStatus(customer) },
                            onToggleSuspend = {
                                if (customer.isSuspended) viewModel.activateCustomer(customer)
                                else viewModel.suspendCustomer(customer)
                            },
                            onEdit = {
                                viewModel.showEditCustomer(customer)
                                viewModel.isAddCustomerDialogOpen.value = true
                            },
                            onDelete = { viewModel.showDeleteConfirmation(customer) },
                            onViewHistory = { viewModel.showPaymentsForCustomer(customer) },
                            onCardClick = { viewModel.showCustomerDetail(customer) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(
    customer: Customer,
    currency: String,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onMaps: () -> Unit,
    onTogglePaid: () -> Unit,
    onToggleSuspend: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewHistory: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("customer_card_${customer.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Code, Name, Area, Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DolphinBluePrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = customer.customerCode.ifEmpty { "DFN-${customer.id}" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DolphinBluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = customer.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Area: ${customer.area.ifEmpty { "N/A" }} • Due: Day ${customer.dueDateDay}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Payment / Account Status Badge
                val badgeText = when {
                    customer.isSuspended -> "🚫 SUSPENDED"
                    customer.isPaidThisMonth -> "🟢 PAID"
                    else -> "🔴 UNPAID"
                }
                val badgeColor = when {
                    customer.isSuspended -> StatusUnpaidRed
                    customer.isPaidThisMonth -> StatusPaidGreen
                    else -> StatusUnpaidRed
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Package & Monthly Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Package: ${customer.internetPackage}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$currency ${customer.monthlyFee.toInt()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DolphinBluePrimary
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            // Quick Actions Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // 📞 Call
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DolphinBluePrimary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = DolphinBluePrimary, modifier = Modifier.size(16.dp))
                    }

                    // 💬 WhatsApp
                    IconButton(
                        onClick = onWhatsApp,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(StatusPaidGreen.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "WhatsApp", tint = StatusPaidGreen, modifier = Modifier.size(16.dp))
                    }

                    // 📍 Google Maps
                    IconButton(
                        onClick = onMaps,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Maps", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }

                    // ✏ Edit
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }

                    // 🗑 Delete (with confirmation)
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(StatusUnpaidRed.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusUnpaidRed, modifier = Modifier.size(16.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (customer.isSuspended) {
                        // 1-Tap Activate Button for suspended customers
                        Button(
                            onClick = onToggleSuspend,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPaidGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Activate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // ⛔ Suspend Button for active customers
                        OutlinedButton(
                            onClick = onToggleSuspend,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusUnpaidRed),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Suspend", fontSize = 11.sp)
                        }

                        // ✔ Mark as Paid / Unpaid
                        Button(
                            onClick = onTogglePaid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (customer.isPaidThisMonth) StatusPaidGreen else StatusUnpaidRed
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = if (customer.isPaidThisMonth) Icons.Default.CheckCircle else Icons.Default.Pending,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (customer.isPaidThisMonth) "Unpaid" else "✔ Paid", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
