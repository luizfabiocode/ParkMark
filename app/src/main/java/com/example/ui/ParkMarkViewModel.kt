package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ParkMarkDatabase
import com.example.data.model.ParkingSpot
import com.example.data.repository.ParkingRepository
import com.example.data.repository.ParkingRepositoryImpl
import com.example.location.LocationClient
import com.example.location.UserLocationData
import com.example.network.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ParkMarkUiState(
    val savedSpot: ParkingSpot? = null,
    val currentLocation: UserLocationData? = null,
    val isSaving: Boolean = false,
    val isGpsEnabled: Boolean = true,
    val isOnline: Boolean = true,
    val distanceMeters: Float? = null,
    val formattedDistance: String = "--",
    val walkingTimeEstimate: String = "--",
    val bearingDegrees: Float? = null,
    val isLowAccuracy: Boolean = false,
    val currentAccuracyMeters: Float = 0f,
    val currentNotes: String = "",
    val currentLevelOrRow: String = ""
)

class ParkMarkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ParkingRepository
    private val locationClient: LocationClient = LocationClient(application)
    private val networkMonitor: NetworkMonitor = NetworkMonitor(application)

    private val _currentLocation = MutableStateFlow<UserLocationData?>(null)
    val currentLocation: StateFlow<UserLocationData?> = _currentLocation.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isGpsEnabled = MutableStateFlow(locationClient.isGpsEnabled())
    val isGpsEnabled: StateFlow<Boolean> = _isGpsEnabled.asStateFlow()

    private val _isOnline = MutableStateFlow(networkMonitor.isOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _localNotes = MutableStateFlow("")
    val localNotes: StateFlow<String> = _localNotes.asStateFlow()

    private val _localLevel = MutableStateFlow("")
    val localLevel: StateFlow<String> = _localLevel.asStateFlow()

    private var locationUpdatesJob: Job? = null

    init {
        val db = ParkMarkDatabase.getInstance(application)
        repository = ParkingRepositoryImpl(db.parkingSpotDao())

        // Monitor Network Connectivity
        viewModelScope.launch {
            networkMonitor.isOnlineFlow.collect { online ->
                _isOnline.value = online
            }
        }
    }

    val savedSpot: StateFlow<ParkingSpot?> = repository.getSavedSpot()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val baseLocationAndSpotFlow = combine(
        savedSpot,
        _currentLocation,
        _isSaving,
        _isGpsEnabled,
        _isOnline
    ) { spot, currentLoc, saving, gpsEnabled, online ->
        var distance: Float? = null
        var formattedDist = "--"
        var walkingTime = "--"
        var bearing: Float? = null

        if (spot != null && currentLoc != null) {
            val dist = LocationClient.calculateDistanceMeters(
                startLat = currentLoc.latitude,
                startLng = currentLoc.longitude,
                endLat = spot.latitude,
                endLng = spot.longitude
            )
            distance = dist
            formattedDist = LocationClient.formatDistance(dist)
            walkingTime = LocationClient.formatWalkingTime(dist)
            bearing = LocationClient.calculateBearing(
                startLat = currentLoc.latitude,
                startLng = currentLoc.longitude,
                endLat = spot.latitude,
                endLng = spot.longitude
            )
        }

        val accuracy = currentLoc?.accuracy ?: spot?.accuracyMeters ?: 0f
        val isLowAcc = accuracy > 20f

        ParkMarkUiState(
            savedSpot = spot,
            currentLocation = currentLoc,
            isSaving = saving,
            isGpsEnabled = gpsEnabled,
            isOnline = online,
            distanceMeters = distance,
            formattedDistance = formattedDist,
            walkingTimeEstimate = walkingTime,
            bearingDegrees = bearing,
            isLowAccuracy = isLowAcc,
            currentAccuracyMeters = accuracy,
            currentNotes = spot?.notes ?: "",
            currentLevelOrRow = spot?.levelOrRow ?: ""
        )
    }

    val uiState: StateFlow<ParkMarkUiState> = combine(
        baseLocationAndSpotFlow,
        _localNotes,
        _localLevel
    ) { baseState, notes, level ->
        val spot = baseState.savedSpot
        val effectiveNotes = if (notes.isNotEmpty() || spot == null) notes else spot.notes
        val effectiveLevel = if (level.isNotEmpty() || spot == null) level else spot.levelOrRow

        baseState.copy(
            currentNotes = effectiveNotes,
            currentLevelOrRow = effectiveLevel
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ParkMarkUiState()
    )

    fun onPermissionGranted() {
        _hasLocationPermission.value = true
        checkGpsStatus()
        fetchLocationOnce()
        startLocationUpdates()
    }

    fun onPermissionDenied() {
        _hasLocationPermission.value = false
    }

    fun checkGpsStatus() {
        _isGpsEnabled.value = locationClient.isGpsEnabled()
    }

    fun fetchLocationOnce() {
        viewModelScope.launch {
            checkGpsStatus()
            val loc = locationClient.getFreshLocation()
            if (loc != null) {
                _currentLocation.value = loc
            }
        }
    }

    fun startLocationUpdates() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            try {
                locationClient.getLocationUpdates(intervalMs = 2000L).collect { loc ->
                    _currentLocation.value = loc
                }
            } catch (e: Exception) {
                // Catch any permission or provider errors safely
            }
        }
    }

    fun stopLocationUpdates() {
        locationUpdatesJob?.cancel()
    }

    fun saveParkingSpot(notes: String = "", levelOrRow: String = "") {
        viewModelScope.launch {
            checkGpsStatus()
            if (!_isGpsEnabled.value) {
                _snackbarEvent.emit("O GPS está desativado. Por favor, ative a localização.")
                return@launch
            }

            _isSaving.value = true
            var loc = _currentLocation.value

            // If we don't have a fresh location, get one immediately
            if (loc == null || System.currentTimeMillis() - loc.timestamp > 15000) {
                loc = locationClient.getFreshLocation()
            }

            if (loc != null) {
                _currentLocation.value = loc
                val finalNotes = if (notes.isNotEmpty()) notes else _localNotes.value
                val finalLevel = if (levelOrRow.isNotEmpty()) levelOrRow else _localLevel.value

                val spot = ParkingSpot(
                    id = 1,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracyMeters = loc.accuracy,
                    timestamp = System.currentTimeMillis(),
                    notes = finalNotes.trim(),
                    levelOrRow = finalLevel.trim()
                )
                repository.saveSpot(spot)
                _localNotes.value = spot.notes
                _localLevel.value = spot.levelOrRow
                _snackbarEvent.emit("Vaga Salva com Sucesso!")
            } else {
                _snackbarEvent.emit("Não foi possível obter a localização GPS atual. Tente novamente.")
            }
            _isSaving.value = false
        }
    }

    fun updateNotes(notes: String) {
        _localNotes.value = notes
        viewModelScope.launch {
            repository.updateNotes(notes.trim())
        }
    }

    fun updateLevelOrRow(levelOrRow: String) {
        _localLevel.value = levelOrRow
        viewModelScope.launch {
            repository.updateLevelOrRow(levelOrRow.trim())
        }
    }

    fun clearSpot() {
        viewModelScope.launch {
            repository.clearSpot()
            _localNotes.value = ""
            _localLevel.value = ""
            _snackbarEvent.emit("Vaga de estacionamento limpa")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
