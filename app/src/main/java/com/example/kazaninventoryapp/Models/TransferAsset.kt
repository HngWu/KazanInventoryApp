package com.example.kazaninventoryapp.Models

import kotlinx.serialization.Serializable

@Serializable
data class TransferAsset (
    val ID: Int,
    val AssetSN: String,
    val DepartmentID: Int,
    val Location: String,
    )