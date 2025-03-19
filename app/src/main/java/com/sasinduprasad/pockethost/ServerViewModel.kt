package com.sasinduprasad.pockethost

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class ServerViewModel : ViewModel() {
    private val _serverStatus = MutableStateFlow(false)
    val serverStatus: StateFlow<Boolean> = _serverStatus.asStateFlow()

    private val _uiState = MutableStateFlow(AddProjectUiState())
    val uiState: StateFlow<AddProjectUiState> = _uiState.asStateFlow()

    private val _serverUptime = MutableStateFlow("00m:00s")
    val serverUptime: StateFlow<String> = _serverUptime.asStateFlow()

    private var uptimeTimer: Job? = null
    private var startTime: Long = 0

    private val _totalRequests = MutableStateFlow(0)
    val totalRequests: StateFlow<Int> = _totalRequests.asStateFlow()



    fun updateProjectName(name: String) {
        _uiState.value = _uiState.value.copy(projectName = name)
    }

    fun updateProjectFile(file: Uri) {
        _uiState.value = _uiState.value.copy(file = file)
    }

    fun updateProjectFolder(file: Uri) {
        _uiState.value = _uiState.value.copy(folder = file)
    }

    fun setServerStatus(isRunning: Boolean) {
        _serverStatus.value = isRunning
        Log.i("ServerViewModel", "Server status changed: $isRunning")

        if (isRunning) {
            startUptimeTracking()
        } else {
            stopUptimeTracking()
        }
    }

    private fun startUptimeTracking() {
        startTime = System.currentTimeMillis()
        uptimeTimer?.cancel()
        uptimeTimer = viewModelScope.launch {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val uptimeMillis = currentTime - startTime

                val minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMillis) % 60

                _serverUptime.value = String.format("%02dm:%02ds", minutes, seconds)
                Log.i("ServerViewModel", "Server uptime: ${_serverUptime.value}")
                delay(1000)
            }
        }
    }

    private fun stopUptimeTracking() {
        uptimeTimer?.cancel()
        _serverUptime.value = "00m:00s"
        Log.i("ServerViewModel", "Server uptime reset to 00m:00s")
    }

    fun recordRequest(startTime: Long, endTime: Long) {
        val responseTime = endTime - startTime
        Log.i("ServerViewModel", "Request received. Response time: ${responseTime}ms")

        viewModelScope.launch {
            _totalRequests.value += 1
        }
    }

    override fun onCleared() {
        uptimeTimer?.cancel()
        Log.i("ServerViewModel", "ViewModel cleared, uptime tracking stopped.")
        super.onCleared()
    }
}

data class AddProjectUiState(
    val projectName: String = "",
    val file: Uri? = null,
    val folder: Uri? = null
)
