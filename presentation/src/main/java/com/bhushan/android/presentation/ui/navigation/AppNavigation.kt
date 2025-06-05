package com.bhushan.android.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bhushan.android.presentation.ui.VoiceNoteDetailScreen
import com.bhushan.android.presentation.ui.VoiceNoteMainScreen
import com.bhushan.android.presentation.vm.VoiceNoteViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    object VoiceRecorder : Screen("voice_recorder")
    object VoiceNoteDetail : Screen("details/{noteId}") {
        fun createRoute(noteId: Long): String = "details/$noteId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.VoiceRecorder.route
    ) {
        composable(Screen.VoiceRecorder.route) {
            VoiceNoteMainScreen(
                onItemClick = { noteId ->
                    navController.navigate(Screen.VoiceNoteDetail.createRoute(noteId))
                }
            )
        }
        composable(Screen.VoiceNoteDetail.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull()
            if (noteId != null) {
                VoiceNoteDetailScreen(
                    noteId = noteId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}