package com.example.zentimer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Water
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerViewModel : ViewModel() {

    private val _timerOptions = MutableStateFlow(listOf("05:00", "10:00", "15:00", "Set Custom Time"))
    val timerOptions: StateFlow<List<String>> = _timerOptions.asStateFlow()

    private val _selectedOption = MutableStateFlow(timerOptions.value[0])
    val selectedOption: StateFlow<String> = _selectedOption.asStateFlow()

    private val _musicOptions = MutableStateFlow<List<Pair<String, ImageVector>>>(
        listOf(
            "Rain" to Icons.Default.Cloud,
            "Waves" to Icons.Default.Water,
            "Forest" to Icons.Default.Park,
            "River" to Icons.Default.Opacity
        )
    )
    val musicOptions: StateFlow<List<Pair<String, ImageVector>>> = _musicOptions.asStateFlow()

    private val _selectedMusicName = MutableStateFlow(musicOptions.value[0].first)
    val selectedMusicName: StateFlow<String> = _selectedMusicName.asStateFlow()

    private val _showCustomTimeDialog = MutableStateFlow(false)
    val showCustomTimeDialog: StateFlow<Boolean> = _showCustomTimeDialog.asStateFlow()

    fun onTimerOptionSelected(option: String) {
        if (option == "Set custom time") {
            _showCustomTimeDialog.value = true
        } else {
            _selectedOption.value = option
        }
    }

    fun onMusicSelected(musicName: String) {
        _selectedMusicName.value = musicName
    }

    fun onCustomTimeSet(minutes: String, seconds: String) {
        val mins = minutes.toIntOrNull() ?: 0
        val secs = seconds.toIntOrNull() ?: 0
        val newTime = String.format("%02d:%02d", mins, secs)

        if (newTime !in _timerOptions.value) {
            val newOptions = _timerOptions.value.toMutableList()
            newOptions.add(_timerOptions.value.size - 1, newTime)
            _timerOptions.value = newOptions
        }
        _selectedOption.value = newTime
        _showCustomTimeDialog.value = false
    }

    fun onCustomTimeDialogDismissed() {
        _showCustomTimeDialog.value = false
    }
}