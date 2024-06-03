package com.acdemic.splashassignment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.*

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeLaunch()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLaunch() {
    val allDestinations = listOf(
        "Library", "E303", "D-block", "E-block",
        "A-block", "Prof's Lab", "Incubation Cell",
        "Main Gate", "Back Gate", "Canteen"
    )
    var searchQuery by remember { mutableStateOf("") }
    val filteredDestinations = remember(searchQuery) {
        allDestinations.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                askSpeechInput(context, onSpeechResult = { result ->
                    searchQuery = result
                })
            } else {
                Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Welcome to IITM RP",
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_location),
                        contentDescription = "Menu Icon",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Enter Destination") },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(start = 15.dp, end = 15.dp)
                .background(Color(0xFFD9D9D9), RoundedCornerShape(8.dp)),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
            },
            trailingIcon = {
                IconButton(onClick = {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) -> {
                            askSpeechInput(context, onSpeechResult = { result ->
                                searchQuery = result
                            })
                        }
                        else -> {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic Icon")
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(15.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            filteredDestinations.forEach { destination ->
                DestinationItem(destination)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DestinationItem(destination: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5DC))
            .padding(16.dp)
    ) {
        Text(
            text = destination,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
fun isSpeechRecognitionAvailable(context: Context): Boolean {
    val pm = context.packageManager
    val activities: List<ResolveInfo> = pm.queryIntentServices(
        Intent(RecognitionService.SERVICE_INTERFACE),
        0
    )
    activities.forEach { service ->
        Log.d("SpeechInput", "Available service: ${service.serviceInfo.packageName}")
    }
    return activities.isNotEmpty()
}

fun askSpeechInput(context: Context, onSpeechResult: (String) -> Unit) {
    if (!isSpeechRecognitionAvailable(context)) {
        Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        Log.e("SpeechInput", "Speech recognition not available")
    } else {
        Log.d("SpeechInput", "Speech recognition available")
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechInput", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechInput", "Speech beginning")
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d("SpeechInput", "RMS changed: $rmsdB")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("SpeechInput", "Buffer received")
            }

            override fun onEndOfSpeech() {
                Log.d("SpeechInput", "End of speech")
            }

            override fun onError(error: Int) {
                val errorMessage = getErrorText(error)
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                Log.e("SpeechInput", "Error: $errorMessage")
            }

            override fun onResults(results: Bundle?) {
                Log.d("SpeechInput", "Results received")
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onSpeechResult(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("SpeechInput", "Partial results received")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("SpeechInput", "Event received: $eventType")
            }
        })

        speechRecognizer.startListening(intent)
        Log.d("SpeechInput", "Started listening")
    }
}

fun getErrorText(error: Int): String {
    return when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timeout"
        else -> "Unknown error"
    }
}