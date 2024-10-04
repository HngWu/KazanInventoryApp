package com.example.kazaninventoryapp.Models
import kotlinx.serialization.Serializable


@Serializable
class Asset
(
    val ID: Int,
    val AssetSN: String,
    val AssetName: String,
    val DepartmetnLocationID: Int,
    val EmployeeID: Int,
    val AssetGroupID: Int,
    val Description: String,
    val WarrantyDate: String,
    val DepartmentName : String,
    val AssetGroupName : String,
)