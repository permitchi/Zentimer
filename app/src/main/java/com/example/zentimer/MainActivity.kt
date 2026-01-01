package com.example.zentimer

import android.content.ContentResolver
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.zentimer.ui.theme.ZentimerTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay
import android.net.Uri
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.graphics.component1
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.style.TextAlign
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.zentimer.R

val MyCustomFont = FontFamily(
    Font(R.font.poppins_medium, FontWeight.Normal),
    Font(R.font.star_cartoon, FontWeight.Bold)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

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
    val timerOptions = listOf("05:00", "10:00", "15:00")
    var selectedOption by remember { mutableStateOf(timerOptions[0]) }
//    var expanded by remember { mutableStateOf(false) }
    val musicOptions = listOf(
        "Rain" to Icons.Default.Cloud,
        "Wave" to Icons.Default.Water,
        "Forest" to Icons.Default.Park,
        "River" to Icons.Default.Opacity
    )
    var selectedMusicName by remember { mutableStateOf(musicOptions[0].first) }
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFF001A3F),
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF001A3F),
                    navigationIconContentColor = Color(0xFF001A3F)
                ),
                title = {
                    Text(
                        "ZenTimer",
                        fontFamily = MyCustomFont,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(state = scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            // Masukkan komponen DurationPicker yang tadi dibuat
            Text(
                text = "Set Duration:",
                color = Color.White,
                fontFamily = MyCustomFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            timerOptions.forEach { option ->
                val isSelected = (selectedOption == option)

                Button(
                    onClick = { selectedOption = option },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .height(60.dp)
                        .shadow(if (isSelected) 12.dp else 2.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF4A9ACB) else Color(0xFFD8E7EE),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = option,
                        fontFamily = MyCustomFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        )
                }
            }

            //pick music
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Set Sound:",
                    color = Color.White,
                    fontFamily = MyCustomFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 20.dp, top = 20.dp)
                )

                Box(modifier = Modifier.height(380.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        userScrollEnabled = false
                    ) {
                        items(musicOptions) { (name, icon) ->
                            val isSelected = selectedMusicName == name

                            Card(
                                onClick = { selectedMusicName = name },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF4A9ACB) else Color(
                                        0xFFD8E7EE)
                                ),
                                border = if (isSelected) BorderStroke(3.dp, Color(0xFF001A3F)) else null,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = name,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color(0xFF001A3F)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = name,
                                        color = Color(0xFF001A3F),
                                        fontFamily = MyCustomFont,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                //Start Meditasi button
                Button(
                    onClick = { onStartClick(selectedOption, selectedMusicName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A9ACB),
                        contentColor = Color(0xFF001A3F)
                    ),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(
                        "Start Meditation",
                        fontSize = 28.sp,
                        fontFamily = MyCustomFont,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(time: String, music: String, onBackClick: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    val (minutes, seconds) = time.split(':').map { it.toInt() }
    val initialSeconds = minutes * 60 + seconds
    var remainingSeconds by remember { mutableIntStateOf(initialSeconds) }
    var isRunning by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    DisposableEffect(music) {
        val musicResourceId = when(music.lowercase()) {
            "rain" -> R.raw.rain
            "waves" -> R.raw.waves
            "forest" -> R.raw.forest
            "river" -> R.raw.river
            else -> 0
        }

        if (musicResourceId != 0) {
            val rawUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(context.packageName)
                .appendEncodedPath("raw/${music.lowercase()}")
                .build()

            val mediaItem = MediaItem.fromUri(rawUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ONE // loop the music
            exoPlayer.prepare()
        }

        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            exoPlayer.play()
            while (isRunning && remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            if (remainingSeconds == 0) {
                // auto-stop when finished
                isRunning = false
            }
            exoPlayer.pause() // Pause when timer stops or is paused by user
        } else {
            exoPlayer.pause()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        containerColor = Color(0xFF001A3F), // Background Biru Gelap
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF001A3F),
                    navigationIconContentColor = Color(0xFF001A3F)
                ),
                title = {
                    Text("ZenTimer",
                        fontFamily = MyCustomFont,
                        fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) }
            )
        }
    ) { innerPadding ->
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    // Judul rata tengah
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Stop Session", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally // Memastikan semua isi Column di tengah
                    ) {
                        Text(
                            text = "Are you sure you want to stop this session?",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Row untuk menempatkan tombol berdampingan di tengah
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center, // Membuat tombol ke tengah horizontal
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // Tombol YES (Border Only)
                            OutlinedButton(
                                onClick = {
                                    showDialog = false
                                    onBackClick()
                                },
                                border = BorderStroke(1.dp, Color(0xFF001A3F)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f) // Memberi ukuran seimbang
                                    .height(48.dp)
                            ) {
                                Text("Yes", color = Color(0xFF001A3F))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = { showDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001A3F)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f) // Memberi ukuran seimbang
                                    .height(48.dp)
                            ) {
                                Text("No", color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {},
                containerColor = Color(0xFFE6EBF2)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))

            // Display Waktu
            Text(
                text = formatTime(remainingSeconds),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontFamily = MyCustomFont,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "Playing: $music",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = MyCustomFont,
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(60.dp))

            //Breathing Effect
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
                if (isRunning && remainingSeconds > 0) {
                    // Glow effect (Lingkaran Luar)
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .background(Color(0xFFD1E5F0).copy(alpha = 0.2f), CircleShape)
                    )
                    // Lingkaran Utama
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .background(Color(0xFFD1E5F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val isBreathingIn = (remainingSeconds % 8) >= 4
                        Text(
                            text = if (isBreathingIn) "Breath In" else "Breath Out",
                            color = Color(0xFF001A3F),
                            fontFamily = MyCustomFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp
                        )
                    }
                } else {
                    // Tampilan saat Pause atau Done
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (remainingSeconds == 0) "Done" else "Paused",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button Play/Pause
                Button(
                    onClick = { isRunning = !isRunning },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1E5F0)),
                    modifier = Modifier.size(88.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Control",
                        tint = Color(0xFF001A3F),
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Button Reset
                IconButton(
                    onClick = {
                        remainingSeconds = initialSeconds
                        isRunning = false
                        exoPlayer.seekTo(0) // Reset musik ke awal
                    },
                    modifier = Modifier
                        .size(88.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Reset",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
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
