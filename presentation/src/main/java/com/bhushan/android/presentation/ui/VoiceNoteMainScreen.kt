package com.bhushan.android.presentation.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bhushan.android.presentation.R
import com.bhushan.android.presentation.vm.VoiceNoteEvent
import com.bhushan.android.presentation.vm.VoiceNoteViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun VoiceNoteMainScreen(
    onItemClick: (noteId: Long) -> Unit
) {
    val viewModel: VoiceNoteViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()
    var permissionGranted by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Permission dialog
    AudioPermissionRequest(
        onGranted = { permissionGranted = true }
    )

    if (!permissionGranted) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    // Collect events and show snackbar if needed
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is VoiceNoteEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!state.isRecording) {
                FloatingActionButton(
                    onClick = viewModel::onRecordClicked,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic),
                        contentDescription = "Start Recording",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (state.isRecording) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        VoiceRecordingControl(
                            isRecording = state.isRecording,
                            elapsedMillis = state.recordingMillis,
                            onRecordClick = viewModel::onRecordClicked,
                            onStopClick = viewModel::onStopClicked
                        )
                    }
                }

                Text(
                    text = "Voice Notes",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(8.dp)
                )

                VoiceNotesGrid(
                    notes = state.notes,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceNotesGrid(
    notes: List<com.bhushan.android.domain.model.VoiceNote>,
    onItemClick: (Long) -> Unit
) {
    val columns = 2
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            VoiceNoteGridItem(
                note = note,
                onClick = { onItemClick(note.id) }
            )
        }
    }
}

@Composable
fun VoiceNoteGridItem(
    note: com.bhushan.android.domain.model.VoiceNote,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                shape = shape
            )
            .clickable { onClick() },
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        shape = shape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = note.text.take(70).ifEmpty { "No transcript" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                        Date(
                            note.timestamp
                        )
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                IconButton(onClick = onClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Note",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceRecordingControl(
    isRecording: Boolean,
    elapsedMillis: Long,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit
) {
    if (isRecording) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedMic(elapsedMillis)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onStopClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stop),
                        contentDescription = "Stop"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("STOP RECORDING")
                }
            }
        }
    }
}

@Composable
fun AnimatedMic(elapsedMillis: Long) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            drawCircle(
                color = Color.Red.copy(alpha = 0.16f),
                radius = size.minDimension / 2 * pulse
            )
            drawCircle(
                color = Color.Red.copy(alpha = 0.28f),
                radius = size.minDimension / 2 * (pulse - 0.18f)
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_mic),
            contentDescription = "Mic",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
    }
    Text(
        text = formatMillis(elapsedMillis),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier
    )
}

fun formatMillis(ms: Long): String {
    val seconds = ms / 1000 % 60
    val minutes = ms / 1000 / 60
    return String.format("%02d:%02d", minutes, seconds)
}