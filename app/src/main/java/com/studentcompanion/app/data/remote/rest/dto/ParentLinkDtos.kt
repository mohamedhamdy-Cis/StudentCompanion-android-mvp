package com.studentcompanion.app.data.remote.rest.dto

data class LinkCodeResponse(
    val code: String,
    val expiresAt: String,
)

data class RedeemCodeRequest(
    val code: String,
)
