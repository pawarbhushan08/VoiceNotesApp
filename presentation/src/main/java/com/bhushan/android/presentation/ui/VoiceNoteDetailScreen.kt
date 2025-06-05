package com.bhushan.android.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bhushan.android.presentation.R
import com.bhushan.android.presentation.vm.VoiceNoteEvent
import com.bhushan.android.presentation.vm.VoiceNoteViewModel
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceNoteDetailScreen(
    noteId: Long,
    onBack: () -> Unit
) {
    val viewModel: VoiceNoteViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()
    val note = state.notes.find { it.id == noteId }
    var editedText by remember { mutableStateOf(note?.text ?: "") }
    LaunchedEffect(noteId, note?.text) {
        if (note?.text != null && note.text != editedText) {
            editedText = note.text
        }
    }
    val isEditingWithAudio = state.isRecordingEditId == noteId
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VoiceNoteEvent.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Voice Note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!isEditingWithAudio) {
                FloatingActionButton(
                    onClick = { viewModel.startEditTranscription(noteId) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = "Edit with Audio",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                FloatingActionButton(
                    onClick = { viewModel.stopEditTranscription(noteId) },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stop),
                        contentDescription = "Stop Audio Edit",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val halfScreen = maxHeight * 0.5f
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 72.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = halfScreen, max = halfScreen),
                    value = editedText,
                    onValueChange = { editedText = it },
                    label = { Text("Edit Text") },
                    singleLine = false,
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (isEditingWithAudio) {
                    Text(
                        text = "Recording... Speak your new note.",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    AnimatedMic(elapsedMillis = state.recordingMillis)
                }
            }

            // Save button at bottom center
            Button(
                onClick = {
                    viewModel.editVoiceNote(noteId, editedText)
                    onBack()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .fillMaxWidth(0.55f), // a bit narrower
                enabled = !isEditingWithAudio
            ) {
                Text("Save")
            }
        }
    }
}