package com.canteenconnect.ui.student

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canteenconnect.data.model.Order
import com.canteenconnect.data.model.OrderStatus
import com.canteenconnect.ui.components.PremiumButton
import com.canteenconnect.ui.components.PremiumCard
import com.canteenconnect.viewmodel.CanteenViewModel
import com.canteenconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenScreen(
    viewModel: CanteenViewModel,
    onBack: () -> Unit
) {
    val order by viewModel.placedOrder.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Token", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearPlacedOrder()
                        onBack()
                    }) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        order?.let { o ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Token number
                val pulse = rememberInfiniteTransition()
                val scale by pulse.animateFloat(
                    1f, 1.05f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
                )
                
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .border(6.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "ORDER TOKEN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "#${o.tokenNumber.toString().padStart(3, '0')}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // New Order Status Stepper
                OrderHistoryStepper(o.status)

                // Order details
                PremiumCard {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Order Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    o.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        
                        o.items.forEach { item ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.itemName} x${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                                Text("₹%.2f".format(item.subtotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("₹%.2f".format(o.totalAmount), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (o.status == OrderStatus.READY.name) {
                    Surface(
                        Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.NotificationsActive, null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "Your order is ready to collect!",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                
                PremiumButton(
                    text = "Return to Home",
                    onClick = {
                        viewModel.clearPlacedOrder()
                        onBack()
                    },
                    icon = Icons.Filled.Home,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } ?: Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.SentimentDissatisfied, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                Text("No active order session found", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                Spacer(Modifier.height(16.dp))
                PremiumButton(text = "Go Back", onClick = onBack, modifier = Modifier.width(150.dp))
            }
        }
    }
}

@Composable
fun OrderHistoryStepper(currentStatus: String) {
    val steps = listOf("PLACED", "PREPARING", "READY", "COMPLETED")
    val currentIdx = steps.indexOf(currentStatus)

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val done = index <= currentIdx
            val isLast = index == steps.lastIndex
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (index < steps.lastIndex) {
                        Divider(
                            modifier = Modifier
                                .width(64.dp)
                                .offset(x = 32.dp, y = 0.dp),
                            color = if (done && index < currentIdx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 3.dp
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                            .border(
                                3.dp,
                                if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) {
                            Icon(Icons.Filled.Check, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("${index + 1}", color = MaterialTheme.colorScheme.onSurface.copy(0.4f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                Text(
                    step,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (index == currentIdx) FontWeight.Bold else FontWeight.Medium,
                    color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.4f),
                    fontSize = 8.sp
                )
            }
        }
    }
}
