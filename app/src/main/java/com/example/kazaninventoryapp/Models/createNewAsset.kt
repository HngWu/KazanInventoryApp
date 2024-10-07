package com.example.kazaninventoryapp.Models

import android.media.Image
import kotlinx.serialization.Serializable

@Serializable
data class createNewAsset(
    val AssetSN: String,
    val AssetName: String,
    val DepartmentID: Int,
    val Location: String,
    val EmployeeID: Int,
    val AssetGroupID: Int,
    val Description: String,
    val WarrantyDate: String,
    val images: List<String>

)