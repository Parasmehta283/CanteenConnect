package com.canteenconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.canteenconnect.navigation.CanteenNavGraph
import com.canteenconnect.ui.theme.CanteenConnectTheme
import com.canteenconnect.viewmodel.CanteenViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CanteenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Use edge to edge for a modern immersive look
        enableEdgeToEdge()
        
        setContent {
            CanteenConnectTheme {
                // Surface provides a background color to the app
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CanteenNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}
