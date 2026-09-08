package com.studentcompanion.app.core.navigation

/** Type-safe route definitions — single source of truth for navigation. */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object OnboardingSubjects : Screen("onboarding/subjects")

    data object Home : Screen("home")

    data object Notes : Screen("notes")
    data object NoteDetail : Screen("notes/{noteId}") {
        fun createRoute(noteId: String) = "notes/$noteId"
    }

    data object Homework : Screen("homework")
    data object HomeworkSubmit : Screen("homework/{taskId}/submit") {
        fun createRoute(taskId: String) = "homework/$taskId/submit"
    }
    data object TaskDetail : Screen("task/{taskId}") {
        fun createRoute(taskId: String) = "task/$taskId"
    }

    data object Points : Screen("points")
}
