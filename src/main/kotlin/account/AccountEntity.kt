package com.account

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: Int? = null,
    val username: String
)
