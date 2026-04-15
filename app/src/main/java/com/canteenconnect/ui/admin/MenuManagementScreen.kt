package com.canteenconnect.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.canteenconnect.data.model.MenuItem
import com.canteenconnect.ui.components.PremiumButton
import com.canteenconnect.ui.components.PremiumCard
import com.canteenconnect.viewmodel.CanteenViewModel
import com.canteenconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuManagementScreen(
    viewModel: CanteenViewModel,
    onBack: () -> Unit
) {
    val menuItems by viewModel.menuItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<MenuItem?>(null) }

    // Debug: Log menu items count
    LaunchedEffect(menuItems) {
        println("MenuManagementScreen: ${menuItems.size} items loaded")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Filled.Add, "Add item", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        if (menuItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp)
                ) {
                    Icon(
                        Icons.Outlined.RestaurantMenu,
                        null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "No Menu Items Yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Add your first dish to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add First Dish")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Total Menu Items: ${menuItems.size}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Available: ${menuItems.count { it.available }} | Out of Stock: ${menuItems.count { !it.available }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                items(menuItems, key = { it.id }) { item ->
                    AdminMenuItemCard(
                        item = item,
                        onToggleAvailability = {
                            viewModel.updateMenuItem(item.copy(available = !item.available))
                        },
                        onEdit = { editItem = item },
                        onDelete = { viewModel.deleteMenuItem(item.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        MenuItemDialog(
            viewModel = viewModel,
            item = null,
            onDismiss = { showAddDialog = false },
            onSave = { viewModel.addMenuItem(it); showAddDialog = false }
        )
    }
    editItem?.let { item ->
        MenuItemDialog(
            viewModel = viewModel,
            item = item,
            onDismiss = { editItem = null },
            onSave = { viewModel.updateMenuItem(it); editItem = null }
        )
    }
}

@Composable
fun AdminMenuItemCard(
    item: MenuItem,
    onToggleAvailability: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    PremiumCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Food Image Thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl.trim())
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        error = {
                            Icon(
                                Icons.Outlined.Restaurant,
                                null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    )
                } else {
                    Icon(
                        Icons.Outlined.Restaurant,
                        null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "₹%.2f".format(item.price),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Switch(
                        checked = item.available,
                        onCheckedChange = { onToggleAvailability() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Emerald500)
                    )
                    Text(
                        if (item.available) "In Stock" else "Out Stock",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = if (item.available) Emerald500 else Red500
                    )
                }
                
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDialog(
    viewModel: CanteenViewModel,
    item: MenuItem?,
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var price by remember { mutableStateOf(item?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Main") }
    var imageUrl by remember { mutableStateOf(item?.imageUrl ?: "") }
    var imageUrlInput by remember { mutableStateOf("") }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var useUrlInput by remember { mutableStateOf(false) }
    val categories = listOf("Main", "Snacks", "Beverages", "Desserts")
    val context = LocalContext.current

    val imageUploadLoading by viewModel.imageUploadLoading.collectAsState()
    val uploadedUrl by viewModel.uploadedImageUrl.collectAsState()

    // When upload completes, capture the URL
    LaunchedEffect(uploadedUrl) {
        if (uploadedUrl != null) {
            imageUrl = uploadedUrl!!
            isUploading = false
            viewModel.clearUploadedImageUrl()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Read the image bytes
            val bytes = context.contentResolver.openInputStream(it)?.readBytes()
            if (bytes != null) {
                imageBytes = bytes
                isUploading = true
                imageUrlInput = ""
                useUrlInput = false
                viewModel.uploadMenuImage(bytes)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (item == null) "Add New Dish" else "Edit Dish",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, "Close")
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Dish Name *") },
                        leadingIcon = { Icon(Icons.Outlined.Restaurant, null) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (₹) *") },
                        leadingIcon = { Icon(Icons.Outlined.CurrencyRupee, null) },
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Category *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories.size) { index ->
                                val cat = categories[index]
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 12.sp, maxLines = 1) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                // ─── Image Section ────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Dish Image * (Choose One Method)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        // Toggle between upload and URL
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !useUrlInput,
                                onClick = { useUrlInput = false },
                                label = { Text("Upload Photo") },
                                leadingIcon = { Icon(Icons.Outlined.AddAPhoto, null, Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(12.dp)
                            )
                            FilterChip(
                                selected = useUrlInput,
                                onClick = { useUrlInput = true },
                                label = { Text("Use Image URL") },
                                leadingIcon = { Icon(Icons.Outlined.Link, null, Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        
                        if (useUrlInput) {
                            // URL Input Method
                            OutlinedTextField(
                                value = imageUrlInput,
                                onValueChange = {
                                    imageUrlInput = it
                                    imageUrl = it
                                },
                                label = { Text("Image URL") },
                                placeholder = { Text("https://example.com/image.jpg") },
                                leadingIcon = { Icon(Icons.Outlined.Link, null) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            if (imageUrlInput.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageUrlInput.trim())
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        error = {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Outlined.BrokenImage,
                                                    null,
                                                    modifier = Modifier.size(32.dp),
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    "Failed to load",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            // Upload Method
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = 2.dp,
                                        color = if (imageUrl.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUploading || imageUploadLoading) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 3.dp
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "Uploading...",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else if (imageUrl.isNotEmpty() && !useUrlInput) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(imageUrl.trim())
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Food Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            loading = {
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        strokeWidth = 2.dp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            },
                                            error = {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        Icons.Outlined.BrokenImage,
                                                        null,
                                                        modifier = Modifier.size(32.dp),
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        "Failed to load",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                                    )
                                                }
                                            }
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.Edit,
                                                null,
                                                modifier = Modifier.size(18.dp),
                                                tint = Color.White
                                            )
                                        }
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Outlined.AddAPhoto,
                                            "Upload Image",
                                            modifier = Modifier.size(40.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "Tap to upload image",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            "JPG, PNG up to 5MB",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
                
                item {
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
                                val p = price.toDoubleOrNull() ?: 0.0
                                val finalImageUrl = if (useUrlInput) imageUrlInput.trim() else imageUrl.trim()
                                val newItem = (item ?: MenuItem()).copy(
                                    name = name,
                                    price = p,
                                    category = category,
                                    imageUrl = finalImageUrl
                                )
                                onSave(newItem)
                            },
                            enabled = name.isNotBlank() &&
                                     price.isNotBlank() &&
                                     !isUploading &&
                                     (imageUrl.isNotBlank() || imageUrlInput.isNotBlank()),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Check, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Dish", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                item {
                    Text(
                        "* Required fields",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

val Red500 = Color(0xFFEF5350)
