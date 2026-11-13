package com.studentcompanion.app.domain.model

import java.time.Instant

/** A short-lived, single-use code a student generates and shares with a parent out of band. */
data class LinkCode(
    val code: String,
    val expiresAt: Instant,
)
