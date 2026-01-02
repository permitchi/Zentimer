package com.example.zentimer

import android.content.ContentResolver
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zentimer.ui.theme.ZentimerTheme
import kotlinx.coroutines.delay
import android.net.Uri

val MyCustomFont = FontFamily(
    Font(R.font.poppins_medium, FontWeight.Normal),
    Font(R.font.star_cartoon, FontWeight.Bold)
)

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
    val timerViewModel: TimerViewModel = viewModel()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            StartScreen(
                viewModel = timerViewModel,
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
fun StartScreen(viewModel: TimerViewModel, onStartClick: (String, String) -> Unit) {
    val timerOptions by viewModel.timerOptions.collectAsState()
    val selectedOption by viewModel.selectedOption.collectAsState()
    val showCustomTimeDialog by viewModel.showCustomTimeDialog.collectAsState()
    val musicOptions by viewModel.musicOptions.collectAsState()
    val selectedMusicName by viewModel.selectedMusicName.collectAsState()
    val scrollState = rememberScrollState()

    if (showCustomTimeDialog) {
        var customMinutes by remember { mutableStateOf("10") }
        var customSeconds by remember { mutableStateOf("00") }

        AlertDialog(
            onDismissRequest = { viewModel.onCustomTimeDialogDismissed() },
            title = { Text("Set Custom Time", fontFamily = MyCustomFont, fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) customMinutes = it },
                        modifier = Modifier.width(80.dp),
                        label = { Text("Mins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text(" : ", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    OutlinedTextField(
                        value = customSeconds,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) customSeconds = it },
                        modifier = Modifier.width(80.dp),
                        label = { Text("Secs") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onCustomTimeSet(customMinutes, customSeconds) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A9ACB))
                ) {
                    Text("Set", color = Color(0xFF001A3F))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.onCustomTimeDialogDismissed() },
                    border = BorderStroke(1.dp, Color(0xFF4A9ACB))
                ) {
                    Text("Cancel", color = Color(0xFF4A9ACB))
                }
            }
        )
    }

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
                    onClick = { viewModel.onTimerOptionSelected(option) },
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Set Sound:",
                    color = Color.White,
                    fontFamily = MyCustomFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(bottom = 20.dp, top = 50.dp)
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
                                onClick = { viewModel.onMusicSelected(name) },
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


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(time: String, music: String, onBackClick: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    val (minutes, seconds) = time.split(':').map { it.toInt() }
    val initialSeconds = minutes * 60 + seconds
    var remainingSeconds by remember { mutableIntStateOf(initialSeconds) }
    var isRunning by remember { mutableStateOf(true) }

    BackHandler { showDialog = true }

    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            volume = 1.0f
        }
    }

    DisposableEffect(music) {
        val musicFilename = when (music.lowercase()) {
            "wave" -> "waves"
            else -> music.lowercase()
        }
        val resourceId = context.resources.getIdentifier(musicFilename, "raw", context.packageName)

        if (resourceId != 0) {
            val uri = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/$resourceId")
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ONE
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
                isRunning = false
            }
            exoPlayer.pause()
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
        containerColor = Color(0xFF001A3F),
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
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = "Stop Session", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF001A3F))
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Are you sure you want to stop this session?",
                            color = Color(0xFF001A3F),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)

                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            OutlinedButton(
                                onClick = {
                                    showDialog = false
                                    onBackClick()
                                },
                                border = BorderStroke(1.dp, Color(0xFF001A3F)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .weight(1f)
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
                                    .weight(1f)
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

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
                if (isRunning && remainingSeconds > 0) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .background(Color(0xFFD1E5F0).copy(alpha = 0.2f), CircleShape)
                    )
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

                IconButton(
                    onClick = {
                        remainingSeconds = initialSeconds
                        isRunning = false
                        exoPlayer.seekTo(0)
                    },
                    modifier = Modifier
                        .size(88.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
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
