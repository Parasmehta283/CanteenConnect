package com.canteenconnect.ui.staff

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canteenconnect.data.model.Order
import com.canteenconnect.data.model.OrderStatus
import com.canteenconnect.ui.components.PremiumButton
import com.canteenconnect.ui.components.PremiumCard
import com.canteenconnect.ui.components.StatusBadge
import com.canteenconnect.viewmodel.CanteenViewModel
import com.canteenconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    viewModel: CanteenViewModel,
    onLogout: () -> Unit
) {
    val activeOrders by viewModel.activeOrders.collectAsState()
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitchen Manager", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) { Icon(Icons.Filled.Logout, "Logout", tint = MaterialTheme.colorScheme.error) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // New Stats Overview
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val placed = activeOrders.count { it.status == "PLACED" }
                val preparing = activeOrders.count { it.status == "PREPARING" }
                val ready = activeOrders.count { it.status == "READY" }
                
                StaffModernStat(placed, "Placed", StatusPlaced, Modifier.weight(1f))
                StaffModernStat(preparing, "Cooking", StatusPreparing, Modifier.weight(1f))
                StaffModernStat(ready, "Ready", StatusReady, Modifier.weight(1f))
            }

            if (activeOrders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(100.dp).background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.tertiary)
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "All orders served!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Kitchen is currently empty of pending orders",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = scrollState,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(activeOrders, key = { it.id }) { order ->
                        KitchenOrderCard(
                            order = order,
                            onUpdateStatus = { viewModel.updateOrderStatus(order) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StaffModernStat(count: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    PremiumCard(
        modifier = modifier,
        containerColor = color.copy(alpha = 0.1f)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                "$count",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun KitchenOrderCard(order: Order, onUpdateStatus: () -> Unit) {
    val statusColor = when (order.status) {
        "PLACED" -> StatusPlaced
        "PREPARING" -> StatusPreparing
        "READY" -> StatusReady
        else -> StatusPlaced
    }
    
    val nextActionText = when (order.status) {
        "PLACED" -> "Start Cooking"
        "PREPARING" -> "Mark as Ready"
        "READY" -> "Hand Over Order"
        else -> null
    }

    PremiumCard {
        Column {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "#${order.tokenNumber.toString().padStart(3, '0')}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(Modifier.weight(1f)) {
                    Text(order.studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${order.items.sumOf { it.quantity }} items to prepare",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                StatusBadge(order.status, statusColor.copy(alpha = 0.1f), statusColor)
            }
            
            Spacer(Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(Modifier.height(16.dp))
            
            order.items.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "• ${item.itemName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "×${item.quantity}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (nextActionText != null) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onUpdateStatus,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                ) {
                    Text(nextActionText, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
