package com.canteenconnect.ui.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canteenconnect.ui.theme.Amber500
import com.canteenconnect.ui.theme.Amber600
import com.canteenconnect.viewmodel.CanteenViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: CanteenViewModel,
    onNavToLogin: () -> Unit,
    onNavToDashboard: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAuthChecked by viewModel.isAuthChecked.collectAsState()
    var startAnimation by remember { mutableStateOf(false) }
    
    val scale = animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.8f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
    )
    
    val alpha = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1500)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(isAuthChecked) {
        if (isAuthChecked) {
            delay(1500) // branding delay
            val user = currentUser
            if (user != null) {
                onNavToDashboard(user.role)
            } else {
                onNavToLogin()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimaryContainer)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "CanteenConnect",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.scale(scale.value)
            )
            
            Text(
                "Campus Food, Simplified",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = alpha.value),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
