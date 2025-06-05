package com.bhushan.android.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun AudioPermissionRequest(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
) {
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var asked by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onGranted() else onDenied()
    }

    val hasPermission = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(Unit) {
        if (!hasPermission.value && !asked) {
            showDialog = true
        } else if (hasPermission.value) {
            onGranted()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Microphone Permission") },
            text = { Text("VoiceNoteX needs permission to record audio for speech-to-text.") },
            confirmButton = {
                Button(onClick = {
                    asked = true
                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                    showDialog = false
                }) { Text("Allow") }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog = false
                    onDenied()
                }) { Text("Deny") }
            }
        )
    }
}