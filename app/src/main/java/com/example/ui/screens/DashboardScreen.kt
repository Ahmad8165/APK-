package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.ui.theme.*
import com.example.ui.viewmodel.CustomerFilter
import com.example.ui.viewmodel.DashboardMetrics
import com.example.ui.viewmodel.MainViewModel
import com.example.util.QuickActionsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    metrics: DashboardMetrics,
    currency: String,
    onNavigateToCustomers: (CustomerFilter) -> Unit,
    onAddCustomer: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCustomer,
                containerColor = DolphinBluePrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_customer")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Frosted Glass Banner Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        DolphinBluePrimary,
                                        DolphinBlueDark
                                    )
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.08f),
                                radius = size.width * 0.45f,
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.15f)
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.05f),
                                radius = size.width * 0.25f,
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.85f)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🐬 Dolphin Fiber",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(StatusPaidGreen)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ONLINE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Personal ISP Fee & Billing Management",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "MONTHLY COLLECTION",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.8f),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$currency ${metrics.monthlyCollection.toInt()}",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${metrics.paidCustomersCount}/${metrics.totalCustomers} Clear",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val collectionRatio = if (metrics.totalCustomers > 0) {
                                (metrics.paidCustomersCount.toFloat() / metrics.totalCustomers.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Collection Target Progress",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "${(collectionRatio * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { collectionRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = FrostedCyanAccent,
                                    trackColor = Color.White.copy(alpha = 0.25f)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Search Bar with Frosted Glass styling
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.setSearchQuery(it)
                        if (it.isNotEmpty()) {
                            onNavigateToCustomers(CustomerFilter.ALL)
                        }
                    },
                    placeholder = { Text("Search by Name, Phone, ID or Area...") },
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
                        .testTag("quick_search_bar"),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )
            }

            // Financial Collections Overview (6 Core Requested Metrics)
            item {
                Text(
                    text = "📊 Revenue & Financial Summary",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: Total Collection & Today's Collection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricGlassCard(
                            title = "💰 Total Collection",
                            value = "$currency ${metrics.totalCollection.toInt()}",
                            subtitle = "All-Time Recorded Revenue",
                            accentColor = StatusPaidGreen,
                            modifier = Modifier.weight(1f)
                        )

                        MetricGlassCard(
                            title = "📅 Today's Collection",
                            value = "$currency ${metrics.todayCollectionAmount.toInt()}",
                            subtitle = "Collected Today",
                            accentColor = DolphinBluePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: This Month Collection & Pending Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricGlassCard(
                            title = "🗓 This Month Collection",
                            value = "$currency ${metrics.monthlyCollection.toInt()}",
                            subtitle = "Current Month Revenue",
                            accentColor = DolphinBluePrimary,
                            modifier = Modifier.weight(1f)
                        )

                        MetricGlassCard(
                            title = "⏳ Pending Amount",
                            value = "$currency ${metrics.pendingAmount.toInt()}",
                            subtitle = "${metrics.unpaidCustomersCount} Customers Pending",
                            accentColor = StatusUnpaidRed,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToCustomers(CustomerFilter.UNPAID) }
                        )
                    }

                    // Row 3: Paid Customers & Unpaid Customers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricGlassCard(
                            title = "🟢 Paid Customers",
                            value = "${metrics.paidCustomersCount}",
                            subtitle = "Clear This Month",
                            accentColor = StatusPaidGreen,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToCustomers(CustomerFilter.PAID) }
                        )

                        MetricGlassCard(
                            title = "🔴 Unpaid Customers",
                            value = "${metrics.unpaidCustomersCount}",
                            subtitle = "Pending Fee Collection",
                            accentColor = StatusUnpaidRed,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToCustomers(CustomerFilter.UNPAID) }
                        )
                    }
                }
            }

            // Today's Collection List Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 Today's Collection List (${metrics.todayCollectionList.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { onNavigateToCustomers(CustomerFilter.DUE_TODAY) }) {
                        Text("View All", fontSize = 13.sp, color = DolphinBluePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (metrics.todayCollectionList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusPaidGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "No Collections Due Today!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "All customer dues for today are fully clear.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(metrics.todayCollectionList) { customer ->
                    CustomerQuickItemCard(
                        customer = customer,
                        currency = currency,
                        onCall = { QuickActionsHelper.makeCall(context, customer.phoneNumber) },
                        onWhatsApp = {
                            val msg = "Hello ${customer.name}, your Dolphin Fiber internet fee ($currency ${customer.monthlyFee.toInt()}) is due today."
                            QuickActionsHelper.openWhatsApp(context, customer.whatsappNumber.ifEmpty { customer.phoneNumber }, msg)
                        },
                        onMarkPaid = { viewModel.togglePaymentStatus(customer) },
                        onSelect = { viewModel.showCustomerDetail(customer) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun MetricGlassCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomerQuickItemCard(
    customer: Customer,
    currency: String,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onMarkPaid: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (customer.isPaidThisMonth) StatusPaidGreen.copy(alpha = 0.15f)
                                else StatusUnpaidRed.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (customer.isPaidThisMonth) "PAID" else "UNPAID",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (customer.isPaidThisMonth) StatusPaidGreen else StatusUnpaidRed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${customer.internetPackage} • $currency ${customer.monthlyFee.toInt()} • Area: ${customer.area}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = DolphinBluePrimary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onWhatsApp, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "WhatsApp", tint = StatusPaidGreen, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onMarkPaid, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (customer.isPaidThisMonth) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Mark Paid",
                        tint = if (customer.isPaidThisMonth) StatusPaidGreen else StatusUnpaidRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

