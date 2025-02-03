package com.account

import kotlinx.serialization.Serializable

@Serializable
data class AccountModel(
    val id: Int? = null,
    val username: String
)
