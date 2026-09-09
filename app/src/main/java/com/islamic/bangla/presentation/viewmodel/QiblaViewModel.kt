package com.islamic.bangla.presentation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class QiblaUiState(
    /** Compass azimuth in degrees (0 = North). */
    val azimuth: Float = 0f,
    /** Qibla bearing in degrees clockwise from North. */
    val qiblaBearing: Double = DEFAULT_BEARING,
    val locationLabel: String = "ঢাকা (ডিফল্ট)",
    val hasLocation: Boolean = false,
    val sensorAvailable: Boolean = true,
    val accuracyLow: Boolean = false
) {
    companion object {
        // Great-circle bearing from Dhaka to the Kaaba (used until real location loads).
        const val DEFAULT_BEARING = 277.5
    }
}

@HiltViewModel
class QiblaViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel(), SensorEventListener {

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var started = false

    fun start() {
        if (started) return
        started = true
        if (accelerometer == null || magnetometer == null) {
            _uiState.value = _uiState.value.copy(sensorAvailable = false)
            return
        }
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        loadLocation()
    }

    fun stop() {
        if (!started) return
        started = false
        sensorManager.unregisterListener(this)
    }

    fun refreshLocation() = loadLocation()

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun loadLocation() {
        if (!hasLocationPermission()) return
        try {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        _uiState.value = _uiState.value.copy(
                            qiblaBearing = qiblaBearing(it.latitude, it.longitude),
                            locationLabel = "আপনার অবস্থান",
                            hasLocation = true
                        )
                    }
                }
        } catch (e: SecurityException) {
            // Permission revoked — keep default bearing.
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
        }
        val g = gravity ?: return
        val m = geomagnetic ?: return
        val rotation = FloatArray(9)
        val inclination = FloatArray(9)
        if (SensorManager.getRotationMatrix(rotation, inclination, g, m)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotation, orientation)
            var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (azimuthDeg < 0) azimuthDeg += 360f
            _uiState.value = _uiState.value.copy(azimuth = azimuthDeg)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            _uiState.value = _uiState.value.copy(
                accuracyLow = accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    companion object {
        private const val KAABA_LAT = 21.4225
        private const val KAABA_LON = 39.8262

        /** Great-circle bearing from (lat, lon) to the Kaaba, in degrees. */
        fun qiblaBearing(lat: Double, lon: Double): Double {
            val latRad = Math.toRadians(lat)
            val dLon = Math.toRadians(KAABA_LON - lon)
            val y = sin(dLon)
            val x = cos(latRad) * Math.tan(Math.toRadians(KAABA_LAT)) -
                sin(latRad) * cos(dLon)
            return (Math.toDegrees(atan2(y, x)) + 360) % 360
        }
    }
}
