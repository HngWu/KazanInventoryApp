package com.example.kazaninventoryapp.Models

import kotlinx.serialization.Serializable

@Serializable
data class TransferHistory(
    val id: Int,
    val transferDate: String,
    val fromAssetSn: String,
    val toAssetSn: String,
    val oldDepartment: String,
    val newDepartment: String
)