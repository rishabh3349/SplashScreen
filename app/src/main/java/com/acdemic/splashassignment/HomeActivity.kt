package com.acdemic.splashassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign

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
    val destinations = listOf(
        "Library", "E303", "D-block", "E-block",
        "A-block", "Prof's Lab", "Incubation Cell",
        "Main Gate", "Back Gate"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(text = "Welcome to IITM RP")
            }
        )

        var textState by remember { mutableStateOf(TextFieldValue("")) }

        BasicTextField(
            value = textState,
            onValueChange = { textState = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFFF5F5DC))
                .padding(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            destinations.forEach { destination ->
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