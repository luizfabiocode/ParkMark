package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParkingSpot
import com.example.ui.ParkMarkUiState
import com.example.ui.components.ParkingRadarMap
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SafetyOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FindScreen(
    uiState: ParkMarkUiState,
    savedSpot: ParkingSpot,
    onUpdateNotes: (String) -> Unit,
    onClearSpot: () -> Unit,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf(savedSpot.notes) }

    LaunchedEffect(savedSpot.notes) {
        if (notesText.isEmpty() && savedSpot.notes.isNotEmpty()) {
            notesText = savedSpot.notes
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Car Saved badge & Clear button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(SafetyOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Carro Estacionado",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Veículo Marcado",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    val timeFormatted = remember(savedSpot.timestamp) {
                        val timeAgo = DateUtils.getRelativeTimeSpanString(
                            savedSpot.timestamp,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        )
                        val exactTime = SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(Date(savedSpot.timestamp))
                        "$timeAgo • $exactTime"
                    }
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            // Clear Spot button (top right header action)
            OutlinedButton(
                onClick = { showClearDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFF87171)
                ),
                border = BorderStroke(1.dp, Color(0xFF7F1D1D)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("clear_spot_header_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Limpar",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Limpar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Low Accuracy GPS Warning if accuracy > 20 meters
        if (uiState.isLowAccuracy && uiState.currentLocation != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF2E1C07),
                border = BorderStroke(1.dp, WarningAmber)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Baixa Precisão",
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aguardando melhor precisão do GPS...",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFFFDE68A),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Precisão atual: ±${uiState.currentAccuracyMeters.toInt()}m.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFCD34D)
                            )
                        )
                    }
                    IconButton(
                        onClick = onRefreshLocation,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar GPS",
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Offline Banner
        if (!uiState.isOnline) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF475569))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Modo Offline",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Modo Offline: O mapa pode não carregar imagens, mas o radar e a navegação no Google Maps continuam funcionando.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        // Live Radar / Map View
        ParkingRadarMap(
            savedSpot = savedSpot,
            currentLocation = uiState.currentLocation,
            distanceMeters = uiState.distanceMeters,
            bearingDegrees = uiState.bearingDegrees,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        // Telemetry Highlights Card (Walking Distance & Duration Estimate)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = ElectricBluePrimary),
            shape = RoundedCornerShape(20.dp),
            color = DarkCardSurface,
            border = BorderStroke(1.5.dp, DarkCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Caminhada",
                            tint = ElectricBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "DISTÂNCIA A PÉ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = uiState.formattedDistance,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    )
                }

                // Estimated Walking Time Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.dp, ElectricBluePrimary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Tempo",
                                tint = ElectricBluePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "TEMPO ESTIM.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = uiState.walkingTimeEstimate,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = ElectricBluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // PRIMARY ACTION: "Open in Google Maps" Button
        Button(
            onClick = {
                launchGoogleMapsDirections(context, savedSpot.latitude, savedSpot.longitude)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = ElectricBluePrimary)
                .testTag("open_in_google_maps_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricBluePrimary,
                contentColor = Color(0xFF001F2B)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Ícone do Mapa",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Abrir no Google Maps",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                )
            }
        }

        // Notes & Level/Row Card with instant auto-save
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = DarkCardSurface,
            border = BorderStroke(1.dp, DarkCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Notas",
                            tint = ElectricBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Notas e Detalhes da Vaga",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (savedSpot.levelOrRow.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, DarkCardBorder)
                        ) {
                            Text(
                                text = savedSpot.levelOrRow,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectricBluePrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = {
                        notesText = it
                        onUpdateNotes(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_text_field"),
                    placeholder = {
                        Text("Digite notas (ex: Piso 4, Vaga G, perto do pilar amarelo)", color = Color(0xFF64748B))
                    },
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = ElectricBluePrimary,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Saved GPS Accuracy and Coordinates Metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "GPS Salvo: ${String.format("%.5f, %.5f", savedSpot.latitude, savedSpot.longitude)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                    )
                    Text(
                        text = "Precisão: ±${savedSpot.accuracyMeters.toInt()}m",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                    )
                }
            }
        }

        // Secondary Action: "Clear Spot" Button (Found Car Reset)
        Button(
            onClick = { showClearDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("clear_spot_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFFFCA5A5)
            ),
            border = BorderStroke(1.dp, Color(0xFF7F1D1D))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Carro Encontrado",
                    tint = SuccessGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Encontrei Meu Carro • Limpar Vaga",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Clear Spot Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = DarkCardSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Limpar Vaga Salva?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Isso irá redefinir as coordenadas e notas salvas para que você possa salvar uma nova vaga da próxima vez.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCBD5E1))
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDialog = false
                        onClearSpot()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_clear_spot_button")
                ) {
                    Text("Limpar Vaga", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

private fun launchGoogleMapsDirections(context: Context, latitude: Double, longitude: Double) {
    try {
        // Preferred Native Walking Navigation Intent
        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=w")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
            return
        }
    } catch (e: Exception) {
        // Fall back to universal browser/maps intent
    }

    try {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&travelmode=walking")
        val browserIntent = Intent(Intent.ACTION_VIEW, webUri)
        context.startActivity(browserIntent)
    } catch (e: Exception) {
        // Generic Geo fallback
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Saved+Parking+Spot)")
        context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
    }
}
