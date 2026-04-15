package com.canteenconnect.ui.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.canteenconnect.data.model.Order
import com.canteenconnect.ui.components.PremiumCard
import com.canteenconnect.ui.components.SectionHeader
import com.canteenconnect.viewmodel.CanteenViewModel
import com.canteenconnect.ui.theme.*
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

enum class DateFilter {
    TODAY, LAST_7_DAYS, LAST_30_DAYS, CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: CanteenViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.todayOrders.collectAsState()
    var selectedFilter by remember { mutableStateOf(DateFilter.TODAY) }
    var showCustomDialog by remember { mutableStateOf(false) }
    
    // Load data based on selected filter
    LaunchedEffect(selectedFilter) {
        when (selectedFilter) {
            DateFilter.TODAY -> viewModel.loadTodayOrders()
            DateFilter.LAST_7_DAYS -> {
                val cal = Calendar.getInstance()
                val endDate = Timestamp(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val startDate = Timestamp(cal.time)
                viewModel.loadOrdersByDateRange(startDate, endDate)
            }
            DateFilter.LAST_30_DAYS -> {
                val cal = Calendar.getInstance()
                val endDate = Timestamp(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, -30)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val startDate = Timestamp(cal.time)
                viewModel.loadOrdersByDateRange(startDate, endDate)
            }
            DateFilter.CUSTOM -> {
                showCustomDialog = true
            }
        }
    }

    val totalRevenue = orders.sumOf { it.totalAmount }
    val completedOrders = orders.count { it.status == "COMPLETED" }
    
    val itemCounts = orders.flatMap { it.items }
        .groupBy { it.itemName }
        .mapValues { it.value.sumOf { i -> i.quantity } }
        .entries.sortedByDescending { it.value }

    val periodLabel = when (selectedFilter) {
        DateFilter.TODAY -> "Today"
        DateFilter.LAST_7_DAYS -> "Last 7 Days"
        DateFilter.LAST_30_DAYS -> "Last 30 Days"
        DateFilter.CUSTOM -> "Custom Range"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitchen Intelligence", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = {
                        when (selectedFilter) {
                            DateFilter.TODAY -> viewModel.loadTodayOrders()
                            DateFilter.LAST_7_DAYS -> {
                                val cal = Calendar.getInstance()
                                val endDate = Timestamp(cal.time)
                                cal.add(Calendar.DAY_OF_YEAR, -7)
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                val startDate = Timestamp(cal.time)
                                viewModel.loadOrdersByDateRange(startDate, endDate)
                            }
                            DateFilter.LAST_30_DAYS -> {
                                val cal = Calendar.getInstance()
                                val endDate = Timestamp(cal.time)
                                cal.add(Calendar.DAY_OF_YEAR, -30)
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                val startDate = Timestamp(cal.time)
                                viewModel.loadOrdersByDateRange(startDate, endDate)
                            }
                            DateFilter.CUSTOM -> {}
                        }
                    }) {
                        Icon(Icons.Filled.Refresh, "Refresh", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Date Filter Chips
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Select Period",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            DateFilterChip(
                                label = "Today",
                                icon = Icons.Outlined.Today,
                                selected = selectedFilter == DateFilter.TODAY,
                                onClick = { selectedFilter = DateFilter.TODAY }
                            )
                        }
                        item {
                            DateFilterChip(
                                label = "Last 7 Days",
                                icon = Icons.Outlined.DateRange,
                                selected = selectedFilter == DateFilter.LAST_7_DAYS,
                                onClick = { selectedFilter = DateFilter.LAST_7_DAYS }
                            )
                        }
                        item {
                            DateFilterChip(
                                label = "Last 30 Days",
                                icon = Icons.Outlined.CalendarMonth,
                                selected = selectedFilter == DateFilter.LAST_30_DAYS,
                                onClick = { selectedFilter = DateFilter.LAST_30_DAYS }
                            )
                        }
                        item {
                            DateFilterChip(
                                label = "Custom",
                                icon = Icons.Outlined.EditCalendar,
                                selected = selectedFilter == DateFilter.CUSTOM,
                                onClick = { showCustomDialog = true }
                            )
                        }
                    }
                }
            }

            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EnhancedStatCard(
                        "Total Orders",
                        "${orders.size}",
                        Icons.Outlined.Receipt,
                        Amber500,
                        Modifier.weight(1f)
                    )
                    EnhancedStatCard(
                        "Completed",
                        "$completedOrders",
                        Icons.Outlined.CheckCircle,
                        Emerald500,
                        Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val avgOrderValue = if (orders.isNotEmpty()) totalRevenue / orders.size else 0.0
                    EnhancedStatCard(
                        "Avg Order",
                        "₹${avgOrderValue.toInt()}",
                        Icons.Outlined.TrendingUp,
                        Sky500,
                        Modifier.weight(1f)
                    )
                    val pendingOrders = orders.size - completedOrders
                    EnhancedStatCard(
                        "Pending",
                        "$pendingOrders",
                        Icons.Outlined.PendingActions,
                        Rose500,
                        Modifier.weight(1f)
                    )
                }
            }
            
            // Revenue Card with Gradient
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.CurrencyRupee,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(Modifier.width(20.dp))
                            Column {
                                Text(
                                    "Total Revenue",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "₹%.2f".format(totalRevenue),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    periodLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Order Status Distribution (Pie Chart)
            if (orders.isNotEmpty()) {
                item {
                    Text(
                        "Order Status Distribution",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                
                item {
                    val statusCounts = orders.groupBy { it.status }.mapValues { it.value.size }
                    OrderStatusPieChart(statusCounts, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }
            
            // Category Distribution
            if (orders.isNotEmpty()) {
                item {
                    Text(
                        "Popular Categories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
                
                item {
                    val categoryRevenue = orders.flatMap { order ->
                        order.items.map { it.itemName to it.subtotal }
                    }.groupBy { it.first }
                        .mapValues { it.value.sumOf { pair -> pair.second } }
                        .entries.sortedByDescending { it.value }
                        .take(5)
                    
                    CategoryRevenueChart(
                        categoryRevenue = categoryRevenue,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
            
            item {
                Text(
                    "Top Selling Items",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            
            if (itemCounts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Receipt,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No orders in this period",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(itemCounts.take(10)) { (name, count) ->
                    val maxCount = itemCounts.firstOrNull()?.value ?: 1
                    PremiumCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Column {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            name.firstOrNull()?.toString() ?: "",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "$count Sold",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            LinearProgressIndicator(
                                progress = (count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showCustomDialog) {
        CustomDateRangeDialog(
            onDismiss = { showCustomDialog = false },
            onDateRangeSelected = { startDate, endDate ->
                selectedFilter = DateFilter.CUSTOM
                viewModel.loadOrdersByDateRange(startDate, endDate)
                showCustomDialog = false
            }
        )
    }
}

@Composable
fun DateFilterChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangeDialog(
    onDismiss: () -> Unit,
    onDateRangeSelected: (Timestamp, Timestamp) -> Unit
) {
    var startDaysAgo by remember { mutableStateOf("7") }
    var endDaysAgo by remember { mutableStateOf("0") }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Custom Date Range",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Close")
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Enter number of days ago",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = startDaysAgo,
                    onValueChange = { startDaysAgo = it.filter { c -> c.isDigit() } },
                    label = { Text("Start (days ago)") },
                    leadingIcon = { Icon(Icons.Outlined.CalendarToday, null) },
                    placeholder = { Text("e.g., 30") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = endDaysAgo,
                    onValueChange = { endDaysAgo = it.filter { c -> c.isDigit() } },
                    label = { Text("End (days ago)") },
                    leadingIcon = { Icon(Icons.Outlined.CalendarToday, null) },
                    placeholder = { Text("e.g., 0 for today") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Example: Start=30, End=0 shows last 30 days\nStart=60, End=30 shows orders from 60-30 days ago",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val start = startDaysAgo.toIntOrNull() ?: 7
                            val end = endDaysAgo.toIntOrNull() ?: 0
                            
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, -end)
                            cal.set(Calendar.HOUR_OF_DAY, 23)
                            cal.set(Calendar.MINUTE, 59)
                            cal.set(Calendar.SECOND, 59)
                            val endDate = Timestamp(cal.time)
                            
                            cal.time = Date()
                            cal.add(Calendar.DAY_OF_YEAR, -start)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            val startDate = Timestamp(cal.time)
                            
                            onDateRangeSelected(startDate, endDate)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = startDaysAgo.isNotBlank() && endDaysAgo.isNotBlank()
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
fun ReportStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        modifier = modifier,
        containerColor = color.copy(alpha = 0.05f)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EnhancedStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon, null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OrderStatusPieChart(
    statusCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            val total = statusCounts.values.sum()
            val statusColors = mapOf(
                "PLACED" to Amber500,
                "PREPARING" to Sky500,
                "READY" to Emerald500,
                "COMPLETED" to Color(0xFF10B981)
            )
            
            statusCounts.forEach { (status, count) ->
                val percentage = if (total > 0) (count.toFloat() / total * 100) else 0f
                val color = statusColors[status] ?: MaterialTheme.colorScheme.primary
                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                status,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            "$count (${percentage.toInt()}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = percentage / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = color,
                        trackColor = color.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryRevenueChart(
    categoryRevenue: List<Map.Entry<String, Double>>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            val maxRevenue = categoryRevenue.firstOrNull()?.value ?: 1.0
            val colors = listOf(
                Color(0xFFF59E0B),
                Color(0xFF3B82F6),
                Color(0xFF10B981),
                Color(0xFFEC4899),
                Color(0xFF8B5CF6)
            )
            
            categoryRevenue.forEachIndexed { index, (item, revenue) ->
                val color = colors.getOrElse(index) { MaterialTheme.colorScheme.primary }
                
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                item,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            "₹${revenue.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = color
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = (revenue / maxRevenue).toFloat().coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = color,
                        trackColor = color.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

val Rose500 = Color(0xFFF43F5E)
