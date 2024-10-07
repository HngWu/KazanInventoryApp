package com.example.kazaninventoryapp.Models

import kotlinx.serialization.Serializable


@Serializable
data class DepartmentLocation(
    val id: Long,
    val departmentId: Long,
    val locationId: Long,
    val startDate: String, // DateOnly is not available in Kotlin, using String instead
    val endDate: String? = null
)