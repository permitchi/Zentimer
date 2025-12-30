package com.example.zentimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.zentimer.ui.theme.ZentimerTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZentimerTheme {
                AppNav()
                }
            }
        }
    }

@Composable
fun AppNav() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            StartScreen(
                onStartClick = { time, music ->
                    navController.navigate("timer/$time/$music")
                },
            )
        }
        composable("timer/{time}/{music}") { backStackEntry ->
            val time = backStackEntry.arguments?.getString("time") ?: "00:00"
            val music = backStackEntry.arguments?.getString("music") ?: ""
            TimerScreen(
                time = time,
                music = music,
                onBackClick = { navController.popBackStack() })

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(onStartClick: (String, String) -> Unit) {
    val timerOptions = listOf("10:00", "20:00", "30:00")
    var selectedOption by remember { mutableStateOf(timerOptions[0]) }
    var expanded by remember { mutableStateOf(false) }
    val musicOptions = listOf(
        "Epic Battle Theme",
        "Chill LoFi Beats",
        "Upbeat Pop Track",
        "Classical Piano",
        "Rock Anthem",
        "Jazz Lounge",
        "Electronic Dance",
        "Acoustic Guitar",
        "Orchestral Score",
        "Hip Hop Beat"
    )
    var selectedMusic by remember { mutableStateOf(musicOptions[0]) }


    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Zentimer")
                }

            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            timerOptions.forEach { text ->
                val isSelected = text == selectedOption

                //Timer Option
                Row(
                    //posisi buttons options
                ) {
                    Button(
                        onClick = { selectedOption = text },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = text,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            //Dropdown pick music

            Button(onClick = { expanded = !expanded }) {
                Text(selectedMusic)

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .heightIn(max = 150.dp)
                        .zIndex(1f)
                ) {
                    musicOptions.forEach { musicName ->
                        DropdownMenuItem(
                            text = { Text(musicName) },
                            modifier = Modifier.height(56.dp),
                            onClick = {
                                selectedMusic = musicName
                                expanded = false
                            }
                        )
                    }
                }
            }

                //Start Meditasi button
                Button(onClick = { onStartClick(selectedOption, selectedMusic) }) {
                    Text("Start Meditation")
                }
            }
        }
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(time: String, music: String, onBackClick: () -> Unit) {
    val (minutes, seconds) = time.split(':').map { it.toInt() }
    val initialSeconds = minutes * 60 + seconds
    var remainingSeconds by remember { mutableIntStateOf(initialSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        if (remainingSeconds == 0) {
            // auto-stop when finished
            isRunning = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text("Zentimer")

                        IconButton(onBackClick) {
                            Text("X")
                        }
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text(text = formatTime(remainingSeconds))
            Spacer(Modifier.height(16.dp))
            Text(text = "Playing: $music")
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Play/Pause toggle
                Button(
                    onClick = { isRunning = !isRunning },
                ) {
                    Text(if (isRunning) "Pause" else "Play")
                }

                // Stop/Reset
                Button(
                    onClick = {
                        remainingSeconds = initialSeconds
                        isRunning = false
                    }
                ) {
                    Text("Reset")
                }
            }
        }
    }
}


fun formatTime(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return String.format("%02d:%02d", mins, secs)
}
