package com.helix.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Sponsor(
    val userName: String,
    val avatar: String,
    val amount: String,
)
