package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ParkMarkUiState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.SafetyOrange
import com.example.ui.theme.SafetyOrangeDark
import com.example.ui.theme.SafetyOrangeLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkScreen(
    uiState: ParkMarkUiState,
    onSaveSpot: (notes: String, levelOrRow: String) -> Unit,
    onRefreshLocation: () -> Unit,
    onEnableGpsClick: () -> Unit,
    onRequestPermissions: () -> Unit,
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedLevelChip by remember { mutableStateOf("") }
    var quickNotes by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "ButtonPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header Brand
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
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
                        .background(
                            Brush.linearGradient(
                                listOf(ElectricBluePrimary, SafetyOrange)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "ParkMark Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "ParkMark",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "Marcador de Vagas com GPS de Alta Precisão",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            // GPS Signal Pill Indicator
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.isGpsEnabled) DarkCardSurface else Color(0xFF3B1212),
                border = BorderStroke(1.dp, if (uiState.isGpsEnabled) DarkCardBorder else Color(0xFFEF4444))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isGpsEnabled) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                        contentDescription = "Status do GPS",
                        tint = if (uiState.isGpsEnabled) ElectricBluePrimary else Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (uiState.isGpsEnabled) {
                            if (uiState.currentLocation != null) "GPS Pronto" else "Buscando sinal..."
                        } else "GPS Desativado",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (uiState.isGpsEnabled) Color(0xFFCBD5E1) else Color(0xFFFCA5A5),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Offline Notification Banner if disconnected
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
                        text = "Modo Offline: O salvamento do local por GPS e a rota a pé no Google Maps continuam funcionando normalmente.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        // GPS Disabled Alert Banner
        if (!uiState.isGpsEnabled) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEnableGpsClick() },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF381A1A),
                border = BorderStroke(1.dp, Color(0xFFEF4444))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "GPS Desativado",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Serviços de Localização Desativados",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Toque aqui para ativar o GPS para máxima precisão.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFCA5A5)
                            )
                        )
                    }
                }
            }
        }

        // Low Accuracy GPS Warning if accuracy > 20 meters
        if (uiState.isGpsEnabled && uiState.isLowAccuracy && uiState.currentLocation != null) {
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
                            text = "Precisão atual: ±${uiState.currentAccuracyMeters.toInt()}m. Aproxime-se de céu aberto ou da entrada da garagem para melhor sinal.",
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

        Spacer(modifier = Modifier.height(10.dp))

        // THE GIANT HIGH-CONTRAST "SAVE MY PARKING SPOT" BUTTON (SCREEN 1 HERO)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            // Radiant ambient glow ring
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .scale(pulseScale)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                SafetyOrange.copy(alpha = 0.35f),
                                SafetyOrangeLight.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            // Giant Circle Button
            Button(
                onClick = {
                    if (!hasLocationPermission) {
                        onRequestPermissions()
                    } else {
                        val level = selectedLevelChip.ifEmpty { "Térreo" }
                        onSaveSpot(quickNotes, level)
                    }
                },
                modifier = Modifier
                    .size(210.dp)
                    .shadow(16.dp, CircleShape, spotColor = SafetyOrange, ambientColor = SafetyOrangeDark)
                    .testTag("save_parking_spot_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafetyOrange
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Fixando GPS...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Ícone Salvar Vaga",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Salvar Minha\nVaga",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 26.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Floor / Level Selector Chips
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Andar ou Setor",
                    tint = ElectricBluePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Andar / Setor Rápido (Opcional):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            val commonLevels = listOf("Térreo", "Piso 1", "Piso 2", "Piso 3", "Subsolo 1", "Subsolo 2", "Cobertura", "Setor A", "Setor B")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                commonLevels.forEach { level ->
                    val isSelected = selectedLevelChip == level
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedLevelChip = if (isSelected) "" else level
                        },
                        label = {
                            Text(
                                text = level,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DarkCardSurface,
                            labelColor = Color(0xFF94A3B8),
                            selectedContainerColor = ElectricBluePrimary,
                            selectedLabelColor = Color(0xFF001F2B)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DarkCardBorder,
                            selectedBorderColor = ElectricBluePrimary
                        )
                    )
                }
            }
        }

        // Optional Quick Spot Notes Input
        OutlinedTextField(
            value = quickNotes,
            onValueChange = { quickNotes = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_notes_input"),
            placeholder = {
                Text(
                    text = "ex: Próximo ao Pilar 42B, Setor Amarelo...",
                    color = Color(0xFF64748B)
                )
            },
            label = {
                Text("Notas da Vaga (Opcional)")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Notas",
                    tint = ElectricBluePrimary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkCardSurface,
                unfocusedContainerColor = DarkCardSurface,
                focusedBorderColor = ElectricBluePrimary,
                unfocusedBorderColor = DarkCardBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = ElectricBluePrimary,
                unfocusedLabelColor = Color(0xFF94A3B8)
            )
        )

        // Live GPS Telemetry Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            shape = RoundedCornerShape(18.dp),
            color = DarkCardSurface,
            border = BorderStroke(1.dp, DarkCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "GPS ao Vivo",
                            tint = ElectricBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Sensor GPS em Tempo Real",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    IconButton(
                        onClick = onRefreshLocation,
                        modifier = Modifier.size(28.dp).testTag("refresh_gps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar GPS",
                            tint = ElectricBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (uiState.currentLocation != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "LATITUDE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = String.format("%.6f°", uiState.currentLocation.latitude),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Column {
                            Text(
                                text = "LONGITUDE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = String.format("%.6f°", uiState.currentLocation.longitude),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFE2E8F0),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "PRECISÃO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "±${uiState.currentLocation.accuracy.toInt()}m",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (uiState.currentLocation.accuracy <= 10f) SuccessGreen else if (uiState.currentLocation.accuracy <= 20f) ElectricBluePrimary else WarningAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = ElectricBluePrimary
                        )
                        Text(
                            text = "Localizando satélites GPS...",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }
            }
        }
    }
}
