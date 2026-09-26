package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ParkMarkViewModel
import com.example.ui.screens.FindScreen
import com.example.ui.screens.ParkScreen
import com.example.ui.screens.PermissionRationaleDialog
import com.example.ui.theme.ParkMarkTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ParkMarkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ParkMarkTheme {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }
                var showRationaleDialog by remember { mutableStateOf(false) }

                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                    val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

                    if (fineLocationGranted || coarseLocationGranted) {
                        viewModel.onPermissionGranted()
                        showRationaleDialog = false
                    } else {
                        viewModel.onPermissionDenied()
                        showRationaleDialog = true
                    }
                }

                // Check permissions on start and resume
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            val fineGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            val coarseGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                            if (fineGranted || coarseGranted) {
                                viewModel.onPermissionGranted()
                            } else {
                                viewModel.onPermissionDenied()
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                LaunchedEffect(Unit) {
                    val fineGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarseGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (fineGranted || coarseGranted) {
                        viewModel.onPermissionGranted()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                // Handle Snackbar feedback
                LaunchedEffect(Unit) {
                    viewModel.snackbarEvent.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = uiState.savedSpot != null,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "ParkMarkScreenTransition",
                        modifier = Modifier.padding(innerPadding)
                    ) { isSaved ->
                        if (isSaved && uiState.savedSpot != null) {
                            FindScreen(
                                uiState = uiState,
                                savedSpot = uiState.savedSpot!!,
                                onUpdateNotes = { viewModel.updateNotes(it) },
                                onClearSpot = { viewModel.clearSpot() },
                                onRefreshLocation = { viewModel.fetchLocationOnce() }
                            )
                        } else {
                            ParkScreen(
                                uiState = uiState,
                                onSaveSpot = { notes, level ->
                                    viewModel.saveParkingSpot(notes, level)
                                },
                                onRefreshLocation = { viewModel.fetchLocationOnce() },
                                onEnableGpsClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val appSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(appSettings)
                                    }
                                },
                                onRequestPermissions = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                hasLocationPermission = hasLocationPermission
                            )
                        }
                    }

                    if (showRationaleDialog) {
                        PermissionRationaleDialog(
                            onGrantClick = {
                                showRationaleDialog = false
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            onDismiss = { showRationaleDialog = false }
                        )
                    }
                }
            }
        }
    }
}
