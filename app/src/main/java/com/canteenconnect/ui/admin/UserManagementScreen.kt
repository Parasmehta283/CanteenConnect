package com.canteenconnect.ui.admin

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
import com.canteenconnect.data.model.User
import com.canteenconnect.ui.components.PremiumCard
import com.canteenconnect.ui.components.StatusBadge
import com.canteenconnect.viewmodel.CanteenViewModel
import com.canteenconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: CanteenViewModel,
    onBack: () -> Unit
) {
    // For demo purposes, we'll show a sample list of users since the repo doesn't have observeAllUsers yet
    val users = listOf(
        User("1", "Rahul Kumar", "rahul@test.com", "student"),
        User("2", "Priya Sharma", "priya@test.com", "staff"),
        User("3", "Dr. Mehta", "admin@test.com", "admin"),
        User("4", "Ankit Patel", "ankit@test.com", "student"),
        User("5", "Sneha Singh", "sneha@test.com", "student")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Manage campus users and roles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            items(users) { user ->
                UserCard(user)
            }
        }
    }
}

@Composable
fun UserCard(user: User) {
    val roleColor = when (user.role) {
        "admin" -> Color(0xFFE65100)
        "staff" -> Color(0xFF1565C0)
        else -> Color(0xFF455A64)
    }
    
    val roleIcon = when (user.role) {
        "admin" -> Icons.Filled.AdminPanelSettings
        "staff" -> Icons.Filled.Engineering
        else -> Icons.Filled.Person
    }

    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(roleColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(roleIcon, null, tint = roleColor, modifier = Modifier.size(28.dp))
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    user.email,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            StatusBadge(
                user.role.uppercase(),
                containerColor = roleColor.copy(alpha = 0.12f),
                contentColor = roleColor
            )
            
            IconButton(onClick = { }) {
                Icon(Icons.Filled.MoreVert, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }
    }
}
