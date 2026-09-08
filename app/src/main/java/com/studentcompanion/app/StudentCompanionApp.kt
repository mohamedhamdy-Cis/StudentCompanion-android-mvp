package com.studentcompanion.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. @HiltAndroidApp triggers Hilt's code generation,
 * making the DI container available to every Android component below it.
 */
@HiltAndroidApp
class StudentCompanionApp : Application()
