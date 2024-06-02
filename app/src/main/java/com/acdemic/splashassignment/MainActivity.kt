package com.acdemic.splashassignment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acdemic.splashassignment.ui.theme.SplashAssignmentTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import androidx.core.app.ActivityOptionsCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashAssignmentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    //get current location
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val locationState = remember { mutableStateOf<Location?>(null) }
    val isAtIITMRP = remember { mutableStateOf(false) }
    val message = remember { mutableStateOf("") }
    val context = LocalContext.current
    //location permission
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    when (locationPermissionState.status) {
        is PermissionStatus.Granted -> {
            GetCurrentLocation { location ->
                locationState.value = location
                //for current location or location from where 200m is needed
                isAtIITMRP.value = isWithin200Meters(location, 21.7159202, 73.0264064)
                message.value = if (isAtIITMRP.value) "You are at IITM RP" else "The location is not supported"
            }
        }

        is PermissionStatus.Denied -> {
            message.value = "Location permission denied"
        }
    }

    SplashScreenContent(message.value)

    if (locationPermissionState.status is PermissionStatus.Granted && !isAtIITMRP.value) {
        LaunchedEffect(Unit) {
            delay(3000)
            Toast.makeText(context, "Closing the app...", Toast.LENGTH_SHORT).show()
            (context as? ComponentActivity)?.finish()
        }
    }
    if (isAtIITMRP.value) {
        LaunchedEffect(Unit) {
            delay(3000)
            val intent = Intent(context, HomeActivity::class.java)
            //adding animations
            val options = ActivityOptionsCompat.makeCustomAnimation(
                context,
                R.anim.slide_in_bottom,
                R.anim.slide_out_top
            )
            context.startActivity(intent, options.toBundle())
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun GetCurrentLocation(onLocationReceived: (Location) -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationRequest = remember {
        LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.firstOrNull()?.let {
                    onLocationReceived(it)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}

@Composable
fun SplashScreenContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val painter: Painter = painterResource(id = R.drawable.img)
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 75.dp)
                .padding(start = 95.dp)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, constraints.maxHeight) {
                        placeable.placeRelative(0, constraints.maxHeight - placeable.height)
                    }
                }
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 22.sp,
                fontFamily = FontFamily.SansSerif,
            )
        }
    }
}

fun isWithin200Meters(currentLocation: Location, targetLat: Double, targetLon: Double): Boolean {
    val targetLocation = Location("").apply {
        latitude = targetLat
        longitude = targetLon
    }
    val distance = currentLocation.distanceTo(targetLocation)
    return distance <= 200
}
