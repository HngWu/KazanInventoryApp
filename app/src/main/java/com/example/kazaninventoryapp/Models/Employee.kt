package com.example.kazaninventoryapp.Models

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val ID: Int,
    val FirstName: String,
)