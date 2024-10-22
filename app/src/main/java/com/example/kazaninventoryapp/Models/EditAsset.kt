package com.example.kazaninventoryapp.Models

class EditAsset
    (
    val ID: Int,
    val AssetSN: String,
    val AssetName: String,
    val DepartmentID: Int,
    val EmployeeID: Int,
    val AssetGroupID: Int,
    val Description: String,
    val WarrantyDate: String,
    val DepartmentName : String,
    val AssetGroupName : String,
    val Location: String,
    val images: List<String>?

)