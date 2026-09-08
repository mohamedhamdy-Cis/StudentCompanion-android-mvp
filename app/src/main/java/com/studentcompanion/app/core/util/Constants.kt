package com.studentcompanion.app.core.util

object Constants {
    // Firestore collection names — centralised so a future REST migration
    // only touches the data-source implementations, not call sites.
    const val COLLECTION_USERS = "users"
    const val COLLECTION_CLASSES = "classes"
    const val COLLECTION_SUBJECTS = "subjects"
    const val COLLECTION_NOTES = "notes"
    const val COLLECTION_TASKS = "tasks"
    const val COLLECTION_POINTS_LEDGER = "points_ledger"

    // Firebase Storage
    const val STORAGE_HOMEWORK_ATTACHMENTS = "homework_attachments"
    const val MAX_ATTACHMENT_SIZE_BYTES = 5L * 1024 * 1024 // 5 MB per homework image

    // Points model (kept centralised so the whole app agrees on scoring)
    const val POINTS_PRAYER_ON_TIME = 10
    const val POINTS_PRAYER_LATE = 5
    const val POINTS_HOMEWORK_SUBMITTED = 15
    const val POINTS_NOTE_ADDED = 5
    const val POINTS_STREAK_BONUS_5_DAY = 20
}
