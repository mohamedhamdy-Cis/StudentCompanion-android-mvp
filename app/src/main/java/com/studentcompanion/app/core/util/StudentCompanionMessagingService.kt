package com.studentcompanion.app.core.util

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles push notifications: prayer/homework reminders for students,
 * submission alerts for parents, and review-ready alerts for teachers.
 * MVP stub — wire actual notification building/display + FCM token
 * upload to the user's profile before pilot launch.
 */
class StudentCompanionMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO: build a NotificationCompat notification from message.notification / message.data
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: persist token to the current user's Firestore document so
        // Cloud Functions can target this device for reminders/alerts.
    }
}
