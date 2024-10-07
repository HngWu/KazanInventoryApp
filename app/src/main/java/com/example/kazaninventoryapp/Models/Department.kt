package com.example.kazaninventoryapp.Models

import kotlinx.serialization.Serializable


@Serializable
data class Department(
    val id: Long,
    val name: String,
    val departmentLocations: List<DepartmentLocation> = listOf()
)