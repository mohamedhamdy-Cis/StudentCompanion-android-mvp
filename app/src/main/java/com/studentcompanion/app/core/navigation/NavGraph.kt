package com.studentcompanion.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.studentcompanion.app.presentation.auth.login.LoginScreen
import com.studentcompanion.app.presentation.auth.register.RegisterScreen
import com.studentcompanion.app.presentation.home.HomeScreen
import com.studentcompanion.app.presentation.homework.HomeworkScreen
import com.studentcompanion.app.presentation.homework.HomeworkSubmitScreen
import com.studentcompanion.app.presentation.notes.NoteDetailScreen
import com.studentcompanion.app.presentation.notes.NotesScreen
import com.studentcompanion.app.presentation.onboarding.OnboardingSubjectsScreen
import com.studentcompanion.app.presentation.points.PointsScreen
import com.studentcompanion.app.presentation.session.SessionViewModel
import com.studentcompanion.app.presentation.task.TaskDetailScreen

/**
 * Single NavHost for the whole app. Each feature package owns its own
 * Screen composable + ViewModel; this graph only wires routes together.
 */
@Composable
fun StudentCompanionNavGraph(
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val session by sessionViewModel.uiState.collectAsStateWithLifecycle()
    if (!session.isReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (session.isLoggedIn) Screen.Home.route else Screen.Login.route,
    ) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.OnboardingSubjects.route) { popUpTo(0) } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.OnboardingSubjects.route) {
            OnboardingSubjectsScreen(
                onContinue = { navController.navigate(Screen.Home.route) { popUpTo(0) } }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onOpenNotes = { navController.navigate(Screen.Notes.route) },
                onOpenHomework = { navController.navigate(Screen.Homework.route) },
                onOpenPoints = { navController.navigate(Screen.Points.route) },
                onOpenTask = { taskId -> navController.navigate(Screen.TaskDetail.createRoute(taskId)) }
            )
        }

        composable(Screen.Notes.route) {
            NotesScreen(
                onOpenNote = { noteId -> navController.navigate(Screen.NoteDetail.createRoute(noteId)) }
            )
        }
        composable(Screen.NoteDetail.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId").orEmpty()
            NoteDetailScreen(noteId = noteId, onBack = { navController.popBackStack() })
        }

        composable(Screen.Homework.route) {
            HomeworkScreen(
                onOpenSubmit = { taskId -> navController.navigate(Screen.HomeworkSubmit.createRoute(taskId)) }
            )
        }
        composable(Screen.HomeworkSubmit.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
            HomeworkSubmitScreen(taskId = taskId, onSubmitted = { navController.popBackStack() })
        }

        composable(Screen.TaskDetail.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onSubmitHomework = { id -> navController.navigate(Screen.HomeworkSubmit.createRoute(id)) },
            )
        }

        composable(Screen.Points.route) {
            PointsScreen()
        }
    }
}
